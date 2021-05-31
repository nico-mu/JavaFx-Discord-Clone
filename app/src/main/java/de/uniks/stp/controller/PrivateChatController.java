package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.component.PrivateChatView;
import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.jpa.model.DirectMessageDTO;
import de.uniks.stp.model.Accord;
import de.uniks.stp.model.DirectMessage;
import de.uniks.stp.model.User;
import de.uniks.stp.network.WebSocketService;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_PRIVATE_CHAT)
public class PrivateChatController implements ControllerInterface {
    private static final String ONLINE_USERS_CONTAINER_ID = "#online-users-container";
    private static final String HOME_SCREEN_LABEL_ID = "#home-screen-label";

    private final Parent view;
    private final Editor editor;
    private final String userId;
    private User user;
    private PrivateChatView chatView;
    private VBox onlineUsersContainer;
    private Label homeScreenLabel;

    private final PropertyChangeListener messagesChangeListener = this::handleNewPrivateMessage;
    private final PropertyChangeListener otherUsersChangeListener = this::onOtherUsersChange;
    private final PropertyChangeListener statusChangeListener = this::onStatusChange;

    public PrivateChatController(Parent view, Editor editor, String userId, String userName) {
        this.view = view;
        this.editor = editor;
        this.userId = userId;
        this.user = editor.getUserById(userId);

        if (Objects.isNull(this.user)) {
            this.user = editor.getOrCreateOtherUser(userId, userName).setStatus(false);
        }
    }

    @Override
    public void init() {
        onlineUsersContainer = (VBox) view.lookup(ONLINE_USERS_CONTAINER_ID);
        homeScreenLabel = (Label) view.lookup(HOME_SCREEN_LABEL_ID);

        showChatView();
    }

    @Override
    public void stop() {
        if (Objects.nonNull(chatView)) {
            chatView.stop();
        }
        if (Objects.nonNull(user)) {
            user.listeners().removePropertyChangeListener(User.PROPERTY_PRIVATE_CHAT_MESSAGES, messagesChangeListener);
            editor.getOrCreateAccord().listeners().removePropertyChangeListener(Accord.PROPERTY_OTHER_USERS, otherUsersChangeListener);
            user.listeners().removePropertyChangeListener(User.PROPERTY_STATUS, statusChangeListener);
        }
    }

    /**
     * Creates PrivateChatView with name of .
     * Also adds all messages from model in the View and creates PropertyChangeListener that will do so in the future.
     */
    private void showChatView() {
        if (Objects.nonNull(user.getSentUserNotification())) {
            user.getSentUserNotification().setNotificationCounter(0);
        }

        chatView = new PrivateChatView();
        onlineUsersContainer.getChildren().add(chatView);

        // disable chat when user offline
        if (Objects.isNull(user)) {
            setHeaderLabel(ViewLoader.loadLabel(Constants.LBL_USER_OFFLINE));
            chatView.disable();
        } else {
            if (!user.isStatus()) {
                setOfflineHeaderLabel();
                chatView.disable();
            } else {
                setOnlineHeaderLabel();
            }

            chatView.setOnMessageSubmit(this::handleMessageSubmit);

            user.listeners().addPropertyChangeListener(User.PROPERTY_PRIVATE_CHAT_MESSAGES, messagesChangeListener);
            editor.getOrCreateAccord().listeners().addPropertyChangeListener(Accord.PROPERTY_OTHER_USERS, otherUsersChangeListener);
            user.listeners().addPropertyChangeListener(User.PROPERTY_STATUS, statusChangeListener);
        }

        List<DirectMessageDTO> directMessages = DatabaseService.getDirectMessages(userId);

        if (directMessages.size() > 0) {
            addUserToChatPartnerList();
        }

        for (DirectMessageDTO directMessageDTO : directMessages) {
            DirectMessage message = (DirectMessage) new DirectMessage()
                .setMessage(directMessageDTO.getMessage())
                .setId(directMessageDTO.getId().toString())
                .setTimestamp(directMessageDTO.getTimestamp().getTime())
                .setSender(editor.getOrCreateAccord().getCurrentUser());

            chatView.appendMessage(message);

            // TODO: To include the messages in the model a hashset should remember all messages from the db
        };
    }

    /**
     * Creates DirectMessage, saves it in the model and sends it via websocket.
     * Adds other user to chatPartner list of currentUser if not already contained.
     *
     * @param message
     */
    private void handleMessageSubmit(String message) {
        // create & save message
        DirectMessage msg = new DirectMessage();
        msg.setMessage(message).setSender(editor.getOrCreateAccord().getCurrentUser())
            .setTimestamp(new Date().getTime()).setId(UUID.randomUUID().toString());
        user.withPrivateChatMessages(msg);

        // add user to chatPartner list if not already in it
        addUserToChatPartnerList();

        // send message to the server
        WebSocketService.sendPrivateMessage(user.getName(), message);
    }

    private void addUserToChatPartnerList() {
        User currentUser = editor.getOrCreateAccord().getCurrentUser();

        if (!currentUser.getChatPartner().contains(user) && Objects.nonNull(user)) {
            currentUser.withChatPartner(user);
        }
    }

    private void setHeaderLabel(String text) {
        Platform.runLater(() -> {
            homeScreenLabel.setText(text);
        });
    }

    private void setOnlineHeaderLabel() {
        setHeaderLabel(user.getName());
    }

    private void setOfflineHeaderLabel() {
        setHeaderLabel(user.getName() + " (" + ViewLoader.loadLabel(Constants.LBL_USER_OFFLINE) + ")");
    }

    private void handleNewPrivateMessage(PropertyChangeEvent propertyChangeEvent) {
        DirectMessage directMessage = (DirectMessage) propertyChangeEvent.getNewValue();

        if (Objects.isNull(directMessage) || Objects.isNull(directMessage.getId())) {
            return;
        }

        chatView.appendMessage(directMessage);
        DatabaseService.saveDirectMessage(directMessage);
    }

    private void onOtherUsersChange(PropertyChangeEvent propertyChangeEvent) {
        User oldValue = (User) propertyChangeEvent.getOldValue();
        User newValue = (User) propertyChangeEvent.getNewValue();

        // User online
        if (Objects.isNull(oldValue) && Objects.nonNull(newValue) && newValue.getId().equals(user.getId())) {
            chatView.enable();
            setOnlineHeaderLabel();
        }
        // User offline
        else if (Objects.nonNull(oldValue) && oldValue.getId().equals(user.getId()) && Objects.isNull(newValue)) {
            chatView.disable();
            setOfflineHeaderLabel();
        }
    }

    private void onStatusChange(PropertyChangeEvent propertyChangeEvent) {
        boolean status = (boolean) propertyChangeEvent.getNewValue();

        if (status) {
            chatView.enable();
            setOnlineHeaderLabel();
        } else {
            chatView.disable();
            setOfflineHeaderLabel();
        }
    }
}
