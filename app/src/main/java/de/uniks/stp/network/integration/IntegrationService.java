package de.uniks.stp.network.integration;
import dagger.Lazy;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.model.User;
import de.uniks.stp.network.integration.api.GitHubApiClient;
import de.uniks.stp.network.integration.api.IntegrationApiClient;
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
    private AbstractAuthorizationClient authorizationClient;

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
        if(isServiceConnected(Integrations.SPOTIFY.key) && isServiceActive(Integrations.SPOTIFY.key)) {
            spotifyApiClientLazy.get().refresh();
        }
        else if(isServiceConnected(Integrations.GITHUB.key) && isServiceActive(Integrations.GITHUB.key)){
            gitHubApiClientLazy.get().refresh();
        }
    }

    public void restartService(String serviceName) {
        IntegrationApiClient apiClient = getServiceByName(serviceName);

        if(Objects.nonNull(apiClient)) {
            apiClient.stop();
            apiClient.refresh();
            databaseService.setIntegrationMode(serviceName, true);
        }
    }

    public void startService(String serviceName, Credentials credentials) {
        IntegrationApiClient apiClient = getServiceByName(serviceName);

        if(Objects.nonNull(apiClient)) {
            apiClient.stop();
            apiClient.start(credentials);
            databaseService.setIntegrationMode(serviceName, true);
        }
    }

    public void stopService(String serviceName) {
        IntegrationApiClient apiClient = getServiceByName(serviceName);

        if(Objects.nonNull(apiClient)) {
            apiClient.stop();
            currentUser.setDescription(" ");
            restClient.updateDescriptionAsync(currentUser.getId(), " ", this::currentUserDescriptionCallback);
            databaseService.setIntegrationMode(serviceName, false);
        }
    }

    public void removeService(String serviceName) {
        IntegrationApiClient apiClient = getServiceByName(serviceName);

        if(Objects.nonNull(apiClient)) {
            apiClient.shutdown();

            if(isServiceActive(serviceName)) {
                currentUser.setDescription(" ");
                restClient.updateDescriptionAsync(currentUser.getId(), " ", this::currentUserDescriptionCallback);
            }
            databaseService.deleteApiIntegrationSetting(serviceName);
        }
    }

    private IntegrationApiClient getServiceByName(String serviceName) {
        if(serviceName.equals(Integrations.SPOTIFY.key)) {
            return spotifyApiClientLazy.get();
        }
        else if(serviceName.equals(Integrations.GITHUB.key)) {
           return gitHubApiClientLazy.get();
        }

        log.warn("Unknown service integration {}", serviceName);
        return null;
    }

    public AbstractAuthorizationClient getAuthorizationClient(String serviceName) {
        stopAuthorizationClientServer();

        if(serviceName.equals(Integrations.SPOTIFY.key)) {
           return authorizationClient = spotifyAuthorizationClientProvider.get();
        }
        else if(serviceName.equals(Integrations.GITHUB.key)) {
            return authorizationClient = gitHubAuthorizationClientProvider.get();
        }
        else {
            log.warn("Unknown service integration {}", serviceName);
            return null;
        }
    }

    private void stopAuthorizationClientServer() {
        if(Objects.nonNull(authorizationClient) && !authorizationClient.isServerStopped()) {
            authorizationClient.stopServer();
        }
    }

    public boolean isServiceConnected(String serviceName) {
        return Objects.nonNull(databaseService.getApiIntegrationSetting(serviceName));
    }

    public boolean isServiceActive(String serviceName) {
        return databaseService.isIntegrationActive(serviceName);
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
        stopAuthorizationClientServer();
        spotifyApiClientLazy.get().shutdown();
        gitHubApiClientLazy.get().shutdown();
        currentUserDescriptionCallback(restClient.updateDescriptionSync(currentUser.getId(), " "));
    }
}
