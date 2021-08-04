package de.uniks.stp.dagger.modules.test;

import dagger.Module;
import dagger.Provides;
import de.uniks.stp.dagger.scope.SessionScope;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.network.integration.IntegrationService;
import de.uniks.stp.network.integration.api.SpotifyApiClient;
import de.uniks.stp.network.integration.authorization.SpotifyAuthorizationClient;
import org.mockito.Mockito;

import javax.inject.Provider;

@Module
public class SessionTestIntegrationModule {
    @Provides
    @SessionScope
    static IntegrationService provideIntegrationService(SpotifyApiClient spotifyApiClient,
                                                        SessionDatabaseService databaseService,
                                                        Provider<SpotifyAuthorizationClient> spotifyAuthorizationClientProvider) {
        return Mockito.mock(IntegrationService.class);
    }
}
