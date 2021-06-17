package de.uniks.stp.controller;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.component.PrivateChatView;
import de.uniks.stp.emote.EmoteParser;
import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.jpa.model.DirectMessageDTO;
import de.uniks.stp.modal.EasterEggModal;
import de.uniks.stp.model.DirectMessage;
import de.uniks.stp.model.User;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.WebSocketService;
import de.uniks.stp.notification.NotificationService;
import de.uniks.stp.util.MessageUtil;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

@Route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_PRIVATE_CHAT)
public class PrivateChatController implements ControllerInterface {
    private static final Logger log = LoggerFactory.getLogger(ServerChatController.class);
    private static final String ONLINE_USERS_CONTAINER_ID = "#online-users-container";
    private static final String HOME_SCREEN_LABEL_ID = "#home-screen-label";

    private final Parent view;
    private final Editor editor;
    private final User chatPartner;
    private final User currentUser;
    private PrivateChatView chatView;
    private VBox onlineUsersContainer;
    private Label homeScreenLabel;

    private final MiniGameController miniGameController;

    private final PropertyChangeListener messagesChangeListener = this::handleNewPrivateMessage;
    private final PropertyChangeListener statusChangeListener = this::onStatusChange;

    public PrivateChatController(Parent view, Editor editor, String userId, String userName) {
        this.view = view;
        this.editor = editor;
        this.chatPartner = editor.getOrCreateChatPartnerOfCurrentUser(userId, userName);
        this.currentUser = editor.getOrCreateAccord().getCurrentUser();
        this.miniGameController = new MiniGameController(chatPartner);
        miniGameController.init();
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
        if (Objects.nonNull(chatPartner)) {
            chatPartner.listeners().removePropertyChangeListener(User.PROPERTY_SENT_MESSAGES, messagesChangeListener);
            chatPartner.listeners().removePropertyChangeListener(User.PROPERTY_PRIVATE_CHAT_MESSAGES, messagesChangeListener);
            chatPartner.listeners().removePropertyChangeListener(User.PROPERTY_STATUS, statusChangeListener);
        }
    }

    /**
     * Creates PrivateChatView with name of .
     * Also adds all messages from model in the View and creates PropertyChangeListener that will do so in the future.
     */
    private void showChatView() {
        if (Objects.isNull(chatPartner)) {
            return;
        }
        chatView = new PrivateChatView(editor.getOrCreateAccord().getLanguage());
        onlineUsersContainer.getChildren().add(chatView);

        NotificationService.consume(chatPartner);
        NotificationService.removePublisher(chatPartner);

        // User is offline
        if (Objects.isNull(chatPartner)) {
            chatPartner.setStatus(false);
            setOfflineHeaderLabel();
            chatView.disable();
        }
        // User is online
        else {
            chatPartner.setStatus(true);
            setOnlineHeaderLabel();
            chatView.enable();
        }

        chatView.setOnMessageSubmit(this::handleMessageSubmit);
        chatPartner.listeners().addPropertyChangeListener(User.PROPERTY_SENT_MESSAGES, messagesChangeListener);
        chatPartner.listeners().addPropertyChangeListener(User.PROPERTY_PRIVATE_CHAT_MESSAGES, messagesChangeListener);
        chatPartner.listeners().addPropertyChangeListener(User.PROPERTY_STATUS, statusChangeListener);

        List<DirectMessageDTO> directMessages = DatabaseService.getConversation(currentUser.getName(), chatPartner.getName());
        for (DirectMessageDTO directMessageDTO : directMessages) {
            DirectMessage message = (DirectMessage) new DirectMessage()
                .setMessage(directMessageDTO.getMessage())
                .setId(directMessageDTO.getId().toString())
                .setTimestamp(directMessageDTO.getTimestamp().getTime());

            if (directMessageDTO.getSenderName().equals(currentUser.getName())) {
                message.setSender(currentUser);

                // check if message contains a server invite link
                Pair<String, String> ids = MessageUtil.getInviteIds(message.getMessage());
                if ((ids != null) && (!editor.serverAdded(ids.getKey()))) {
                    chatView.appendMessageWithButton(message, ids, this::joinServer);
                } else {
                    chatView.appendMessage(message);
                }
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
        if (miniGameController.isOutgoingCommandMessage(message)) {
            miniGameController.handleOutgoingMessage(message);
        } else {
            // create & save message
            DirectMessage msg = (DirectMessage) new DirectMessage().setMessage(message).setSender(currentUser)
                .setTimestamp(new Date().getTime()).setId(UUID.randomUUID().toString());
            // This fires handleNewPrivateMessage()
            msg.setReceiver(chatPartner);
            DatabaseService.saveDirectMessage(msg);
        }
        // send message to the server
        WebSocketService.sendPrivateMessage(chatPartner.getName(), message);
    }

    private void setOnlineHeaderLabel() {
        Platform.runLater(() -> {
            homeScreenLabel.setText(chatPartner.getName());
        });
    }

    private void setOfflineHeaderLabel() {
        Platform.runLater(() -> {
            homeScreenLabel.setText(chatPartner.getName() + " (" + ViewLoader.loadLabel(Constants.LBL_USER_OFFLINE) + ")");
        });
    }

    private void handleNewPrivateMessage(PropertyChangeEvent propertyChangeEvent) {
        DirectMessage directMessage = (DirectMessage) propertyChangeEvent.getNewValue();

        if (Objects.nonNull(directMessage)) {
            if (miniGameController.isIncomingCommandMessage(directMessage.getMessage())) {
                miniGameController.handleIncomingMessage(directMessage);
                directMessage.setReceiver(null).setSender(null);  //delete message fom model
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

        NetworkClientInjector.getRestClient().joinServer(serverId, inviteId, currentUser.getName(), currentUser.getPassword(),
            (response) -> onJoinServerResponse(serverId, response));
    }

    private void onJoinServerResponse(String serverId, HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toPrettyString());

        if (response.isSuccess()) {
            NetworkClientInjector.getRestClient().getServerInformation(serverId, this::onServerInformationResponse);
        } else {
            log.error("Join Server failed because: " + response.getBody().getObject().getString("message"));
        }
    }

    private void onServerInformationResponse(HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toString());
        if (response.isSuccess()) {
            JSONObject resJson = response.getBody().getObject().getJSONObject("data");
            final String serverId = resJson.getString("id");
            final String serverName = resJson.getString("name");

            // add server to model -> to NavBar List
            editor.getOrCreateServer(serverId, serverName);

            // reload chatView -> some button might not be needed anymore
            Platform.runLater(() -> {
                onlineUsersContainer.getChildren().remove(chatView);
                chatView.stop();
                showChatView();
            });
        } else {
            log.error("Error trying to load information of new server");
        }
    }
}
