package de.uniks.stp.network;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.model.DirectMessage;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.User;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import kong.unirest.json.JSONObject;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class WebSocketService {
    private static final HashMap<String, WebSocketClient> pathWebSocketClientHashMap = new HashMap<>();
    private static Editor editor;

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
        final WebSocketClient systemWebSocketClient = new WebSocketClient(Constants.WS_SYSTEM_PATH, WebSocketService::onSystemMessage);
        pathWebSocketClientHashMap.put(Constants.WS_SYSTEM_PATH, systemWebSocketClient);


        String endpoint = Constants.WS_USER_PATH + editor.getCurrentUser().getName();
        final WebSocketClient privateWebSocketClient = new WebSocketClient(endpoint, WebSocketService::onPrivateMessage);
        pathWebSocketClientHashMap.put(endpoint, privateWebSocketClient);
    }

    public static void addServerWebSocket(String serverId) {
        String endpoint = Constants.WS_SYSTEM_PATH + Constants.WS_SERVER_SYSTEM_PATH + serverId;
        final WebSocketClient systemServerWSC = new WebSocketClient(endpoint, WebSocketService::onServerSystemMessage);
        pathWebSocketClientHashMap.put(endpoint, systemServerWSC);

        endpoint = Constants.WS_USER_PATH + editor.getCurrentUser().getName() + Constants.WS_SERVER_CHAT_PATH + serverId;
        final WebSocketClient chatServerWSC = new WebSocketClient(endpoint, WebSocketService::onServerChatMessage);
        pathWebSocketClientHashMap.put(endpoint, chatServerWSC);
    }

    private static void onServerSystemMessage(JsonStructure jsonStructure) {
        System.out.println("server system message: " + jsonStructure.toString());

        //TODO...
    }

    private static void onServerChatMessage(JsonStructure jsonStructure) {
        System.out.println("server chat message: " + jsonStructure.toString());

        //TODO...
    }

    public static void sendServerMessage(String serverId, String channel, String message) {
        JsonObject msgObject = Json.createObjectBuilder()
            .add("channel", channel)
            .add("message", message)
            .build();

        try {
            String endpointKey = Constants.WS_USER_PATH + editor.getCurrentUser().getName() + Constants.WS_SERVER_CHAT_PATH + serverId;
            pathWebSocketClientHashMap.get(endpointKey).sendMessage(msgObject.toString());
            System.out.println("Message sent: " + msgObject.toString());
        } catch (IOException e) {
            System.out.println("ERROR:WebSocketService: sendServerMessage failed");
            e.printStackTrace();
        }
    }

    public static void sendPrivateMessage(String receiver, String message) {
        JsonObject msgObject = Json.createObjectBuilder()
            .add("channel", "private")
            .add("to", receiver)
            .add("message", message)
            .build();

        try {
            String endpointKey = Constants.WS_USER_PATH + editor.getCurrentUser().getName();
            pathWebSocketClientHashMap.get(endpointKey).sendMessage(msgObject.toString());
            System.out.println("Message sent: " + msgObject.toString());
        } catch (IOException e) {
            System.out.println("ERROR:WebSocketService: sendPrivateMessage failed");
            e.printStackTrace();
        }
    }

    private static void onPrivateMessage(JsonStructure jsonStructure) {
        System.out.println("[" + new Date() + "] Received private message with content: " + jsonStructure.toString());

        final JSONObject jsonObject = new JSONObject(jsonStructure.asJsonObject().toString());

        final String from = jsonObject.getString("from");
        final long timestamp = jsonObject.getLong("timestamp");
        final String msgText = jsonObject.getString("message");
        final String to = jsonObject.getString("to");

        //if it's sent by you
        if (from.equals(editor.getCurrentUser().getName())) {
            return;
        }

        User sender = editor.getOtherUser(from);
        if (Objects.isNull(sender)) {
            System.out.println("ERROR:WebSocketService: Sender \"" + from + "\" of received message is not in editor");
            return;
        }
        User receiver = editor.getCurrentUser();

        DirectMessage msg = new DirectMessage().setReceiver(receiver);
        msg.setMessage(msgText).setTimestamp(timestamp).setSender(sender);

        //Namen in DirectMessages Liste hinzufügen
        sender.withReceivedMessages(msg);
    }

    private static void onSystemMessage(final JsonStructure jsonStructure) {
        System.out.println("[" + new Date() + "] Received system message with content: " + jsonStructure.toString());

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
