package de.uniks.stp.network.integration;
import dagger.Lazy;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.network.integration.api.SpotifyApiClient;
import de.uniks.stp.network.integration.authorization.AbstractAuthorizationClient;
import de.uniks.stp.network.integration.authorization.SpotifyAuthorizationClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import java.util.Objects;


public class IntegrationService {

    private static final Logger log = LoggerFactory.getLogger(IntegrationService.class);
    private final SpotifyApiClient spotifyApiClient;
    private final SessionDatabaseService databaseService;
    private final Provider<SpotifyAuthorizationClient> spotifyAuthorizationClientProvider;

    public IntegrationService(SpotifyApiClient spotifyApiClient,
                              SessionDatabaseService databaseService,
                              Provider<SpotifyAuthorizationClient> spotifyAuthorizationClientProvider) {
        this.spotifyApiClient = spotifyApiClient;
        this.databaseService = databaseService;
        this.spotifyAuthorizationClientProvider = spotifyAuthorizationClientProvider;
    }

    public void init() {
        spotifyApiClient.refresh();
    }

    public void startService(String serviceName, Credentials credentials) {
        if(serviceName.equals(Integrations.SPOTIFY.key)) {
            spotifyApiClient.stop();
            spotifyApiClient.start(credentials);
        }
        else {
            log.debug("Unknown service integration {}", serviceName);
        }
    }

    public AbstractAuthorizationClient getAuthorizationClient(String serviceName) {
        if(serviceName.equals(Integrations.SPOTIFY.key)) {
           return spotifyAuthorizationClientProvider.get();
        }
        else {
            log.debug("Unknown service integration {}", serviceName);
            return null;
        }
    }

    public boolean isServiceConnected(String serviceName) {
        return Objects.nonNull(databaseService.getApiIntegrationSetting(serviceName));
    }

    public void stop() {
        spotifyApiClient.stop();
    }
}
