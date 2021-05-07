package de.uniks.stp.network;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;

import javax.json.JsonStructure;
import java.util.HashMap;

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
        System.out.println("Received system message with content: " + jsonStructure.toString());
    }
}
