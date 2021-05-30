package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.component.UserCheckList;
import de.uniks.stp.component.UserCheckListEntry;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.User;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class AddChannelModal extends AbstractModal {
    private static final Logger log = LoggerFactory.getLogger(AddChannelModal.class);

    public static final String ADD_CHANNEL_NAME_TEXTFIELD = "#add-channel-name-textfield";
    public static final String PRIVILEGED_CHECKBOX = "#privileged-checkbox";
    public static final String FILTER_USER_TEXTFIELD = "#filter-user-textfield";
    public static final String USER_CHECK_LIST_CONTAINER = "#user-check-list-container";
    public static final String ADD_CHANNEL_CREATE_BUTTON = "#add-channel-create-button";
    public static final String ADD_CHANNEL_CANCEL_BUTTON = "#add-channel-cancel-button";
    public static final String ADD_CHANNEL_ERROR_LABEL = "#add-channel-error";
    private JFXTextField channelName;
    private JFXCheckBox privileged;
    private JFXTextField filter;
    private HBox userCheckListContainer;
    private UserCheckList selectUserList;
    private JFXButton createButton;
    private JFXButton cancelButton;
    private Label errorLabel;
    private Category category;
    private HashMap<String, UserCheckListEntry> serverUserElementsMap;
    private RestClient restClient;

    public AddChannelModal(Parent root, Category category) {
        super(root);
        this.category = category;
        this.restClient = NetworkClientInjector.getRestClient();

        setTitle(ViewLoader.loadLabel(Constants.LBL_CREATE_CHANNEL));
        channelName = (JFXTextField) view.lookup(ADD_CHANNEL_NAME_TEXTFIELD);
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

        serverUserElementsMap = new HashMap<>();

        for (User user : category.getServer().getUsers()) {
            UserCheckListEntry userCheckListEntry = new UserCheckListEntry(user);
            serverUserElementsMap.put(user.getName(), userCheckListEntry);
            selectUserList.addUserCheckListEntry(userCheckListEntry);
        }
    }

    private void filterUsers(String newValue) {
        selectUserList.clearUserCheckList();
        for (String name : serverUserElementsMap.keySet()) {
            if (name.contains(newValue)) {
                selectUserList.addUserCheckListEntry(serverUserElementsMap.get(name));
            }
        }
    }

    private void onCreatButtonClicked(ActionEvent actionEvent) {
        setErrorMessage(null);
        String chName = channelName.getText();
        Boolean priv = privileged.isSelected();
        String serverId = category.getServer().getId();
        String categoryId = category.getId();

        ArrayList<String> selectedUserNames = new ArrayList<>();
        for (UserCheckListEntry userCheckListEntry : serverUserElementsMap.values()) {
            if (userCheckListEntry.isUserSelected()) {
                selectedUserNames.add(userCheckListEntry.getUserId());
            }
        }
        restClient.createTextChannel(serverId, categoryId, chName, priv, selectedUserNames, this::handleCreateChannelResponse);
    }

    private void setErrorMessage(String label) {
        if (Objects.isNull(label)) {
            Platform.runLater(() -> {
                errorLabel.setText("");
            });
            return;
        }
        String message = ViewLoader.loadLabel(label);
        Platform.runLater(() -> {
            errorLabel.setText(message);
        });
    }

    private void handleCreateChannelResponse(HttpResponse<JsonNode> jsonNodeHttpResponse) {
        log.debug("Received create server response: " + jsonNodeHttpResponse.getBody().toPrettyString());

        if (jsonNodeHttpResponse.isSuccess()) {
            Platform.runLater(this::close);
        } else {
            log.error("Create server failed: " + jsonNodeHttpResponse.getBody().toPrettyString());
            String errorMessage = jsonNodeHttpResponse.getBody().getObject().getString("message");
            if (errorMessage.equals("Missing name")) {
                setErrorMessage(Constants.LBL_MISSING_NAME);
            } else if (errorMessage.equals("Missing members")) {
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
}
