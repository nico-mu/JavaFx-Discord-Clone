//FIXME: Check if "userKey" is the right key to establish Connection

package de.uniks.stp.network;

import javax.websocket.ClientEndpointConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomWebSocketConfigurator extends ClientEndpointConfig.Configurator {
    private final String userKey;

    public CustomWebSocketConfigurator(String userKey) {
        this.userKey = userKey;
    }

    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        super.beforeRequest(headers);
        ArrayList<String> key = new ArrayList<>();
        key.add(this.userKey);
        headers.put("userKey", key);  // userKey not sure, might also be "name", ...
    }
}
