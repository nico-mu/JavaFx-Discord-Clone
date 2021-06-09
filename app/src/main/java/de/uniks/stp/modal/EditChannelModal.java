package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.component.UserCheckList;
import de.uniks.stp.component.UserCheckListEntry;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.User;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.view.Views;
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

public class EditChannelModal extends AbstractModal {
    private static final Logger log = LoggerFactory.getLogger(AddChannelModal.class);

    public static final String EDIT_CHANNEL_NAME_TEXTFIELD = "#edit-channel-name-textfield";
    public static final String PRIVILEGED_CHECKBOX = "#privileged-checkbox";
    public static final String FILTER_USER_TEXTFIELD = "#filter-user-textfield";
    public static final String USER_CHECK_LIST_CONTAINER = "#user-check-list-container";
    public static final String EDIT_CHANNEL_EDIT_BUTTON = "#edit-channel-create-button";
    public static final String EDIT_CHANNEL_CANCEL_BUTTON = "#edit-channel-cancel-button";
    public static final String EDIT_CHANNEL_ERROR_LABEL = "#edit-channel-error";
    public static final String EDIT_CHANNEL_DELETE_BUTTON = "#delete-channel";
    private JFXTextField channelName;
    private JFXCheckBox privileged;
    private JFXTextField filter;
    private HBox userCheckListContainer;
    private UserCheckList selectUserList;
    private JFXButton editButton;
    private JFXButton cancelButton;
    private Label errorLabel;
    private JFXButton deleteButton;
    private Category category;
    private Channel channel;
    private RestClient restClient;
    private ConfirmationModal confirmationModal;

    public EditChannelModal(Parent root, Channel channel) {
        super(root);
        this.category = channel.getCategory();
        this.channel = channel;
        this.restClient = NetworkClientInjector.getRestClient();

        setTitle(ViewLoader.loadLabel(Constants.LBL_EDIT_CHANNEL));
        channelName = (JFXTextField) view.lookup(EDIT_CHANNEL_NAME_TEXTFIELD);
        privileged = (JFXCheckBox) view.lookup(PRIVILEGED_CHECKBOX);
        filter = (JFXTextField) view.lookup(FILTER_USER_TEXTFIELD);
        userCheckListContainer = (HBox) view.lookup(USER_CHECK_LIST_CONTAINER);
        editButton = (JFXButton) view.lookup(EDIT_CHANNEL_EDIT_BUTTON);
        cancelButton = (JFXButton) view.lookup(EDIT_CHANNEL_CANCEL_BUTTON);
        errorLabel = (Label) view.lookup(EDIT_CHANNEL_ERROR_LABEL);
        deleteButton = (JFXButton) view.lookup(EDIT_CHANNEL_DELETE_BUTTON);

        selectUserList = new UserCheckList();
        selectUserList.setMaxHeight(userCheckListContainer.getMaxHeight());
        selectUserList.setDisable(true);
        userCheckListContainer.getChildren().add(selectUserList);

        editButton.setOnAction(this::onEditButtonClicked);
        cancelButton.setOnAction(this::onCancelButtonClicked);
        cancelButton.setCancelButton(true);  // use Escape in order to press button
        deleteButton.setOnAction(this::onDeleteButtonClicked);

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

    private void onDeleteButtonClicked(ActionEvent actionEvent) {
        Parent confirmationModalView = ViewLoader.loadView(Views.CONFIRMATION_MODAL);
        confirmationModal = new ConfirmationModal(confirmationModalView,
            Constants.LBL_DELETE_CHANNEL,
            Constants.LBL_CONFIRM_DELETE_CHANNEL,
            this::onYesButtonClicked,
            this::onNoButtonClicked);
        confirmationModal.show();

        // disabling buttons improves the view
        editButton.setDisable(true);
        cancelButton.setDisable(true);
        deleteButton.setDisable(true);

    }

    private void onNoButtonClicked(ActionEvent actionEvent) {
        Platform.runLater(confirmationModal::close);
        editButton.setDisable(false);
        cancelButton.setDisable(false);
        deleteButton.setDisable(false);
    }

    private void onYesButtonClicked(ActionEvent actionEvent) {
        Platform.runLater(confirmationModal::close);
        editButton.setDisable(false);
        cancelButton.setDisable(false);
        deleteButton.setDisable(false);
        restClient.deleteChannel(channel.getServer().getId(), channel.getCategory().getId(), channel.getId(), this::handleDeleteChannelResponse);
    }

    private void handleDeleteChannelResponse(HttpResponse<JsonNode> jsonNodeHttpResponse) {
        log.debug("Received delete channel response: " + jsonNodeHttpResponse.getBody().toPrettyString());

        if (jsonNodeHttpResponse.isSuccess()) {
            Platform.runLater(this::close);
        }else {
            log.error("Unhandled create server response: " + jsonNodeHttpResponse.getBody().toPrettyString());
        }
    }

    private void filterUsers(String newValue) {
        selectUserList.filterUsers(newValue);
    }

    private void onEditButtonClicked(ActionEvent actionEvent) {
        setErrorMessage(null);
        String chName = channelName.getText();
        Boolean priv = privileged.isSelected();
        String serverId = category.getServer().getId();
        String categoryId = category.getId();
        String channelId = channel.getId();

        restClient.editTextChannel(serverId, categoryId, channelId, chName, priv, selectUserList.getSelectedUserIds(), this::handleEditChannelResponse);
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

    private void handleEditChannelResponse(HttpResponse<JsonNode> jsonNodeHttpResponse) {
        log.debug("Received edit channel response: " + jsonNodeHttpResponse.getBody().toPrettyString());

        if (jsonNodeHttpResponse.isSuccess()) {
            Platform.runLater(this::close);
        } else {
            log.error("Edit server failed: " + jsonNodeHttpResponse.getBody().toPrettyString());
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
        editButton.setOnAction(null);
        cancelButton.setOnAction(null);
        super.close();
    }
}
