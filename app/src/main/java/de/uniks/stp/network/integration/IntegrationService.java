package de.uniks.stp.network.integration;
import dagger.Lazy;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.model.User;
import de.uniks.stp.network.integration.api.SpotifyApiClient;
import de.uniks.stp.network.integration.authorization.AbstractAuthorizationClient;
import de.uniks.stp.network.integration.authorization.SpotifyAuthorizationClient;
import de.uniks.stp.network.rest.SessionRestClient;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Provider;
import java.util.Objects;


public class IntegrationService {

    private static final Logger log = LoggerFactory.getLogger(IntegrationService.class);
    private final SpotifyApiClient spotifyApiClient;
    private final SessionDatabaseService databaseService;
    private final Provider<SpotifyAuthorizationClient> spotifyAuthorizationClientProvider;
    private final User currentUser;
    private final SessionRestClient restClient;

    public IntegrationService(SpotifyApiClient spotifyApiClient,
                              @Named("currentUser") User currentUser,
                              SessionRestClient restClient,
                              SessionDatabaseService databaseService,
                              Provider<SpotifyAuthorizationClient> spotifyAuthorizationClientProvider) {
        this.spotifyApiClient = spotifyApiClient;
        this.databaseService = databaseService;
        this.spotifyAuthorizationClientProvider = spotifyAuthorizationClientProvider;
        this.currentUser = currentUser;
        this.restClient = restClient;
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
            log.warn("Unknown service integration {}", serviceName);
        }
    }

    public AbstractAuthorizationClient getAuthorizationClient(String serviceName) {
        if(serviceName.equals(Integrations.SPOTIFY.key)) {
           return spotifyAuthorizationClientProvider.get();
        }
        else {
            log.warn("Unknown service integration {}", serviceName);
            return null;
        }
    }

    public boolean isServiceConnected(String serviceName) {
        return Objects.nonNull(databaseService.getApiIntegrationSetting(serviceName));
    }

    private void currentUserDescriptionCallback(HttpResponse<JsonNode> response) {
        if(response.isSuccess()) {
            log.debug("description for user {} changed successfully", currentUser.getName());
        }
        else {
            log.warn("Could not change description of user {}", currentUser.getName());
        }
    }

    public void stop() {
        spotifyApiClient.shutdown();
        currentUserDescriptionCallback(restClient.updateDescription(currentUser.getId(), " "));
    }
}
