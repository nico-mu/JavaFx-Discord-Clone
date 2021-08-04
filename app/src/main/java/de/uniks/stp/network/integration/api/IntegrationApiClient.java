package de.uniks.stp.network.integration.api;

import de.uniks.stp.network.integration.Credentials;
import de.uniks.stp.network.integration.authorization.AuthorizationCallback;

public interface IntegrationApiClient {

    void start(Credentials credentials);
    void refresh();
    void stop();
    void shutdown();
}
