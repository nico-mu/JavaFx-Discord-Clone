package de.uniks.stp.network.websocket;

public interface WebSocketClientFactory {
    WebSocketClient create(String endpoint, WSCallback callback);
}
