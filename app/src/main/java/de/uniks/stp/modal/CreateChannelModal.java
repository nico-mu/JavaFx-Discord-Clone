package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.component.UserCheckList;
import de.uniks.stp.component.UserCheckListEntry;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.User;
import de.uniks.stp.network.rest.SessionRestClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class CreateChannelModal extends AbstractModal {
    private static final Logger log = LoggerFactory.getLogger(CreateChannelModal.class);

    public static final String ADD_CHANNEL_NAME_TEXTFIELD = "#add-channel-name-textfield";
    public static final String TYPE_TOGGLE_BUTTON = "#type-toggle-button";
    public static final String PRIVILEGED_CHECKBOX = "#privileged-checkbox";
    public static final String FILTER_USER_TEXTFIELD = "#filter-user-textfield";
    public static final String USER_CHECK_LIST_CONTAINER = "#user-check-list-container";
    public static final String ADD_CHANNEL_CREATE_BUTTON = "#add-channel-create-button";
    public static final String ADD_CHANNEL_CANCEL_BUTTON = "#add-channel-cancel-button";
    public static final String ADD_CHANNEL_ERROR_LABEL = "#add-channel-error";
    private final JFXTextField channelName;
    private final JFXToggleButton typeToggleButton;
    private final JFXCheckBox privileged;
    private final JFXTextField filter;
    private final HBox userCheckListContainer;
    private final UserCheckList selectUserList;
    private final JFXButton createButton;
    private final JFXButton cancelButton;
    private final Label errorLabel;
    private final Category category;

    private final SessionRestClient restClient;
    private final ViewLoader viewLoader;

    @AssistedInject
    public CreateChannelModal(SessionRestClient restClient,
                              ViewLoader viewLoader,
                              @Assisted Parent root,
                              @Assisted Category category) {
        super(root);
        this.category = category;
        this.restClient = restClient;
        this.viewLoader = viewLoader;

        setTitle(viewLoader.loadLabel(Constants.LBL_CREATE_CHANNEL));
        channelName = (JFXTextField) view.lookup(ADD_CHANNEL_NAME_TEXTFIELD);
        typeToggleButton = (JFXToggleButton) view.lookup(TYPE_TOGGLE_BUTTON);
        privileged = (JFXCheckBox) view.lookup(PRIVILEGED_CHECKBOX);
        filter = (JFXTextField) view.lookup(FILTER_USER_TEXTFIELD);
        userCheckListContainer = (HBox) view.lookup(USER_CHECK_LIST_CONTAINER);
        createButton = (JFXButton) view.lookup(ADD_CHANNEL_CREATE_BUTTON);
        cancelButton = (JFXButton) view.lookup(ADD_CHANNEL_CANCEL_BUTTON);
        errorLabel = (Label) view.lookup(ADD_CHANNEL_ERROR_LABEL);

        selectUserList = new UserCheckList();
        selectUserList.setMaxHeight(userCheckListContainer.getMaxHeight());
        selectUserList.setDisable(true);
        userCheckListContainer.getChildren().add(selectUserList);

        createButton.setOnAction(this::onCreatButtonClicked);
        cancelButton.setOnAction(this::onCancelButtonClicked);
        cancelButton.setCancelButton(true);  // use Escape in order to press button
        filter.textProperty().addListener(((observable, oldValue, newValue) -> {
            filterUsers(newValue);
        }));
        privileged.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            selectUserList.setDisable(!newValue);
        }));

        for (User user : category.getServer().getUsers()) {
            UserCheckListEntry userCheckListEntry = new UserCheckListEntry(user);
            selectUserList.addUserToChecklist(userCheckListEntry);
        }
    }

    private void filterUsers(String newValue) {
        selectUserList.filterUsers(newValue);
    }

    private void onCreatButtonClicked(ActionEvent actionEvent) {
        setErrorMessage(null);
        String chName = channelName.getText();
        Boolean priv = privileged.isSelected();
        String serverId = category.getServer().getId();
        String categoryId = category.getId();
        boolean voiceChannel = typeToggleButton.isSelected();
        String type = voiceChannel ? "audio" : "text";

        restClient.createChannel(serverId, categoryId, chName, type, priv, selectUserList.getSelectedUserIds(), this::handleCreateChannelResponse);
    }

    private void setErrorMessage(String label) {
        if (Objects.isNull(label)) {
            Platform.runLater(() -> {
                errorLabel.setText("");
            });
            return;
        }
        String message = viewLoader.loadLabel(label);
        Platform.runLater(() -> {
            errorLabel.setText(message);
        });
    }

    private void handleCreateChannelResponse(HttpResponse<JsonNode> jsonNodeHttpResponse) {
        log.debug("Received create channel response: " + jsonNodeHttpResponse.getBody().toPrettyString());

        if (jsonNodeHttpResponse.isSuccess()) {
            Platform.runLater(this::close);
        } else {
            log.error("Create server failed: " + jsonNodeHttpResponse.getBody().toPrettyString());
            String errorMessage = jsonNodeHttpResponse.getBody().getObject().getString("message");
            if (errorMessage.equals("Missing name")) {
                setErrorMessage(Constants.LBL_MISSING_NAME);
            } else if (errorMessage.equals("Missing members") || errorMessage.equals("Members list may not be empty")) {
                setErrorMessage(Constants.LBL_MISSING_MEMBERS);
            } else {
                log.error("Unhandled create server response: " + jsonNodeHttpResponse.getBody().toPrettyString());
            }
        }
    }

    private void onCancelButtonClicked(ActionEvent actionEvent) {
        this.close();
    }

    @Override
    public void close() {
        createButton.setOnAction(null);
        cancelButton.setOnAction(null);
        super.close();
    }

    @AssistedFactory
    public interface CreateChannelModalFactory {
        CreateChannelModal create(Parent view, Category category);
    }
}
