package de.uniks.stp.network.integration;

import dagger.Lazy;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.model.User;
import de.uniks.stp.network.integration.api.GitHubApiClient;
import de.uniks.stp.network.integration.api.SpotifyApiClient;
import de.uniks.stp.network.integration.authorization.GitHubAuthorizationClient;
import de.uniks.stp.network.integration.authorization.SpotifyAuthorizationClient;
import de.uniks.stp.network.rest.SessionRestClient;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.inject.Provider;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class IntegrationServiceTest {

    @Mock
    private Provider<SpotifyAuthorizationClient> spotifyAuthorizationClientProviderMock;

    @Mock
    private Provider<GitHubAuthorizationClient> gitHubAuthorizationClientProviderMock;

    @Mock
    private SpotifyAuthorizationClient spotifyAuthorizationClientMock;

    @Mock
    private GitHubAuthorizationClient gitHubAuthorizationClientMock;

    @Mock
    private Lazy<SpotifyApiClient> spotifyApiClientLazyMock;

    @Mock
    private Lazy<GitHubApiClient> gitHubApiClientLazyMock;

    @Mock
    private SpotifyApiClient spotifyApiClient;

    @Mock
    private GitHubApiClient gitHubApiClient;


    @Mock
    private SessionRestClient restClientMock;

    private SessionDatabaseService databaseServiceSpy;

    private IntegrationService integrationServiceSpy;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        User currentUser = new User().setId("123").setName("test");
        databaseServiceSpy = Mockito.spy(new SessionDatabaseService(currentUser));
        integrationServiceSpy = Mockito.spy(
            new IntegrationService(spotifyApiClientLazyMock,
                gitHubApiClientLazyMock,
            currentUser,
                restClientMock,
                databaseServiceSpy,
                gitHubAuthorizationClientProviderMock,
                spotifyAuthorizationClientProviderMock
            )
        );

        when(spotifyApiClientLazyMock.get()).thenReturn(spotifyApiClient);
        when(gitHubApiClientLazyMock.get()).thenReturn(gitHubApiClient);
    }

    @Test
    public void initServiceTest() {
        databaseServiceSpy.addApiIntegrationSetting(Integrations.SPOTIFY.key, "123");
        databaseServiceSpy.setIntegrationMode(Integrations.SPOTIFY.key, true);

        integrationServiceSpy.init();

        verify(spotifyApiClient).refresh();
        verify(gitHubApiClient, never()).refresh();
    }

    @Test
    public void restartServiceTest() {
        databaseServiceSpy.addApiIntegrationSetting(Integrations.SPOTIFY.key, "123");
        integrationServiceSpy.restartService(Integrations.SPOTIFY.key);
        verify(spotifyApiClient).stop();
        verify(spotifyApiClient).refresh();
        Assertions.assertTrue(integrationServiceSpy.isServiceActive(Integrations.SPOTIFY.key));
    }

    @Test
    public void startServiceTest() {
        databaseServiceSpy.addApiIntegrationSetting(Integrations.SPOTIFY.key, "123");
        Credentials credentials = new Credentials();
        integrationServiceSpy.startService(Integrations.SPOTIFY.key, credentials);
        verify(spotifyApiClient).stop();
        verify(spotifyApiClient).start(eq(credentials));
        Assertions.assertTrue(integrationServiceSpy.isServiceActive(Integrations.SPOTIFY.key));
    }

    @Test
    public void stopServiceTest() {
        databaseServiceSpy.addApiIntegrationSetting(Integrations.SPOTIFY.key, "123");
        integrationServiceSpy.stopService(Integrations.SPOTIFY.key);
        verify(spotifyApiClient).stop();
        verify(restClientMock).updateDescriptionAsync(eq("123"), eq(" "), any());
        Assertions.assertFalse(integrationServiceSpy.isServiceActive(Integrations.SPOTIFY.key));
    }

    @Test
    public void removeServiceTest() {
        databaseServiceSpy.addApiIntegrationSetting(Integrations.GITHUB.key, "123");
        databaseServiceSpy.setIntegrationMode(Integrations.GITHUB.key, true);

        integrationServiceSpy.removeService(Integrations.GITHUB.key);
        verify(gitHubApiClient).shutdown();
        verify(restClientMock).updateDescriptionAsync(eq("123"), eq(" "), any());
        Assertions.assertNull(databaseServiceSpy.getApiIntegrationSetting(Integrations.GITHUB.key));
    }

    @Test
    public void getAuthorizationClientTest() {
        when(gitHubAuthorizationClientProviderMock.get()).thenReturn(gitHubAuthorizationClientMock);
        when(spotifyAuthorizationClientProviderMock.get()).thenReturn(spotifyAuthorizationClientMock);
        when(gitHubAuthorizationClientMock.isServerStopped()).thenReturn(false);

        integrationServiceSpy.getAuthorizationClient(Integrations.GITHUB.key);
        integrationServiceSpy.getAuthorizationClient(Integrations.SPOTIFY.key);
        verify(gitHubAuthorizationClientMock).stopServer();
    }

    @AfterEach
    public void tearDown() {
        databaseServiceSpy.deleteApiIntegrationSetting(Integrations.SPOTIFY.key);
        databaseServiceSpy.deleteApiIntegrationSetting(Integrations.GITHUB.key);
        spotifyAuthorizationClientProviderMock = null;
        gitHubAuthorizationClientProviderMock = null;
        spotifyApiClientLazyMock = null;
        gitHubApiClientLazyMock = null;
        spotifyApiClient = null;
        gitHubApiClient = null;
        restClientMock = null;
        databaseServiceSpy = null;
        integrationServiceSpy = null;
    }
}
