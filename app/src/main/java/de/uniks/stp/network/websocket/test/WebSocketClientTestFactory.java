package de.uniks.stp.network.websocket.test;

import de.uniks.stp.network.websocket.WSCallback;
import de.uniks.stp.network.websocket.WebSocketClient;
import de.uniks.stp.network.websocket.WebSocketClientFactory;
import org.mockito.Mockito;

public class WebSocketClientTestFactory implements WebSocketClientFactory {
    @Override
    public WebSocketClient create(String endpoint, WSCallback callback) {
        return Mockito.mock(WebSocketClient.class);
    }
}
