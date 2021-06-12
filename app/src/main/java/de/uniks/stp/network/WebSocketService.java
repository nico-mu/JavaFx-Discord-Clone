package de.uniks.stp.network;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.model.*;
import de.uniks.stp.notification.NotificationService;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class WebSocketService {
    private static final Logger log = LoggerFactory.getLogger(WebSocketService.class);

    private static final HashMap<String, WebSocketClient> pathWebSocketClientHashMap = new HashMap<>();
    private static Editor editor;
    private static User currentUser;

    public static void setEditor(Editor editor) {
        WebSocketService.editor = editor;
    }

    public static void stop() {
        if (!pathWebSocketClientHashMap.isEmpty()) {
            pathWebSocketClientHashMap.values().forEach(WebSocketClient::stop);
        }
        pathWebSocketClientHashMap.clear();
    }

    /**
     * Creates and saves WebSocketClients for private chat and system messages
     */
    public static void init() {
        currentUser = editor.getOrCreateAccord().getCurrentUser();

        final WebSocketClient systemWebSocketClient = NetworkClientInjector.getWebSocketClient(Constants.WS_SYSTEM_PATH, WebSocketService::onSystemMessage);
        pathWebSocketClientHashMap.put(Constants.WS_SYSTEM_PATH, systemWebSocketClient);


        String endpoint = Constants.WS_USER_PATH + currentUser.getName();
        final WebSocketClient privateWebSocketClient = NetworkClientInjector.getWebSocketClient(endpoint, WebSocketService::onPrivateMessage);
        pathWebSocketClientHashMap.put(endpoint, privateWebSocketClient);
    }

    /**
     * Creates WebSocketClients for system & chat messages for given server. Has to be called for each server.
     * WebSocketClients are saved in pathWebSocketClientHashMap, keys are the server unique endpoints.
     *
     * @param serverId
     */
    public static void addServerWebSocket(String serverId) {
        String endpoint = Constants.WS_SYSTEM_PATH + Constants.WS_SERVER_SYSTEM_PATH + serverId;
        if (!pathWebSocketClientHashMap.containsKey(endpoint)) {
            final WebSocketClient systemServerWSC = NetworkClientInjector.getWebSocketClient(endpoint, (msg) -> onServerSystemMessage(msg, serverId));
            pathWebSocketClientHashMap.put(endpoint, systemServerWSC);
        }

        endpoint = Constants.WS_USER_PATH + currentUser.getName() + Constants.WS_SERVER_CHAT_PATH + serverId;
        if (!pathWebSocketClientHashMap.containsKey(endpoint)) {
            final WebSocketClient chatServerWSC = NetworkClientInjector.getWebSocketClient(endpoint, (msg) -> onServerChatMessage(msg, serverId));
            pathWebSocketClientHashMap.put(endpoint, chatServerWSC);
        }
    }

    /**
     * Sends private chat message.
     *
     * @param receiverName
     * @param message
     */
    public static void sendPrivateMessage(String receiverName, String message) {
        JsonObject msgObject = Json.createObjectBuilder()
            .add("channel", "private")
            .add("to", receiverName)
            .add("message", message)
            .build();

        try {
            String endpointKey = Constants.WS_USER_PATH + currentUser.getName();
            pathWebSocketClientHashMap.get(endpointKey).sendMessage(msgObject.toString());
            log.debug("Message sent: {}", msgObject);
        } catch (IOException e) {
            log.error("WebSocketService: sendPrivateMessage failed", e);
        }
    }

    /**
     * Called when private chat message is received.
     * Creates DirectMessage Object and saves it in the model (list privateChatMessages of other user) if it isn't
     * sent by currentUser (then, it would already be in the model).
     * Adds other user to chatPartner list of currentUser if not already contained.
     *
     * @param jsonStructure
     */
    private static void onPrivateMessage(JsonStructure jsonStructure) {
        log.debug("Received private message with content: {}", jsonStructure.toString());

        final JSONObject jsonObject = new JSONObject(jsonStructure.asJsonObject().toString());

        final String from = jsonObject.getString("from");
        final long timestamp = jsonObject.getLong("timestamp");
        final String msgText = jsonObject.getString("message");
        final String to = jsonObject.getString("to");

        // in case it's sent by you
        if (from.equals(currentUser.getName())) {
            return;
        }

        User sender = editor.getOtherUser(from);
        if (Objects.isNull(sender)) {
            log.error("WebSocketService: Sender \"{}\" of received message is not in editor", from);
            return;
        }
        User chatPartner = editor.getOrCreateChatPartnerOfCurrentUser(sender.getId(), sender.getName());
        DirectMessage msg = new DirectMessage();
        msg.setReceiver(currentUser).setMessage(msgText).setTimestamp(timestamp).setId(UUID.randomUUID().toString());
        msg.setSender(chatPartner);
        DatabaseService.saveDirectMessage(msg);

        NotificationService.register(chatPartner);
        NotificationService.onPrivateMessage(chatPartner);
    }

    /**
     * Called when system chat message is received. Adds or removes user in model
     *
     * @param jsonStructure
     */
    private static void onSystemMessage(final JsonStructure jsonStructure) {
        log.debug("Received system message with content: {}", jsonStructure.toString());

        final JsonObject jsonObject = jsonStructure.asJsonObject();

        final String action = jsonObject.getString("action");
        final JsonObject data = jsonObject.getJsonObject("data");

        if (Objects.nonNull(data)) {
            final String userId = data.getString("id");
            final String userName = data.getString("name");
            switch (action) {
                case "userJoined":
                    editor.getOrCreateOtherUser(userId, userName);
                    if (editor.isChatPartnerOfCurrentUser(userId)) {
                        editor.getOrCreateChatPartnerOfCurrentUser(userId, userName).setStatus(true);
                    }
                    break;
                case "userLeft":
                    editor.removeOtherUserById(userId);
                    if (editor.isChatPartnerOfCurrentUser(userId)) {
                        editor.getOrCreateChatPartnerOfCurrentUser(userId, userName).setStatus(false);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Sends message in text channel of server.
     *
     * @param serverId
     * @param channelId
     * @param message
     */
    public static void sendServerMessage(String serverId, String channelId, String message) {
        JsonObject msgObject = Json.createObjectBuilder()
            .add("channel", channelId)
            .add("message", message)
            .build();

        try {
            String endpointKey = Constants.WS_USER_PATH + currentUser.getName() + Constants.WS_SERVER_CHAT_PATH + serverId;
            pathWebSocketClientHashMap.get(endpointKey).sendMessage(msgObject.toString());
            log.debug("Message sent: {}", msgObject);
        } catch (IOException e) {
            log.error("WebSocketService: sendServerMessage failed", e);
        }
    }

    /**
     * Called when server text channel message is received.
     * Creates ServerMessage Object and saves it in the model (messages list of channel) if it isn't sent by
     * currentUser (then, it would already be in the model).
     *
     * @param jsonStructure
     * @param serverId
     */
    private static void onServerChatMessage(JsonStructure jsonStructure, String serverId) {
        log.debug("received server chat message: {}", jsonStructure.toString());

        final JSONObject jsonObject = new JSONObject(jsonStructure.asJsonObject().toString());

        final String messageId = jsonObject.getString("id");
        final String channelId = jsonObject.getString("channel");
        final long timestamp = jsonObject.getLong("timestamp");
        final String from = jsonObject.getString("from");
        final String msgText = jsonObject.getString("text");

        // in case it's sent by you
        if (from.equals(currentUser.getName())) {
            // could save the messageId (not already in editor), but would have to get the message saved in editor for this
            return;
        }

        User sender = editor.getOtherUser(from);
        if (Objects.isNull(sender)) {
            log.error("WebSocketService: Sender \"{}\" of received message is not in editor", from);
            return;
        }

        ServerMessage msg = new ServerMessage();
        msg.setMessage(msgText).setTimestamp(timestamp).setSender(sender).setId(messageId);

        // setChannel triggers PropertyChangeListener that shows Message in Chat
        Server server = editor.getServer(serverId);
        Channel channel = editor.getChannel(channelId, server);
        if (Objects.isNull(channel)) {
            channel = new Channel().setServer(server).setId(channelId);
            NotificationService.register(channel);
        }
        msg.setChannel(channel);
        NotificationService.onChannelMessage(channel);
    }

    /**
     * Called when server system message is received. Reacts to following actions_:
     * - userJoined/userLeft: sets user online/offline in model
     * - serverUpdated:
     *
     * @param jsonStructure
     */
    private static void onServerSystemMessage(JsonStructure jsonStructure, String serverId) {
        log.debug("received server system message: {}", jsonStructure.toString());

        JsonObject jsonObject = jsonStructure.asJsonObject();

        String action = jsonObject.getString("action");
        JsonObject data = jsonObject.getJsonObject("data");

        if (Objects.nonNull(data)) {

            switch (action) {
                case "userJoined":
                    String userId = data.getString("id");
                    String userName = data.getString("name");
                    editor.setServerMemberOnline(userId, userName, editor.getServer(serverId));
                    return;
                case "userLeft":
                    userId = data.getString("id");
                    userName = data.getString("name");
                    editor.setServerMemberOffline(userId, userName, editor.getServer(serverId));
                    return;
                case "serverUpdated":
                    serverId = data.getString("id");
                    String newName = data.getString("name");
                    editor.getServer(serverId).setName(newName);
                    return;
                case "categoryCreated":
                    String categoryId = data.getString("id");
                    String name = data.getString("name");
                    serverId = data.getString("server");
                    if (Objects.isNull(editor.getCategory(categoryId, editor.getServer(serverId)))) {
                        editor.getOrCreateCategory(categoryId, name, editor.getServer(serverId));
                    }
                    return;
                case "channelCreated":
                    String channelId = data.getString("id");
                    String channelName = data.getString("name");
                    String type = data.getString("type");
                    boolean privileged = data.getBoolean("privileged");
                    categoryId = data.getString("category");
                    JsonArray jsonArray = data.getJsonArray("members");

                    Channel channel = new Channel().setId(channelId).setName(channelName).setType(type).setPrivileged(privileged);
                    Server modifiedServer = null;

                    for (Server server : editor.getAvailableServers()) {
                        for (Category category : server.getCategories()) {
                            if (category.getId().equals(categoryId)) {
                                category.withChannels(channel);
                                modifiedServer = server;
                                channel.setServer(server);
                            }
                        }
                    }

                    if (privileged && Objects.nonNull(modifiedServer)) {
                        ArrayList<String> members = new ArrayList<>();
                        for (int i = 0; i < jsonArray.size(); i++) {
                            members.add(jsonArray.getString(i));
                        }
                        for (User user : modifiedServer.getUsers()) {
                            if (members.contains(user.getId())) {
                                channel.withChannelMembers(user);
                            }
                        }
                    }
                    return;
                case "inviteExpired":
                    String invId = data.getString("id");
                    String servId = data.getString("server");
                    Server server = editor.getServer(servId);
                    for (ServerInvitation serverInvitation : server.getInvitations()) {
                        if (serverInvitation.getId().equals(invId)) {
                            server.withoutInvitations(serverInvitation);
                            break;
                        }
                    }
                    return;
                case "categoryUpdated":
                    String catId = data.getString("id");
                    String catName = data.getString("name");
                    Server serv = editor.getServer(data.getString("server"));
                    editor.getOrCreateCategory(catId, catName, serv).setName(catName);
                    return;
                case "channelUpdated":
                    String chId = data.getString("id");
                    String chName = data.getString("name");
                    String chType = data.getString("type");
                    Boolean priv = data.getBoolean("privileged");
                    JsonArray jsonMembers = data.getJsonArray("members");

                    Channel ch = editor.getChannelById(chId);
                    ch.setName(chName);
                    ch.setType(chType);
                    ch.setPrivileged(priv);
                    ch.withoutChannelMembers(ch.getChannelMembers());

                    if (priv && Objects.nonNull(ch)) {
                        ArrayList<String> membersList = new ArrayList<>();
                        for (int i = 0; i < jsonMembers.size(); i++) {
                            membersList.add(jsonMembers.getString(i));
                        }
                        for (User user : ch.getServer().getUsers()) {
                            if (membersList.contains(user.getId())) {
                                ch.withChannelMembers(user);
                            }
                        }
                    }
                    return;
                case "serverDeleted":
                    serverId = data.getString("id");
                    editor.removeServer(serverId);
                    return;
                case "channelDeleted":
                    channelId = data.getString("id");
                    categoryId = data.getString("category");
                    editor.deleteChannel(channelId);
                    return;
                case "categoryDeleted":
                    categoryId = data.getString("id");
                    serverId = data.getString("server");
                    editor.deleteCategory(serverId, categoryId);
                    return;
                default:
                    break;
            }
        }
        log.error("WebSocketService: can't process server system message with content: {}", jsonObject);
    }
}
