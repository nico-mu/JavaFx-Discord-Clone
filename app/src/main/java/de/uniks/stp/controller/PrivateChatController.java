package de.uniks.stp.controller;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.component.PrivateChatView;
import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.jpa.model.DirectMessageDTO;
import de.uniks.stp.model.DirectMessage;
import de.uniks.stp.model.User;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.network.ServerInformationHandler;
import de.uniks.stp.network.WebSocketService;
import de.uniks.stp.notification.NotificationService;
import de.uniks.stp.util.MessageUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
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
    private static final Logger log = LoggerFactory.getLogger(ServerChatController.class);
    private static final String ONLINE_USERS_CONTAINER_ID = "#online-users-container";
    private static final String HOME_SCREEN_LABEL_ID = "#home-screen-label";

    private final Parent view;
    private final Editor editor;
    private final String userId;
    private final User user;
    private final User currentUser;
    private PrivateChatView chatView;
    private VBox onlineUsersContainer;
    private Label homeScreenLabel;

    private final MiniGameController miniGameController;

    private final PropertyChangeListener messagesChangeListener = this::handleNewPrivateMessage;
    private final PropertyChangeListener statusChangeListener = this::onStatusChange;

    private final RestClient restClient;
    private final ServerInformationHandler serverInformationHandler;

    public PrivateChatController(Parent view, Editor editor, String userId, String userName) {
        this.view = view;
        this.editor = editor;
        this.userId = userId;
        this.user = editor.getOrCreateChatPartnerOfCurrentUser(userId, userName);
        this.currentUser = editor.getOrCreateAccord().getCurrentUser();
        this.miniGameController = new MiniGameController(user);
        miniGameController.init();
        restClient = NetworkClientInjector.getRestClient();
        serverInformationHandler = new ServerInformationHandler(editor);
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
        chatView = new PrivateChatView(editor.getOrCreateAccord().getLanguage(), currentUser);
        onlineUsersContainer.getChildren().add(chatView);

        User otherUser = editor.getOtherUserById(userId);

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
        addPropertyChangeListeners();

        List<DirectMessageDTO> directMessages = DatabaseService.getConversation(currentUser.getName(), user.getName());
        for (DirectMessageDTO directMessageDTO : directMessages) {
            DirectMessage message = (DirectMessage) new DirectMessage()
                .setMessage(directMessageDTO.getMessage())
                .setId(directMessageDTO.getId().toString())
                .setTimestamp(directMessageDTO.getTimestamp().getTime());

            if (directMessageDTO.getSenderName().equals(currentUser.getName())) {
                message.setSender(currentUser);

                if(miniGameController.isPlayMessage(message.getMessage())){
                    if(message.getTimestamp() > (new Date().getTime() - 30*1000)){
                        chatView.appendMessage(message);  //show game invitation when still active
                    }
                }
                else{

                    // check if message contains a server invite link
                    Pair<String, String> ids = MessageUtil.getInviteIds(message.getMessage());
                    if ((ids != null) && (!editor.serverAdded(ids.getKey()))) {
                        chatView.appendMessageWithButton(message, ids, this::joinServer);
                    } else {
                        chatView.appendMessage(message);
                    }
                }
            } else {
                String senderId = directMessageDTO.getSender();
                String senderName = directMessageDTO.getSenderName();
                message.setSender(editor.getOrCreateChatPartnerOfCurrentUser(senderId, senderName));
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
        if (miniGameController.isPlayMessage(message)) {
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
            if (miniGameController.isIncomingCommandMessage(directMessage.getMessage())) {
                if(! directMessage.getSender().getName().equals(currentUser.getName())){
                    miniGameController.handleIncomingMessage(directMessage);  //when sent by chatPartner
                }

                if(miniGameController.isPlayMessage(directMessage.getMessage())){
                    if(directMessage.getTimestamp() > (new Date().getTime() - 30*1000)){
                        chatView.appendMessage(directMessage);  //show game invitation when still active
                    }
                } else{
                    directMessage.setReceiver(null).setSender(null);  //delete ingame message fom model
                }
            } else {
                // check if message contains a server invite link
                Pair<String, String> ids = MessageUtil.getInviteIds(directMessage.getMessage());
                if ((ids != null) && (!editor.serverAdded(ids.getKey()))) {
                    chatView.appendMessageWithButton(directMessage, ids, this::joinServer);
                } else {
                    chatView.appendMessage(directMessage);
                }
            }
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

    private void joinServer(ActionEvent actionEvent) {
        JFXButton button = (JFXButton) actionEvent.getSource();
        String ids = button.getId();
        String serverId = ids.split("-")[0];
        String inviteId = ids.split("-")[1];

        restClient.joinServer(serverId, inviteId, currentUser.getName(), currentUser.getPassword(),
            (response) -> onJoinServerResponse(serverId, response));
    }

    private void onJoinServerResponse(String serverId, HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toPrettyString());

        if(response.isSuccess()){
            editor.getOrCreateServer(serverId);
            restClient.getServerInformation(serverId, serverInformationHandler::handleServerInformationRequest);
            restClient.getCategories(serverId, (msg) -> serverInformationHandler.handleCategories(msg, editor.getServer(serverId)));
        } else{
            log.error("Join Server failed because: " + response.getBody().getObject().getString("message"));
        }
    }
}
