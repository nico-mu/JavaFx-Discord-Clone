package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.component.PrivateChatView;
import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.jpa.model.DirectMessageDTO;
import de.uniks.stp.model.DirectMessage;
import de.uniks.stp.model.User;
import de.uniks.stp.network.WebSocketService;
import de.uniks.stp.notification.NotificationService;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

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
    private final User user;
    private PrivateChatView chatView;
    private VBox onlineUsersContainer;
    private Label homeScreenLabel;

    private final PropertyChangeListener messagesChangeListener = this::handleNewPrivateMessage;
    private final PropertyChangeListener statusChangeListener = this::onStatusChange;

    public PrivateChatController(Parent view, Editor editor, String userId, String userName) {
        this.view = view;
        this.editor = editor;
        this.userId = userId;
        this.user = editor.getOrCreateChatPartnerOfCurrentUser(userId, userName);
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
            user.listeners().removePropertyChangeListener(User.PROPERTY_SENT_MESSAGES, messagesChangeListener);
            user.listeners().removePropertyChangeListener(User.PROPERTY_PRIVATE_CHAT_MESSAGES, messagesChangeListener);
            user.listeners().removePropertyChangeListener(User.PROPERTY_STATUS, statusChangeListener);
        }
    }

    /**
     * Creates PrivateChatView with name of .
     * Also adds all messages from model in the View and creates PropertyChangeListener that will do so in the future.
     */
    private void showChatView() {
        if (Objects.isNull(user)) {
            return;
        }
        chatView = new PrivateChatView(editor.getOrCreateAccord().getLanguage());
        onlineUsersContainer.getChildren().add(chatView);

        User otherUser = editor.getUserById(userId);

        NotificationService.consume(user);
        NotificationService.removePublisher(user);

        // User is offline
        if (Objects.isNull(otherUser)) {
            user.setStatus(false);
            setOfflineHeaderLabel();
            chatView.disable();
        }
        // User is online
        else {
            user.setStatus(true);
            setOnlineHeaderLabel();
            chatView.enable();
        }

        chatView.setOnMessageSubmit(this::handleMessageSubmit);
        user.listeners().addPropertyChangeListener(User.PROPERTY_SENT_MESSAGES, messagesChangeListener);
        user.listeners().addPropertyChangeListener(User.PROPERTY_PRIVATE_CHAT_MESSAGES, messagesChangeListener);
        user.listeners().addPropertyChangeListener(User.PROPERTY_STATUS, statusChangeListener);

        User currentUser = editor.getOrCreateAccord().getCurrentUser();
        List<DirectMessageDTO> directMessages = DatabaseService.getConversation(currentUser.getName(), user.getName());
        for (DirectMessageDTO directMessageDTO : directMessages) {
            DirectMessage message = (DirectMessage) new DirectMessage()
                .setMessage(directMessageDTO.getMessage())
                .setId(directMessageDTO.getId().toString())
                .setTimestamp(directMessageDTO.getTimestamp().getTime());

            if (directMessageDTO.getSenderName().equals(currentUser.getName())) {
                message.setSender(currentUser);
                chatView.appendMessage(message);
            } else {
                String senderId = directMessageDTO.getSender();
                String senderName = directMessageDTO.getSenderName();
                message.setSender(editor.getOrCreateChatPartnerOfCurrentUser(senderId, senderName));
            }
        }
    }

    /**
     * Creates DirectMessage, saves it in the model and sends it via websocket.
     * Adds other user to chatPartner list of currentUser if not already contained.
     *
     * @param message
     */
    private void handleMessageSubmit(String message) {
        // create & save message
        DirectMessage msg = (DirectMessage) new DirectMessage().setMessage(message).setSender(editor.getOrCreateAccord().getCurrentUser())
            .setTimestamp(new Date().getTime()).setId(UUID.randomUUID().toString());
        // This fires handleNewPrivateMessage()
        msg.setReceiver(user);
        DatabaseService.saveDirectMessage(msg);

        // send message to the server
        WebSocketService.sendPrivateMessage(user.getName(), message);
    }

    private void setOnlineHeaderLabel() {
        Platform.runLater(() -> {
            homeScreenLabel.setText(user.getName());
        });
    }

    private void setOfflineHeaderLabel() {
        Platform.runLater(() -> {
            homeScreenLabel.setText(user.getName() + " (" + ViewLoader.loadLabel(Constants.LBL_USER_OFFLINE) + ")");
        });
    }

    private void handleNewPrivateMessage(PropertyChangeEvent propertyChangeEvent) {
        DirectMessage directMessage = (DirectMessage) propertyChangeEvent.getNewValue();

        if (Objects.nonNull(directMessage)) {
            chatView.appendMessage(directMessage);
        }
    }

    private void onStatusChange(PropertyChangeEvent propertyChangeEvent) {
        Boolean status = (Boolean) propertyChangeEvent.getNewValue();

        if (Objects.isNull(status) || !status) {
            chatView.disable();
            setOfflineHeaderLabel();
        } else {
            chatView.enable();
            setOnlineHeaderLabel();
        }
    }
}
