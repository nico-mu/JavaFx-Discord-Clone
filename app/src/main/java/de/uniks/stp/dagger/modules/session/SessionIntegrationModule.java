package de.uniks.stp.dagger.modules.session;

import dagger.Module;
import dagger.Provides;
import de.uniks.stp.dagger.scope.SessionScope;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.model.User;
import de.uniks.stp.network.integration.IntegrationService;
import de.uniks.stp.network.integration.api.SpotifyApiClient;
import de.uniks.stp.network.integration.authorization.SpotifyAuthorizationClient;
import de.uniks.stp.network.rest.SessionRestClient;

import javax.inject.Named;
import javax.inject.Provider;

@Module
public class SessionIntegrationModule {
    @Provides
    @SessionScope
    static IntegrationService provideIntegrationService(SpotifyApiClient spotifyApiClient,
                                                        SessionDatabaseService databaseService,
                                                        SessionRestClient restClient,
                                                        @Named("currentUser") User currentUser,
                                                        Provider<SpotifyAuthorizationClient> spotifyAuthorizationClientProvider) {
        return new IntegrationService(spotifyApiClient,currentUser, restClient, databaseService, spotifyAuthorizationClientProvider);
    }
}
