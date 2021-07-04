package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.component.ChatMessage;
import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.jpa.model.DirectMessageDTO;
import de.uniks.stp.minigame.GameInvitation;
import de.uniks.stp.model.DirectMessage;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.User;
import de.uniks.stp.network.MediaRequestClient;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.WebSocketService;
import de.uniks.stp.notification.NotificationService;
import de.uniks.stp.util.InviteInfo;
import de.uniks.stp.util.MessageUtil;
import de.uniks.stp.util.UrlUtil;
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
public class PrivateChatController extends ChatController<DirectMessage> implements ControllerInterface {

    private static final String ONLINE_USERS_CONTAINER_ID = "#online-users-container";
    private static final String HOME_SCREEN_LABEL_ID = "#home-screen-label";

    /**
     * contains chat partner user object
     */
    private final User user;
    private final Parent view;
    private final MiniGameController miniGameController;
    private final MediaRequestClient mediaRequestClient = NetworkClientInjector.getMediaRequestClient();
    private final PropertyChangeListener messagesChangeListener = this::handleNewPrivateMessage;
    private VBox onlineUsersContainer;
    private Label homeScreenLabel;
    private final PropertyChangeListener statusChangeListener = this::onStatusChange;


    public PrivateChatController(Parent view, Editor editor, User user) {
        super(editor);
        this.view = view;
        this.user = user;
        this.miniGameController = new MiniGameController(user);
        miniGameController.init();
    }

    @Override
    public void init() {
        onlineUsersContainer = (VBox) view.lookup(ONLINE_USERS_CONTAINER_ID);
        homeScreenLabel = (Label) view.lookup(HOME_SCREEN_LABEL_ID);

        //add chatMessageList
        onlineUsersContainer.getChildren().add(chatMessageList);
        //add chatMessageInput
        onlineUsersContainer.getChildren().add(chatMessageInput);

        User otherUser = editor.getOtherUserById(user.getId());

        NotificationService.consume(user);
        NotificationService.removePublisher(user);

        boolean status = Objects.nonNull(otherUser);
        user.setStatus(status);
        changeChatViewStatus(status);

        chatMessageInput.setOnMessageSubmit(this::handleMessageSubmit);

        loadMessages();
        addPropertyChangeListeners();
    }

    @Override
    public void stop() {
        if (Objects.nonNull(user)) {
            user.listeners().removePropertyChangeListener(User.PROPERTY_SENT_MESSAGES, messagesChangeListener);
            user.listeners().removePropertyChangeListener(User.PROPERTY_PRIVATE_CHAT_MESSAGES, messagesChangeListener);
            user.listeners().removePropertyChangeListener(User.PROPERTY_STATUS, statusChangeListener);
        }
    }

    @Override
    protected ChatMessage parseMessage(Message message) {
        String messageText = message.getMessage();
        long timestampBefore = new Date().getTime() - GameInvitation.TIMEOUT;
        InviteInfo info = null;

        if (miniGameController.isIncomingCommandMessage(messageText)) {
            if (!message.getSender().getName().equals(currentUser.getName())) {
                miniGameController.handleIncomingMessage((DirectMessage) message);  //when sent by chatPartner
            }

            if (MiniGameController.isPlayMessage(messageText) && message.getTimestamp() > timestampBefore) {
                if (message.getSender().getName().equals(currentUser.getName())) {
                    message.setMessage(ViewLoader.loadLabel(Constants.LBL_GAME_WAIT));
                } else {
                    message.setMessage(ViewLoader.loadLabel(Constants.LBL_GAME_CHALLENGE));
                }
            } else {
                message.removeYou();
                return null;
            }
        } else if (!message.getSender().getName().equals(currentUser.getName())) {
            // check if message contains a server invite link
            info = MessageUtil.getInviteInfo(messageText);
        }

        ChatMessage messageNode = new ChatMessage(message, editor.getOrCreateAccord().getLanguage(), false);

        if (Objects.nonNull(info) && Objects.isNull(editor.getServer(info.getServerId()))) {
            messageNode.addJoinButtonButton(info, this::joinServer);
        }

        UrlUtil.addMedia(message, messageNode);

        return messageNode;
    }

    protected void loadMessages() {
        List<DirectMessageDTO> directMessages = DatabaseService.getConversation(currentUser.getName(), user.getName());
        for (DirectMessageDTO directMessageDTO : directMessages) {
            DirectMessage message = (DirectMessage) new DirectMessage()
                .setMessage(directMessageDTO.getMessage())
                .setId(directMessageDTO.getId().toString())
                .setTimestamp(directMessageDTO.getTimestamp().getTime());

            if (directMessageDTO.getSenderName().equals(currentUser.getName())) {
                message.setSender(currentUser);
            } else {
                String senderId = directMessageDTO.getSender();
                String senderName = directMessageDTO.getSenderName();
                message.setSender(editor.getOrCreateChatPartnerOfCurrentUser(senderId, senderName));
            }

            ChatMessage messageElement = parseMessage(message);

            if (Objects.nonNull(messageElement)) {
                chatMessageList.addElement(message, parseMessage(message));
            }
        }
    }

    private void addPropertyChangeListeners() {
        // first remove PCL in case this is a reload and they are already there!
        user.listeners().removePropertyChangeListener(User.PROPERTY_SENT_MESSAGES, messagesChangeListener);
        user.listeners().removePropertyChangeListener(User.PROPERTY_PRIVATE_CHAT_MESSAGES, messagesChangeListener);
        user.listeners().removePropertyChangeListener(User.PROPERTY_STATUS, statusChangeListener);
        // now add them
        user.listeners().addPropertyChangeListener(User.PROPERTY_SENT_MESSAGES, messagesChangeListener);
        user.listeners().addPropertyChangeListener(User.PROPERTY_PRIVATE_CHAT_MESSAGES, messagesChangeListener);
        user.listeners().addPropertyChangeListener(User.PROPERTY_STATUS, statusChangeListener);
    }

    /**
     * Creates DirectMessage, saves it in the model and sends it via websocket.
     * Adds other user to chatPartner list of currentUser if not already contained.
     *
     * @param message
     */
    private void handleMessageSubmit(String message) {
        if (MiniGameController.isPlayMessage(message)) {
            miniGameController.handleOutgoingPlayMessage(message);
        }

        // create & save message
        DirectMessage msg = (DirectMessage) new DirectMessage().setMessage(message).setSender(currentUser)
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
            ChatMessage messageElement = parseMessage(directMessage);
            if (Objects.nonNull(messageElement)) {
                Platform.runLater(() -> {
                    chatMessageList.addElement(directMessage, parseMessage(directMessage));
                });
            }
        }
    }

    private void changeChatViewStatus(boolean mode) {
        if (mode) {
            chatMessageInput.enable();
            setOnlineHeaderLabel();
        } else {
            chatMessageInput.disable();
            setOfflineHeaderLabel();
        }
    }

    private void onStatusChange(PropertyChangeEvent propertyChangeEvent) {
        boolean status = (boolean) propertyChangeEvent.getNewValue();
        changeChatViewStatus(status);
    }
}
