package de.uniks.stp.network.integration;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.network.integration.api.SpotifyApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;


public class IntegrationService {

    private static final Logger log = LoggerFactory.getLogger(IntegrationService.class);
    private final SpotifyApiClient spotifyApiClient;
    private final SessionDatabaseService databaseService;

    public IntegrationService(SpotifyApiClient spotifyApiClient, SessionDatabaseService databaseService) {
        this.spotifyApiClient = spotifyApiClient;
        this.databaseService = databaseService;
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

    public boolean isServiceConnected(String serviceName) {
        return Objects.nonNull(databaseService.getApiIntegrationSetting(serviceName));
    }

    public void stop() {
        spotifyApiClient.stop();
    }
}
