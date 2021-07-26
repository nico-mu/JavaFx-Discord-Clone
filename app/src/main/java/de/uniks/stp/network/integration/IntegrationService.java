package de.uniks.stp.network.integration;
import de.uniks.stp.network.integration.api.SpotifyApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IntegrationService {

    private static final Logger log = LoggerFactory.getLogger(IntegrationService.class);
    private final SpotifyApiClient spotifyApiClient;

    public IntegrationService(SpotifyApiClient spotifyApiClient) {
        this.spotifyApiClient = spotifyApiClient;
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

    public void stop() {
        spotifyApiClient.stop();
    }
}
