package de.uniks.stp.network.websocket;

import javax.json.JsonStructure;

public interface WSCallback {
    void handleMessage(JsonStructure msg);
}
