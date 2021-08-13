package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.component.UserCheckList;
import de.uniks.stp.component.UserCheckListEntry;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.User;
import de.uniks.stp.network.rest.SessionRestClient;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;

public class EditChannelModal extends AbstractModal {
    private static final Logger log = LoggerFactory.getLogger(CreateChannelModal.class);

    public static final String EDIT_CHANNEL_NAME_TEXTFIELD = "#edit-channel-name-textfield";
    public static final String NOTIFICATION_CONTAINER = "#notification-anchorpane";
    public static final String NOTIFICATIONS_TOGGLE_BUTTON = "#notifications-toggle-button";
    public static final String NOTIFICATIONS_ACTIVATED_LABEL = "#notifications-activated-label";
    public static final String PRIVILEGED_CHECKBOX = "#privileged-checkbox";
    public static final String FILTER_USER_TEXTFIELD = "#filter-user-textfield";
    public static final String USER_CHECK_LIST_CONTAINER = "#user-check-list-container";
    public static final String EDIT_CHANNEL_EDIT_BUTTON = "#edit-channel-create-button";
    public static final String EDIT_CHANNEL_CANCEL_BUTTON = "#edit-channel-cancel-button";
    public static final String EDIT_CHANNEL_ERROR_LABEL = "#edit-channel-error";
    public static final String EDIT_CHANNEL_DELETE_BUTTON = "#delete-channel";

    private final JFXTextField channelName;
    private final AnchorPane notificationAnchorPane;
    private final JFXToggleButton notificationsToggleButton;
    private final Label notificationsLabel;
    private final JFXCheckBox privileged;
    private final JFXTextField filter;
    private final HBox userCheckListContainer;
    private final UserCheckList selectUserList;
    private final JFXButton editButton;
    private final JFXButton cancelButton;
    private final Label errorLabel;
    private final JFXButton deleteButton;
    private final Category category;
    private final Channel channel;

    private final SessionDatabaseService databaseService;
    private final ViewLoader viewLoader;
    private ConfirmationModal confirmationModal;
    private final Editor editor;
    private final SessionRestClient restClient;


    private final ConfirmationModal.ConfirmationModalFactory confirmationModalFactory;

    @AssistedInject
    public EditChannelModal(Editor editor,
                            SessionRestClient restClient,
                            SessionDatabaseService databaseService,
                            ViewLoader viewLoader,
                            @Named("primaryStage") Stage primaryStage,
                            UserCheckList userCheckList,
                            UserCheckListEntry.UserCheckListEntryFactory userCheckListEntryFactory,
                            ConfirmationModal.ConfirmationModalFactory confirmationModalFactory,
                            @Assisted Parent root,
                            @Assisted Channel channel) {
        super(root, primaryStage);
        this.editor = editor;
        this.category = channel.getCategory();
        this.channel = channel;
        this.restClient = restClient;
        this.databaseService = databaseService;
        this.viewLoader = viewLoader;
        this.confirmationModalFactory = confirmationModalFactory;

        setTitle(viewLoader.loadLabel(Constants.LBL_EDIT_CHANNEL));
        channelName = (JFXTextField) view.lookup(EDIT_CHANNEL_NAME_TEXTFIELD);
        notificationAnchorPane = (AnchorPane)  view.lookup(NOTIFICATION_CONTAINER);
        notificationsToggleButton = (JFXToggleButton) view.lookup(NOTIFICATIONS_TOGGLE_BUTTON);
        notificationsLabel = (Label) view.lookup(NOTIFICATIONS_ACTIVATED_LABEL);
        privileged = (JFXCheckBox) view.lookup(PRIVILEGED_CHECKBOX);
        filter = (JFXTextField) view.lookup(FILTER_USER_TEXTFIELD);
        userCheckListContainer = (HBox) view.lookup(USER_CHECK_LIST_CONTAINER);
        editButton = (JFXButton) view.lookup(EDIT_CHANNEL_EDIT_BUTTON);
        cancelButton = (JFXButton) view.lookup(EDIT_CHANNEL_CANCEL_BUTTON);
        errorLabel = (Label) view.lookup(EDIT_CHANNEL_ERROR_LABEL);
        deleteButton = (JFXButton) view.lookup(EDIT_CHANNEL_DELETE_BUTTON);

        boolean muted = databaseService.isChannelMuted(channel.getId());
        boolean voice = channel.getType().equals("audio");
        if(voice) {
            notificationAnchorPane.getChildren().clear();
            notificationAnchorPane.setMinHeight(20);
        }
        notificationsToggleButton.setDisable(voice);
        notificationsToggleButton.setVisible(!voice);
        notificationsLabel.setVisible(!voice);
        notificationsToggleButton.setSelected(!muted);
        notificationsLabel.setText(viewLoader.loadLabel(muted ? Constants.LBL_OFF : Constants.LBL_ON));
        notificationsToggleButton.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            notificationsLabel.setText(viewLoader.loadLabel(!notificationsToggleButton.isSelected() ? Constants.LBL_OFF : Constants.LBL_ON));
        }));

        privileged.setSelected(channel.isPrivileged());

        selectUserList = userCheckList;
        selectUserList.setMaxHeight(userCheckListContainer.getMaxHeight());
        selectUserList.setDisable(!channel.isPrivileged());
        userCheckListContainer.getChildren().add(selectUserList);

        editButton.setOnAction(this::onEditButtonClicked);
        cancelButton.setOnAction(this::onCancelButtonClicked);
        cancelButton.setCancelButton(true);  // use Escape in order to press button
        deleteButton.setOnAction(this::onDeleteButtonClicked);

        channelName.setText(channel.getName());

        filter.textProperty().addListener(((observable, oldValue, newValue) -> {
            filterUsers(newValue);
        }));
        privileged.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            selectUserList.setDisable(!newValue);
        }));

        LinkedList<String> memberNames = new LinkedList<>();
        for(User user : channel.getChannelMembers()) {
            memberNames.add(user.getName());
        }
        for (User user : category.getServer().getUsers()) {
            if(user.getName().equals(editor.getOrCreateAccord().getCurrentUser().getName())) {
                continue;
            }
            UserCheckListEntry userCheckListEntry = userCheckListEntryFactory.create(user);
            if(memberNames.contains(user.getName())) {
                userCheckListEntry.setSelected(true);
            }
            selectUserList.addUserToChecklist(userCheckListEntry);
        }
        System.out.println(channel.getChannelMembers());
        System.out.println(channel.getServer().getUsers());
    }

    private void onDeleteButtonClicked(ActionEvent actionEvent) {
        Parent confirmationModalView = viewLoader.loadView(Views.CONFIRMATION_MODAL);
        confirmationModal = confirmationModalFactory.create(confirmationModalView,
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
        boolean priv = privileged.isSelected();
        String serverId = category.getServer().getId();
        String categoryId = category.getId();
        String channelId = channel.getId();

        boolean muted = !notificationsToggleButton.isSelected();
        if(muted) {
            databaseService.addMutedChannelId(channelId);
        }else {
            databaseService.removeMutedChannelId(channelId);
        }

        ArrayList<String> privilegedUserIds = selectUserList.getSelectedUserIds();
        if(priv) {
            if(!privilegedUserIds.contains(editor.getOrCreateAccord().getCurrentUser().getId())) {
                privilegedUserIds.add(editor.getOrCreateAccord().getCurrentUser().getId());
            }
        }

        if(hasRestChanges(chName, priv, privilegedUserIds)) {
            restClient.editTextChannel(serverId, categoryId, channelId, chName, priv, privilegedUserIds, this::handleEditChannelResponse);
        }else {
            this.close();
        }
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

    private void handleEditChannelResponse(HttpResponse<JsonNode> jsonNodeHttpResponse) {
        log.debug("Received edit channel response: " + jsonNodeHttpResponse.getBody().toPrettyString());

        if (jsonNodeHttpResponse.isSuccess()) {
            JSONObject data = jsonNodeHttpResponse.getBody().getObject().getJSONObject("data");
            JSONArray jsonMemberIds = data.getJSONArray("members");
            ArrayList<String> memberIds = (ArrayList<String>) jsonMemberIds.toList();
            for (User user : channel.getChannelMembers()) {
                if(! memberIds.contains(user.getId())) {
                    channel.withoutChannelMembers(user);
                }
            }
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

    private boolean hasRestChanges(String chName, boolean priv, ArrayList<String> selectedUsers) {
        if(!chName.equals(channel.getName())) {
            return true;
        }
        if(priv != channel.isPrivileged()) {
            return true;
        }
        if(selectedUsers.size() != channel.getChannelMembers().size()) {
            return true;
        }
        for(User user : channel.getChannelMembers()) {
            if(!selectedUsers.contains(user.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void close() {
        editButton.setOnAction(null);
        cancelButton.setOnAction(null);
        super.close();
    }

    @AssistedFactory
    public interface EditChannelModalFactory {
        EditChannelModal create(Parent view, Channel channel);
    }
}
