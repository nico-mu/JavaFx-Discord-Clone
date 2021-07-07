package de.uniks.stp.network.websocket;

import javax.websocket.ClientEndpointConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomWebSocketConfigurator extends ClientEndpointConfig.Configurator {
    private final String userKey;

    public CustomWebSocketConfigurator(String userKey) {
        this.userKey = userKey;
    }

    /**
     * Is called automatically; adds necessary userKey to Header
     *
     * @param headers passed automatically
     */
    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        super.beforeRequest(headers);
        ArrayList<String> key = new ArrayList<>();
        key.add(this.userKey);
        headers.put("userKey", key);
    }
}
