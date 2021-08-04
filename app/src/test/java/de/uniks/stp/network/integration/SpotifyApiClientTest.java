package de.uniks.stp.network.integration;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.exceptions.detailed.BadRequestException;
import com.wrapper.spotify.exceptions.detailed.UnauthorizedException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.requests.authorization.authorization_code.pkce.AuthorizationCodePKCERefreshRequest;
import com.wrapper.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.model.User;
import de.uniks.stp.network.integration.api.SpotifyApiClient;
import de.uniks.stp.network.rest.SessionRestClient;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.IOException;
import java.util.concurrent.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class SpotifyApiClientTest {

    @Mock
    public HttpResponse<JsonNode> httpResponseMock;

    @Mock
    private ExecutorService executorService;

    @Mock
    private ScheduledExecutorService scheduledExecutorService;

    @Mock
    private ScheduledFuture<?> scheduledFutureMock;

    @Captor
    private ArgumentCaptor<Runnable> runnableArgumentCaptor;

    @Captor
    private ArgumentCaptor<Runnable> scheduledRunnableArgumentCaptor;

    private SessionRestClient sessionRestClient;
    private SessionDatabaseService databaseService;
    private User currentUser;


    @InjectMocks
    private SpotifyApiClient spotifyApiClientSpy;

    @BeforeEach
    public void setUp() {
        ViewLoader viewLoader = Mockito.mock(ViewLoader.class);
        sessionRestClient = Mockito.mock(SessionRestClient.class);
        currentUser = Mockito.spy(new User().setName("test").setId("123"));
        databaseService =  Mockito.spy(new SessionDatabaseService(currentUser));
        spotifyApiClientSpy = Mockito.spy(new SpotifyApiClient(currentUser, viewLoader, sessionRestClient, databaseService));

        MockitoAnnotations.initMocks(this);
        when(httpResponseMock.isSuccess()).thenReturn(true);

        doReturn(scheduledFutureMock).when(scheduledExecutorService)
            .scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
    }

    @Test
    public void startSpotifyApiClientTestSuccessful() throws IOException, ParseException, SpotifyWebApiException {
        GetUsersCurrentlyPlayingTrackRequest currentlyPlayingTrackRequestMock = Mockito.mock(GetUsersCurrentlyPlayingTrackRequest.class);
        CurrentlyPlaying playing = Mockito.mock(CurrentlyPlaying.class);
        Credentials credentials = new Credentials();
        credentials.setAccessToken("access_token");
        credentials.setRefreshToken("refresh_token");

        doReturn(currentlyPlayingTrackRequestMock).when(spotifyApiClientSpy).getUsersCurrentlyPlayingTrackRequest();
        when(currentlyPlayingTrackRequestMock.execute()).thenReturn(playing);
        when(playing.getIs_playing()).thenReturn(true);
        when(sessionRestClient.updateDescription(eq("123"), anyString())).thenReturn(httpResponseMock);

        spotifyApiClientSpy.start(credentials);

        verify(scheduledExecutorService)
            .scheduleAtFixedRate(scheduledRunnableArgumentCaptor.capture(), eq(0L), anyLong(), eq(TimeUnit.MILLISECONDS));

        Runnable scheduledRunnable = scheduledRunnableArgumentCaptor.getValue();
        scheduledRunnable.run();

        verify(currentUser).setDescription(anyString());
        Assertions.assertTrue(currentUser.isSpotifyPlaying());
    }

    @Test
    public void startSpotifyApiClientTestRefresh() throws IOException, ParseException, SpotifyWebApiException {
        GetUsersCurrentlyPlayingTrackRequest currentlyPlayingTrackRequestMock = Mockito.mock(GetUsersCurrentlyPlayingTrackRequest.class);
        AuthorizationCodePKCERefreshRequest authorizationCodePKCERefreshRequestMock = Mockito.mock(AuthorizationCodePKCERefreshRequest.class);
        AuthorizationCodeCredentials authorizationCodeCredentialsMock = Mockito.mock(AuthorizationCodeCredentials.class);
        Credentials credentials = new Credentials();
        credentials.setAccessToken("access_token");
        credentials.setRefreshToken("refresh_token");

        doReturn(currentlyPlayingTrackRequestMock).when(spotifyApiClientSpy).getUsersCurrentlyPlayingTrackRequest();
        doReturn(authorizationCodePKCERefreshRequestMock).when(spotifyApiClientSpy).getAuthorizationCodePKCERefreshRequest();
        when(currentlyPlayingTrackRequestMock.execute()).thenThrow(new UnauthorizedException("The access token expired"));
        when(authorizationCodePKCERefreshRequestMock.execute()).thenReturn(authorizationCodeCredentialsMock);
        when(authorizationCodeCredentialsMock.getAccessToken()).thenReturn("access_token");
        when(authorizationCodeCredentialsMock.getRefreshToken()).thenReturn("refresh_token");

        spotifyApiClientSpy.start(credentials);
        verify(scheduledExecutorService)
            .scheduleAtFixedRate(scheduledRunnableArgumentCaptor.capture(), eq(0L), anyLong(), eq(TimeUnit.MILLISECONDS));

        Runnable scheduledRunnable = scheduledRunnableArgumentCaptor.getValue();
        scheduledRunnable.run();

        verify(spotifyApiClientSpy).refresh();
    }

    @Test
    public void refreshSpotifyApiClientTestSuccess() throws IOException, ParseException, SpotifyWebApiException {
        AuthorizationCodePKCERefreshRequest authorizationCodePKCERefreshRequestMock = Mockito.mock(AuthorizationCodePKCERefreshRequest.class);
        AuthorizationCodeCredentials authorizationCodeCredentialsMock = Mockito.mock(AuthorizationCodeCredentials.class);
        Credentials credentials = new Credentials();
        credentials.setAccessToken("access_token");
        credentials.setRefreshToken("refresh_token");

        doReturn(authorizationCodePKCERefreshRequestMock).when(spotifyApiClientSpy).getAuthorizationCodePKCERefreshRequest();
        when(authorizationCodePKCERefreshRequestMock.execute()).thenReturn(authorizationCodeCredentialsMock);
        when(authorizationCodeCredentialsMock.getAccessToken()).thenReturn("access_token");
        when(authorizationCodeCredentialsMock.getRefreshToken()).thenReturn("refresh_token");
        databaseService.addApiIntegrationSetting(Integrations.SPOTIFY.key, credentials.getRefreshToken());

        doNothing().when(spotifyApiClientSpy).start(any());
        spotifyApiClientSpy.refresh();
        verify(executorService).execute(runnableArgumentCaptor.capture());
        Runnable runnable = runnableArgumentCaptor.getValue();
        runnable.run();
        verify(spotifyApiClientSpy).start(any());
    }

    @Test
    public void refreshSpotifyApiClientTestFailure() throws IOException, ParseException, SpotifyWebApiException {
        AuthorizationCodePKCERefreshRequest authorizationCodePKCERefreshRequestMock = Mockito.mock(AuthorizationCodePKCERefreshRequest.class);
        Credentials credentials = new Credentials();
        credentials.setAccessToken("access_token");
        credentials.setRefreshToken("refresh_token");

        doReturn(authorizationCodePKCERefreshRequestMock).when(spotifyApiClientSpy).getAuthorizationCodePKCERefreshRequest();
        when(authorizationCodePKCERefreshRequestMock.execute()).thenThrow(new BadRequestException("Refresh token revoked"));
        databaseService.addApiIntegrationSetting(Integrations.SPOTIFY.key, credentials.getRefreshToken());

        spotifyApiClientSpy.refresh();
        verify(executorService).execute(runnableArgumentCaptor.capture());
        Runnable runnable = runnableArgumentCaptor.getValue();
        runnable.run();
        verify(databaseService).deleteApiIntegrationSetting(eq(Integrations.SPOTIFY.key));
    }

    @AfterEach
    public void tearDown() {
        databaseService.deleteApiIntegrationSetting(Integrations.SPOTIFY.key);
        currentUser = null;
        databaseService = null;
        spotifyApiClientSpy = null;
    }
}
