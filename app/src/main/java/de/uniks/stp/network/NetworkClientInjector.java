package de.uniks.stp.network;

import de.uniks.stp.model.Channel;
import de.uniks.stp.model.User;

import java.util.Objects;

public class NetworkClientInjector {

    private static RestClient restClient = null;
    private static WebSocketClient webSocketClient = null;
    private static VoiceChatClient voiceChatClient = null;

    public static void setRestClient(RestClient newClient) {
        restClient = newClient;
    }

    public static RestClient getRestClient() {
        if(Objects.isNull(restClient)) {
            restClient = new RestClient();
        }
        return restClient;
    }

    public static WebSocketClient getWebSocketClient(String endpoint, WSCallback callback) {
        if(Objects.isNull(webSocketClient)) {
            return new WebSocketClient(endpoint, callback);
        }
        webSocketClient.inject(endpoint, callback);
        return webSocketClient;
    }

    public static void setWebSocketClient(WebSocketClient newWebSocketClient) {
        webSocketClient = newWebSocketClient;
    }

    public static VoiceChatClient getVoiceChatClient(User currentUser, Channel channel) {
        if(Objects.isNull(voiceChatClient)) {
            return new VoiceChatClient(currentUser, channel);
        }
        voiceChatClient.inject();
        return voiceChatClient;
    }

    public static void setVoiceChatClient(VoiceChatClient newVoiceChatClient) {
        voiceChatClient = newVoiceChatClient;
    }
}
