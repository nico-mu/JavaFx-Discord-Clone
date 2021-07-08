package de.uniks.stp.controller;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.component.ChatMessage;
import de.uniks.stp.component.ChatMessageInput;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.jpa.model.DirectMessageDTO;
import de.uniks.stp.minigame.GameInvitation;
import de.uniks.stp.model.DirectMessage;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.User;
import de.uniks.stp.network.rest.MediaRequestClient;
import de.uniks.stp.network.rest.ServerInformationHandler;
import de.uniks.stp.network.rest.SessionRestClient;
import de.uniks.stp.network.websocket.WebSocketService;
import de.uniks.stp.notification.NotificationService;
import de.uniks.stp.util.InviteInfo;
import de.uniks.stp.util.MessageUtil;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
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
    private final NotificationService notificationService;
    private final SessionDatabaseService databaseService;
    private final WebSocketService webSocketService;
    private final ChatMessage.ChatMessageFactory chatMessageFactory;

    private VBox onlineUsersContainer;
    private Label homeScreenLabel;

    @Inject
    MiniGameController.MiniGameControllerFactory miniGameControllerFactory;

    private final Parent view;
    private MiniGameController miniGameController;
    private final PropertyChangeListener messagesChangeListener = this::handleNewPrivateMessage;
    private final PropertyChangeListener statusChangeListener = this::onStatusChange;


    @AssistedInject
    public PrivateChatController(Editor editor,
                                 NotificationService notificationService,
                                 SessionDatabaseService databaseService,
                                 WebSocketService webSocketService,
                                 ViewLoader viewLoader,
                                 ServerInformationHandler serverInformationHandler,
                                 SessionRestClient restClient,
                                 MediaRequestClient mediaRequestClient,
                                 ChatMessageInput chatMessageInput,
                                 ChatMessage.ChatMessageFactory chatMessageFactory,
                                 @Assisted Parent view,
                                 @Assisted User user) {
        super(editor, serverInformationHandler, restClient, mediaRequestClient, chatMessageInput, viewLoader);
        this.view = view;
        this.user = user;
        this.notificationService = notificationService;
        this.databaseService = databaseService;
        this.webSocketService = webSocketService;
        this.chatMessageFactory = chatMessageFactory;
    }

    @Override
    public void init() {
        this.miniGameController = miniGameControllerFactory.create(user);
        miniGameController.init();

        onlineUsersContainer = (VBox) view.lookup(ONLINE_USERS_CONTAINER_ID);
        homeScreenLabel = (Label) view.lookup(HOME_SCREEN_LABEL_ID);

        //add chatMessageList
        onlineUsersContainer.getChildren().add(chatMessageList);
        //add chatMessageInput
        onlineUsersContainer.getChildren().add(chatMessageInput);

        User otherUser = editor.getOtherUserById(user.getId());

        notificationService.consume(user);
        notificationService.removePublisher(user);

        boolean status = Objects.nonNull(otherUser);
        user.setStatus(status);
        changeChatViewStatus(status);

        chatMessageInput.setOnMessageSubmit(this::handleMessageSubmit);

        loadMessages();
        addPropertyChangeListeners();
        VBox.setVgrow(chatMessageList, Priority.ALWAYS);
    }

    @Override
    public void stop() {
        if (Objects.nonNull(user)) {
            user.listeners().removePropertyChangeListener(User.PROPERTY_SENT_MESSAGES, messagesChangeListener);
            user.listeners().removePropertyChangeListener(User.PROPERTY_PRIVATE_CHAT_MESSAGES, messagesChangeListener);
            user.listeners().removePropertyChangeListener(User.PROPERTY_STATUS, statusChangeListener);
        }
        mediaRequestClient.stop();
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

            if(MiniGameController.isPlayMessage(messageText) && message.getTimestamp() > timestampBefore){
                if(message.getSender().getName().equals(currentUser.getName())){
                    message.setMessage(viewLoader.loadLabel(Constants.LBL_GAME_WAIT));
                } else{
                    message.setMessage(viewLoader.loadLabel(Constants.LBL_GAME_CHALLENGE));
                }
            } else {
                message.removeYou();
                return null;
            }
        } else if (!message.getSender().getName().equals(currentUser.getName())) {
            // check if message contains a server invite link
            info = MessageUtil.getInviteInfo(messageText);
        }

        ChatMessage messageNode = chatMessageFactory.create(message, false);

        if (Objects.nonNull(info) && Objects.isNull(editor.getServer(info.getServerId()))) {
            messageNode.addJoinButtonButton(info, this::joinServer);
        }
        mediaRequestClient.addMedia(message, messageNode);

        return messageNode;
    }

    protected void loadMessages() {
        List<DirectMessageDTO> directMessages = databaseService.getConversation(currentUser.getName(), user.getName());
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
        databaseService.saveDirectMessage(msg);

        // send message to the server
        webSocketService.sendPrivateMessage(user.getName(), message);
    }

    private void setOnlineHeaderLabel() {
        Platform.runLater(() -> {
            homeScreenLabel.setText(user.getName());
        });
    }

    private void setOfflineHeaderLabel() {
        Platform.runLater(() -> {
            homeScreenLabel.setText(user.getName() + " (" + viewLoader.loadLabel(Constants.LBL_USER_OFFLINE) + ")");
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

    @AssistedFactory
    public interface PrivateChatControllerFactory {
        PrivateChatController create(Parent view, User user);
    }
}
