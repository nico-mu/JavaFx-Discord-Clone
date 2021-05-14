package de.uniks.stp.network;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.model.DirectMessage;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.User;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

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

    public static void init() {
        currentUser = editor.getOrCreateAccord().getCurrentUser();

        final WebSocketClient systemWebSocketClient = new WebSocketClient(Constants.WS_SYSTEM_PATH, WebSocketService::onSystemMessage);
        pathWebSocketClientHashMap.put(Constants.WS_SYSTEM_PATH, systemWebSocketClient);


        String endpoint = Constants.WS_USER_PATH + currentUser.getName();
        final WebSocketClient privateWebSocketClient = new WebSocketClient(endpoint, WebSocketService::onPrivateMessage);
        pathWebSocketClientHashMap.put(endpoint, privateWebSocketClient);
    }

    public static void addServerWebSocket(String serverId) {
        String endpoint = Constants.WS_SYSTEM_PATH + Constants.WS_SERVER_SYSTEM_PATH + serverId;
        final WebSocketClient systemServerWSC = new WebSocketClient(endpoint, WebSocketService::onServerSystemMessage);
        pathWebSocketClientHashMap.put(endpoint, systemServerWSC);

        endpoint = Constants.WS_USER_PATH + currentUser.getName() + Constants.WS_SERVER_CHAT_PATH + serverId;
        final WebSocketClient chatServerWSC = new WebSocketClient(endpoint, WebSocketService::onServerChatMessage);
        pathWebSocketClientHashMap.put(endpoint, chatServerWSC);
    }

    private static void onServerSystemMessage(JsonStructure jsonStructure) {
        log.debug("server system message: {}", jsonStructure.toString());

        //TODO...
    }

    private static void onServerChatMessage(JsonStructure jsonStructure) {
        log.debug("server chat message: {}", jsonStructure.toString());

        //TODO...
    }

    public static void sendServerMessage(String serverId, String channel, String message) {
        JsonObject msgObject = Json.createObjectBuilder()
            .add("channel", channel)
            .add("message", message)
            .build();

        try {
            String endpointKey = Constants.WS_USER_PATH + currentUser.getName() + Constants.WS_SERVER_CHAT_PATH + serverId;
            pathWebSocketClientHashMap.get(endpointKey).sendMessage(msgObject.toString());
            log.debug("Message sent: {}", msgObject.toString());
        } catch (IOException e) {
            log.error("WebSocketService: sendServerMessage failed", e);
        }
    }

    public static void sendPrivateMessage(String receiver, String message) {
        JsonObject msgObject = Json.createObjectBuilder()
            .add("channel", "private")
            .add("to", receiver)
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

        DirectMessage msg = new DirectMessage().setReceiver(currentUser);
        msg.setMessage(msgText).setTimestamp(timestamp).setSender(sender);

        // show message
        sender.withPrivateChatMessages(msg);
        if(!currentUser.getChatPartner().contains(sender)){
            currentUser.withChatPartner(sender);
        }
    }

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
                    break;
                case "userLeft":
                    editor.removeOtherUserById(userId);
                    break;
                default:
                    break;
            }
        }
    }
}
