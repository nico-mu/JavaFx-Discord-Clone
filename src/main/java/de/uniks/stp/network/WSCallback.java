package de.uniks.stp.network;

import javax.json.JsonStructure;

public interface WSCallback {
    void handleMessage(JsonStructure msg);
}
