package de.uniks.stp.network.integration;
import dagger.Lazy;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.model.User;
import de.uniks.stp.network.integration.api.GitHubApiClient;
import de.uniks.stp.network.integration.api.SpotifyApiClient;
import de.uniks.stp.network.integration.authorization.AbstractAuthorizationClient;
import de.uniks.stp.network.integration.authorization.GitHubAuthorizationClient;
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

    private final SessionDatabaseService databaseService;
    private final Provider<SpotifyAuthorizationClient> spotifyAuthorizationClientProvider;
    private final Provider<GitHubAuthorizationClient> gitHubAuthorizationClientProvider;
    private final Lazy<SpotifyApiClient> spotifyApiClientLazy;
    private final Lazy<GitHubApiClient> gitHubApiClientLazy;
    private final User currentUser;
    private final SessionRestClient restClient;

    public IntegrationService(Lazy<SpotifyApiClient> spotifyApiClientLazy,
                              Lazy<GitHubApiClient> gitHubApiClientLazy,
                              @Named("currentUser") User currentUser,
                              SessionRestClient restClient,
                              SessionDatabaseService databaseService,
                              Provider<GitHubAuthorizationClient> gitHubAuthorizationClientProvider,
                              Provider<SpotifyAuthorizationClient> spotifyAuthorizationClientProvider) {
        this.spotifyApiClientLazy = spotifyApiClientLazy;
        this.gitHubApiClientLazy = gitHubApiClientLazy;
        this.databaseService = databaseService;
        this.spotifyAuthorizationClientProvider = spotifyAuthorizationClientProvider;
        this.gitHubAuthorizationClientProvider = gitHubAuthorizationClientProvider;
        this.currentUser = currentUser;
        this.restClient = restClient;
    }

    public void init() {
        if(isServiceConnected(Integrations.SPOTIFY.key)) {
            spotifyApiClientLazy.get().refresh();
        }
        else if(isServiceConnected(Integrations.GITHUB.key)){
            gitHubApiClientLazy.get().refresh();
        }
    }

    public void startService(String serviceName, Credentials credentials) {
        if(serviceName.equals(Integrations.SPOTIFY.key)) {
            spotifyApiClientLazy.get().stop();
            spotifyApiClientLazy.get().start(credentials);
        }
        else if(serviceName.equals(Integrations.GITHUB.key)) {
            gitHubApiClientLazy.get().stop();
            gitHubApiClientLazy.get().start(credentials);
        }
        else {
            log.warn("Unknown service integration {}", serviceName);
        }
    }

    public AbstractAuthorizationClient getAuthorizationClient(String serviceName) {
        if(serviceName.equals(Integrations.SPOTIFY.key)) {
           return spotifyAuthorizationClientProvider.get();
        }
        else if(serviceName.equals(Integrations.GITHUB.key)) {
            return gitHubAuthorizationClientProvider.get();
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
        spotifyApiClientLazy.get().shutdown();
        gitHubApiClientLazy.get().shutdown();
        currentUserDescriptionCallback(restClient.updateDescription(currentUser.getId(), " "));
    }
}
