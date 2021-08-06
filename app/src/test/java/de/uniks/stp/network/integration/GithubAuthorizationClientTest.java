package de.uniks.stp.network.integration;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import de.uniks.stp.AccordApp;
import de.uniks.stp.network.integration.authorization.AuthorizationCallback;
import de.uniks.stp.network.integration.authorization.GitHubAuthorizationClient;
import kong.unirest.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import javax.json.Json;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class GithubAuthorizationClientTest {

    private AccordApp accordAppMock;


    private UnirestInstance instanceSpy;

    @Mock
    private ExecutorService executorServiceMock;

    @Mock
    private HttpServer httpServerMock;

    @Mock
    private HttpResponse<JsonNode> httpResponseMock;

    @Captor
    private ArgumentCaptor<String> authUriArgumentCaptor;

    @Captor
    private ArgumentCaptor<Runnable> runnableArgumentCaptor;

    @InjectMocks
    private GitHubAuthorizationClient githubAuthorizationClient;


    @BeforeEach
    public void setUp() {
        instanceSpy = Mockito.spy(new UnirestInstance(new Config()));
        accordAppMock = Mockito.mock(AccordApp.class);
        githubAuthorizationClient = new GitHubAuthorizationClient(accordAppMock);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGithubAuthorizationUri() {
       githubAuthorizationClient.authorize(null);

       verify(accordAppMock).showUriInBrowser(authUriArgumentCaptor.capture());
       String requestUri = authUriArgumentCaptor.getValue();
       String expectedRedirectUri = getRedirectUri();
       String expectedRequestUri = IntegrationConstants.GITHUB_AUTH_URL + "?" +
           "client_id=" + IntegrationConstants.GITHUB_CLIENT_ID + "&" +
           "redirect_uri=" + URLEncoder.encode(expectedRedirectUri, StandardCharsets.UTF_8) + "&" +
           "scope=" + URLEncoder.encode(IntegrationConstants.GITHUB_SCOPES, StandardCharsets.UTF_8);

        Assertions.assertEquals(expectedRequestUri, requestUri);
    }

    public String getRedirectUri() {
        return "http://" + IntegrationConstants.TEMP_SERVER_HOST + ":"
            + IntegrationConstants.TEMP_SERVER_PORT
            + IntegrationConstants.TEMP_SERVER_INTEGRATION_PATH
            + IntegrationConstants.TEMP_SERVER_INTEGRATION_PATH_GITHUB;
    }

    @Test
    public void testGithubAuthorizationSuccess() throws URISyntaxException, IOException {
        String code = "123";
        String accessToken = "access_token";

        githubAuthorizationClient.authorize(new AuthorizationCallback() {
            @Override
            public void onSuccess(Credentials credentials) {
                Assertions.assertEquals(accessToken, credentials.getAccessToken());
            }

            @Override
            public void onFailure(String errorMessage) {
                //not tested here
            }
        });

        HttpExchange httpExchangeMock = Mockito.mock(HttpExchange.class);
        OutputStream outputStreamMock = Mockito.mock(OutputStream.class);

        String redirectUriWithCode = getRedirectUri() + "?code=" + code;
        String jsonResponseBody = Json.createObjectBuilder()
            .add("access_token", accessToken).build().toString();


         HttpRequestWithBody httpRequestWithBodyMock = Mockito.mock(HttpRequestWithBody.class);
         MultipartBody multipartBodyMock = Mockito.mock(MultipartBody.class);

        doReturn(httpRequestWithBodyMock).when(instanceSpy).post(eq(IntegrationConstants.GITHUB_OAUTH_URL));
        when(httpRequestWithBodyMock.field(anyString(),anyString())).thenReturn(multipartBodyMock);
        when(multipartBodyMock.field(anyString(), anyString())).thenReturn(multipartBodyMock);
        when(multipartBodyMock.header(anyString(), anyString())).thenReturn(multipartBodyMock);
        when(multipartBodyMock.asJson()).thenReturn(httpResponseMock);
        when(httpResponseMock.isSuccess()).thenReturn(true);
        when(httpResponseMock.getBody()).thenReturn(new JsonNode(jsonResponseBody));

        when(httpExchangeMock.getRequestURI()).thenReturn(new URI(redirectUriWithCode));
        when(httpExchangeMock.getResponseBody()).thenReturn(outputStreamMock);

        githubAuthorizationClient.handle(httpExchangeMock);
        verify(executorServiceMock).execute(runnableArgumentCaptor.capture());
        Runnable runnable = runnableArgumentCaptor.getValue();
        runnable.run();
    }

    @Test
    public void testSpotifyAuthorizationFailure() throws URISyntaxException, IOException {
        AuthorizationCallback authorizationCallback = Mockito.spy(new AuthorizationCallback() {
            @Override
            public void onSuccess(Credentials credentials) {

            }

            @Override
            public void onFailure(String errorMessage) {

            }
        });

        githubAuthorizationClient.authorize(authorizationCallback);

        HttpExchange httpExchangeMock = Mockito.mock(HttpExchange.class);
        OutputStream outputStreamMock = Mockito.mock(OutputStream.class);
        String redirectUriWithoutCode = getRedirectUri();

        when(httpExchangeMock.getRequestURI()).thenReturn(new URI(redirectUriWithoutCode));
        when(httpExchangeMock.getResponseBody()).thenReturn(outputStreamMock);

        githubAuthorizationClient.handle(httpExchangeMock);
        verify(executorServiceMock).execute(runnableArgumentCaptor.capture());

        Runnable runnable = runnableArgumentCaptor.getValue();
        runnable.run();
        verify(authorizationCallback).onFailure("not authenticated");
    }

    @AfterEach
    public void tearDown() {
        githubAuthorizationClient.noTimeout();
        accordAppMock = null;
        executorServiceMock = null;
        httpServerMock = null;
        httpResponseMock = null;
        instanceSpy = null;
        authUriArgumentCaptor = null;
        runnableArgumentCaptor = null;
        githubAuthorizationClient = null;
    }
}
