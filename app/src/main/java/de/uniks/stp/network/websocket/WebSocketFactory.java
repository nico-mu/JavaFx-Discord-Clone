package de.uniks.stp.network.websocket;

import javax.inject.Inject;
import javax.inject.Named;

public class WebSocketFactory implements WebSocketClientFactory {

    private final String userKey;

    @Inject
    public WebSocketFactory(@Named("userKey") String userKey) {
        this.userKey = userKey;
    }

    @Override
    public WebSocketClient create(String endpoint, WSCallback callback) {
        return new WebSocketClient(userKey, endpoint, callback);
    }
}
