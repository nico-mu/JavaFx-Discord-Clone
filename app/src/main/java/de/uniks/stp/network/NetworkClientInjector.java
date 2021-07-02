package de.uniks.stp.network;

import java.util.Objects;

public class NetworkClientInjector {

    private static RestClient restClient = null;
    private static WebSocketClient webSocketClient = null;
    private static MediaRequestClient mediaRequestClient = null;

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

    public static void setMediaRequestClient(MediaRequestClient newMediaRequestClient) {
        mediaRequestClient = newMediaRequestClient;
    }

    public static MediaRequestClient getMediaRequestClient() {
        if(Objects.isNull(mediaRequestClient)) {
            mediaRequestClient = new MediaRequestClient();
        }
        return mediaRequestClient;
    }

    public static void setWebSocketClient(WebSocketClient newWebSocketClient) {
        webSocketClient = newWebSocketClient;
    }
}
