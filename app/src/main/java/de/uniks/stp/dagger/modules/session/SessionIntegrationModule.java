package de.uniks.stp.dagger.modules.session;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import de.uniks.stp.dagger.scope.SessionScope;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.model.User;
import de.uniks.stp.network.integration.IntegrationService;
import de.uniks.stp.network.integration.api.GitHubApiClient;
import de.uniks.stp.network.integration.api.SpotifyApiClient;
import de.uniks.stp.network.integration.authorization.GitHubAuthorizationClient;
import de.uniks.stp.network.integration.authorization.SpotifyAuthorizationClient;
import de.uniks.stp.network.rest.SessionRestClient;

import javax.inject.Named;
import javax.inject.Provider;

@Module
public class SessionIntegrationModule {
    @Provides
    @SessionScope
    static IntegrationService provideIntegrationService(Provider<SpotifyApiClient> spotifyApiClientProvider,
                                                        Provider<GitHubApiClient> gitHubApiClientProvider,
                                                        SessionDatabaseService databaseService,
                                                        SessionRestClient restClient,
                                                        @Named("currentUser") User currentUser,
                                                        Provider<GitHubAuthorizationClient> gitHubAuthorizationClientProvider,
                                                        Provider<SpotifyAuthorizationClient> spotifyAuthorizationClientProvider) {
        return new IntegrationService(spotifyApiClientProvider, gitHubApiClientProvider, currentUser, restClient, databaseService, gitHubAuthorizationClientProvider, spotifyAuthorizationClientProvider);
    }
}
