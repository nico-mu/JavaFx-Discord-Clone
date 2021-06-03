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
    private static final Logger log = LoggerFactory.getLogger(MainScreenController.class);
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

        if (Objects.nonNull(user.getSentUserNotification())) {
            user.getSentUserNotification().setNotificationCounter(0);
        }

        chatView = new PrivateChatView();
        onlineUsersContainer.getChildren().add(chatView);

        User otherUser = editor.getUserById(userId);

        // User is offline
        if (Objects.isNull(otherUser)) {
            setOfflineHeaderLabel();
            chatView.disable();
        }
        // User is online
        else {
            setOnlineHeaderLabel();
        }

        chatView.setOnMessageSubmit(this::handleMessageSubmit);

        user.listeners().addPropertyChangeListener(User.PROPERTY_PRIVATE_CHAT_MESSAGES, messagesChangeListener);
        user.listeners().addPropertyChangeListener(User.PROPERTY_STATUS, statusChangeListener);


        User currentUser = editor.getOrCreateAccord().getCurrentUser();
        List<DirectMessageDTO> directMessages = DatabaseService.getDirectMessages(currentUser.getName(), user.getName());

        for (DirectMessageDTO directMessageDTO : directMessages) {
            DirectMessage message = (DirectMessage) new DirectMessage()
                .setMessage(directMessageDTO.getMessage())
                .setId(directMessageDTO.getId().toString())
                .setTimestamp(directMessageDTO.getTimestamp().getTime());

            if (directMessageDTO.getSender().equals(currentUser.getId())) {
                message.setSender(currentUser);
            } else {
                String senderId = directMessageDTO.getSender();
                if (editor.isChatPartnerOfCurrentUser(senderId)) {
                    message.setSender(editor.getChatPartnerOfCurrentUserById(senderId));
                }
            }

            chatView.appendMessage(message);
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
        DirectMessage msg = new DirectMessage();
        msg.setMessage(message).setSender(editor.getOrCreateAccord().getCurrentUser())
            .setTimestamp(new Date().getTime()).setId(UUID.randomUUID().toString());
        user.withPrivateChatMessages(msg);
        DatabaseService.saveDirectMessage(msg);

        // send message to the server
        WebSocketService.sendPrivateMessage(user.getName(), message);
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

        if (Objects.isNull(directMessage)) {
            return;
        }

        chatView.appendMessage(directMessage);
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
