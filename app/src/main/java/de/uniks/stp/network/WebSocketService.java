package de.uniks.stp.network;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;

import javax.json.JsonObject;
import javax.json.JsonStructure;
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
    }

    public static void init() {
        final WebSocketClient systemWebSocketClient = new WebSocketClient(Constants.WS_SYSTEM_PATH, WebSocketService::onSystemMessage);
        pathWebSocketClientHashMap.put(Constants.WS_SYSTEM_PATH, systemWebSocketClient);
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
