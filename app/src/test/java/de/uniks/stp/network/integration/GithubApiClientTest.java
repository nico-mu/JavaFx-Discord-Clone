package de.uniks.stp.network.integration;

import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.model.User;
import de.uniks.stp.network.integration.api.GitHubApiClient;
import de.uniks.stp.network.rest.SessionRestClient;
import kong.unirest.GetRequest;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.UnirestInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import javax.json.Json;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class GithubApiClientTest {

    @Mock
    private HttpResponse<JsonNode> changeDescriptionResponseMock;

    @Mock
    private HttpResponse<JsonNode> gitHubApiResponseMock;

    @Mock
    private ExecutorService executorService;

    @Mock
    private UnirestInstance instanceMock;

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
    private GitHubApiClient githubApiClientSpy;

    @BeforeEach
    public void setUp() {
        sessionRestClient = Mockito.mock(SessionRestClient.class);
        currentUser = Mockito.spy(new User().setName("test").setId("123"));
        databaseService =  Mockito.spy(new SessionDatabaseService(currentUser));
        githubApiClientSpy = Mockito.spy(new GitHubApiClient(currentUser, sessionRestClient, databaseService));

        MockitoAnnotations.initMocks(this);
        when(changeDescriptionResponseMock.isSuccess()).thenReturn(true);

        doReturn(scheduledFutureMock).when(scheduledExecutorService)
            .scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
    }

    @Test
    public void startGithubApiClientTestSuccessful() {
        Credentials credentials = new Credentials();
        credentials.setAccessToken("access_token");
        credentials.setRefreshToken("refresh_token");
        String name = "github";
        String jsonResponse = Json.createObjectBuilder().add("name", name).build().toString();

        GetRequest getRequestMock = Mockito.mock(GetRequest.class);
        when(instanceMock.get(IntegrationConstants.GITHUB_API_URL + IntegrationConstants.GITHUB_API_USER_URL)).thenReturn(getRequestMock);
        when(getRequestMock.header(anyString(), anyString())).thenReturn(getRequestMock);
        when(getRequestMock.asJson()).thenReturn(gitHubApiResponseMock);
        when(gitHubApiResponseMock.isSuccess()).thenReturn(true);
        when(gitHubApiResponseMock.getBody()).thenReturn(new JsonNode(jsonResponse));
        when(sessionRestClient.updateDescriptionSync(eq("123"), anyString())).thenReturn(changeDescriptionResponseMock);

        githubApiClientSpy.start(credentials);

        verify(scheduledExecutorService)
            .scheduleAtFixedRate(scheduledRunnableArgumentCaptor.capture(), eq(0L), anyLong(), eq(TimeUnit.MILLISECONDS));

        Runnable scheduledRunnable = scheduledRunnableArgumentCaptor.getValue();
        scheduledRunnable.run();

        verify(currentUser).setDescription("%" + name);
    }

//    @Test
//    public void startGithubApiClientTestRefresh() throws IOException, ParseException, SpotifyWebApiException {
//        GetUsersCurrentlyPlayingTrackRequest currentlyPlayingTrackRequestMock = Mockito.mock(GetUsersCurrentlyPlayingTrackRequest.class);
//        AuthorizationCodePKCERefreshRequest authorizationCodePKCERefreshRequestMock = Mockito.mock(AuthorizationCodePKCERefreshRequest.class);
//        AuthorizationCodeCredentials authorizationCodeCredentialsMock = Mockito.mock(AuthorizationCodeCredentials.class);
//        Credentials credentials = new Credentials();
//        credentials.setAccessToken("access_token");
//        credentials.setRefreshToken("refresh_token");
//
//        doReturn(currentlyPlayingTrackRequestMock).when(githubApiClientSpy).getUsersCurrentlyPlayingTrackRequest();
//        doReturn(authorizationCodePKCERefreshRequestMock).when(githubApiClientSpy).getAuthorizationCodePKCERefreshRequest();
//        when(currentlyPlayingTrackRequestMock.execute()).thenThrow(new UnauthorizedException("The access token expired"));
//        when(authorizationCodePKCERefreshRequestMock.execute()).thenReturn(authorizationCodeCredentialsMock);
//        when(authorizationCodeCredentialsMock.getAccessToken()).thenReturn("access_token");
//        when(authorizationCodeCredentialsMock.getRefreshToken()).thenReturn("refresh_token");
//
//        githubApiClientSpy.start(credentials);
//        verify(scheduledExecutorService)
//            .scheduleAtFixedRate(scheduledRunnableArgumentCaptor.capture(), eq(0L), anyLong(), eq(TimeUnit.MILLISECONDS));
//
//        Runnable scheduledRunnable = scheduledRunnableArgumentCaptor.getValue();
//        scheduledRunnable.run();
//
//        verify(githubApiClientSpy).refresh();
//    }
//
    @Test
    public void refreshGithubApiClientTestSuccess() {
        Credentials credentials = new Credentials();
        credentials.setAccessToken("access_token");
        credentials.setRefreshToken("refresh_token");

        databaseService.addApiIntegrationSetting(Integrations.GITHUB.key, credentials.getRefreshToken());

        doNothing().when(githubApiClientSpy).start(any());
        githubApiClientSpy.refresh();
        verify(githubApiClientSpy).start(any());
    }

    @Test
    public void refreshGithubApiClientTestFailure() {
        Credentials credentials = new Credentials();
        credentials.setAccessToken("access_token");
        credentials.setRefreshToken("refresh_token");

        String jsonResponse = Json.createObjectBuilder().add("message", "Bad credentials").build().toString();

        GetRequest getRequestMock = Mockito.mock(GetRequest.class);
        when(instanceMock.get(IntegrationConstants.GITHUB_API_URL + IntegrationConstants.GITHUB_API_USER_URL)).thenReturn(getRequestMock);
        when(getRequestMock.header(anyString(), anyString())).thenReturn(getRequestMock);
        when(getRequestMock.asJson()).thenReturn(gitHubApiResponseMock);

        when(gitHubApiResponseMock.isSuccess()).thenReturn(false);
        when(gitHubApiResponseMock.getStatus()).thenReturn(401);
        when(gitHubApiResponseMock.getBody()).thenReturn(new JsonNode(jsonResponse));
        databaseService.addApiIntegrationSetting(Integrations.GITHUB.key, credentials.getRefreshToken());

        githubApiClientSpy.refresh();

        verify(scheduledExecutorService)
            .scheduleAtFixedRate(scheduledRunnableArgumentCaptor.capture(), eq(0L), anyLong(), eq(TimeUnit.MILLISECONDS));

        Runnable scheduledRunnable = scheduledRunnableArgumentCaptor.getValue();
        scheduledRunnable.run();

        verify(databaseService).deleteApiIntegrationSetting(eq(Integrations.GITHUB.key));
    }

    @AfterEach
    public void tearDown() {
        databaseService.deleteApiIntegrationSetting(Integrations.GITHUB.key);
        currentUser = null;
        databaseService = null;
        instanceMock = null;
        githubApiClientSpy = null;
        scheduledExecutorService = null;
        scheduledFutureMock = null;
        runnableArgumentCaptor = null;
        scheduledRunnableArgumentCaptor = null;
        changeDescriptionResponseMock = null;
        gitHubApiResponseMock = null;
    }
}
