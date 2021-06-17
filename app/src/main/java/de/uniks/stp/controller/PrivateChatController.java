package de.uniks.stp.controller;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.component.PrivateChatView;
import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.jpa.model.DirectMessageDTO;
import de.uniks.stp.model.*;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
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
import kong.unirest.json.JSONArray;
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
    private final String userId;
    private final User user;
    private PrivateChatView chatView;
    private VBox onlineUsersContainer;
    private Label homeScreenLabel;

    private final PropertyChangeListener messagesChangeListener = this::handleNewPrivateMessage;
    private final PropertyChangeListener statusChangeListener = this::onStatusChange;

    private final RestClient restClient;

    public PrivateChatController(Parent view, Editor editor, String userId, String userName) {
        this.view = view;
        this.editor = editor;
        this.userId = userId;
        this.user = editor.getOrCreateChatPartnerOfCurrentUser(userId, userName);
        restClient = NetworkClientInjector.getRestClient();
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

                // check if message contains a server invite link
                Pair<String, String> ids = MessageUtil.getInviteIds(message.getMessage());
                if((ids != null) && (!editor.serverAdded(ids.getKey()))){
                    chatView.appendMessageWithButton(message, ids, this::joinServer);
                } else{
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
            // check if message contains a server invite link
            Pair<String, String> ids = MessageUtil.getInviteIds(directMessage.getMessage());
            if((ids != null) && (!editor.serverAdded(ids.getKey()))){
                chatView.appendMessageWithButton(directMessage, ids, this::joinServer);
            } else{
                chatView.appendMessage(directMessage);
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

        User currentUser = editor.getOrCreateAccord().getCurrentUser();
        restClient.joinServer(serverId, inviteId, currentUser.getName(), currentUser.getPassword(),
            (response) -> onJoinServerResponse(serverId, response));
    }

    private void onJoinServerResponse(String serverId, HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toPrettyString());

        if(response.isSuccess()){
            editor.getOrCreateServer(serverId);
            restClient.getServerInformation(serverId, this::handleServerInformationRequest);
            restClient.getCategories(serverId, (msg) -> handleCategories(msg, editor.getServer(serverId)));
        } else{
            log.error("Join Server failed because: " + response.getBody().getObject().getString("message"));
        }
    }

    private void handleServerInformationRequest(HttpResponse<JsonNode> response) {
        if (response.isSuccess()) {
            final JSONObject data = response.getBody().getObject().getJSONObject("data");
            final JSONArray member = data.getJSONArray("members");
            final String serverId = data.getString("id");
            final String serverName = data.getString("name");
            final String serverOwner = data.getString("owner");

            // add server to model -> to NavBar List
            if (serverOwner.equals(editor.getOrCreateAccord().getCurrentUser().getId())) {
                editor.getOrCreateServer(serverId, serverName).setOwner(editor.getOrCreateAccord().getCurrentUser());
            } else {
                editor.getOrCreateServer(serverId, serverName);
            }

            member.forEach(o -> {
                JSONObject jsonUser = (JSONObject) o;
                String userId = jsonUser.getString("id");
                String name = jsonUser.getString("name");
                boolean status = Boolean.parseBoolean(jsonUser.getString("online"));

                User serverMember = editor.getOrCreateServerMember(userId, name, editor.getServer(serverId));
                serverMember.setStatus(status);
            });
        }
    }

    private void handleCategories(HttpResponse<JsonNode> response, Server server) {
        if (response.isSuccess()) {
            JSONArray categoriesJson = response.getBody().getObject().getJSONArray("data");
            for (Object category : categoriesJson) {
                JSONObject categoryJson = (JSONObject) category;
                final String name = categoryJson.getString("name");
                final String categoryId = categoryJson.getString("id");

                Category categoryModel = editor.getOrCreateCategory(categoryId, name, server);
                restClient.getChannels(server.getId(), categoryId, (msg) -> handleChannels(msg, server));
            }
        } else {
            //TODO: show error message
        }
    }

    private void handleChannels(HttpResponse<JsonNode> response, Server server) {
        if (response.isSuccess()) {
            JSONArray channelsJson = response.getBody().getObject().getJSONArray("data");
            for (Object channel : channelsJson) {
                JSONObject channelJson = (JSONObject) channel;
                final String name = channelJson.getString("name");
                final String channelId = channelJson.getString("id");
                final String categoryId = channelJson.getString("category");
                String type = channelJson.getString("type");
                boolean privileged = channelJson.getBoolean("privileged");
                JSONArray jsonMemberIds = channelJson.getJSONArray("members");
                ArrayList<String> memberIds = (ArrayList<String>) jsonMemberIds.toList();

                Category categoryModel = editor.getCategory(categoryId, server);
                Channel channelModel = editor.getChannel(channelId, server);
                if (Objects.nonNull(channelModel)) {
                    // Channel is already in model because it got added by a notification
                    channelModel.setCategory(categoryModel).setName(name);
                } else {
                    channelModel = editor.getOrCreateChannel(channelId, name, categoryModel);
                    channelModel.setServer(server);
                }
                channelModel.setType(type);
                channelModel.setPrivileged(privileged);
                for(User user : server.getUsers()) {
                    if(memberIds.contains(user.getId())) {
                        channelModel.withChannelMembers(user);
                    }
                }
                NotificationService.register(channelModel);
            }
        } else {
            //TODO: show error message
        }
    }

    private void onServerInformationResponse(HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toString());
        if(response.isSuccess()){
            JSONObject resJson = response.getBody().getObject().getJSONObject("data");
            final String serverId = resJson.getString("id");
            final String serverName = resJson.getString("name");

            // add server to model -> to NavBar List
            editor.getOrCreateServer(serverId, serverName);

            // reload chatView -> some button might not be needed anymore
            Platform.runLater(()->{
                onlineUsersContainer.getChildren().remove(chatView);
                chatView.stop();
                showChatView();
            });
        } else{
            log.error("Error trying to load information of new server");
        }
    }
}
