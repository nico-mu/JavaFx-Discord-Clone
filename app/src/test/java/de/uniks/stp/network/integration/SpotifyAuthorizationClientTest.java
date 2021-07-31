package de.uniks.stp.network.integration;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.requests.authorization.authorization_code.pkce.AuthorizationCodePKCERequest;
import de.uniks.stp.AccordApp;
import de.uniks.stp.network.integration.authorization.AuthorizationCallback;
import de.uniks.stp.network.integration.authorization.SpotifyAuthorizationClient;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SpotifyAuthorizationClientTest {

    private AccordApp accordAppMock;

    @Mock
    private ExecutorService executorServiceMock;

    @Mock
    private HttpServer httpServerMock;

    @Mock
    private SpotifyApi spotifyApiMock;

    @Captor
    private ArgumentCaptor<String> authUriArgumentCaptor;

    @Captor
    private ArgumentCaptor<Runnable> runnableArgumentCaptor;

    @InjectMocks
    private SpotifyAuthorizationClient spotifyAuthorizationClient;


    @BeforeEach
    public void setUp() {
        accordAppMock = Mockito.mock(AccordApp.class);
        spotifyAuthorizationClient = new SpotifyAuthorizationClient(accordAppMock);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSpotifyAuthorizationUri() {
       spotifyAuthorizationClient.authorize(null);

       verify(accordAppMock).showUriInBrowser(authUriArgumentCaptor.capture());
       String requestUri = authUriArgumentCaptor.getValue();
       String expectedRedirectUri = getRedirectUri();
       String expectedRequestUri = "https://accounts.spotify.com:443/authorize?" +
           "client_id=" + IntegrationConstants.SPOTIFY_CLIENT_ID + "&" +
           "response_type=code&" +
           "code_challenge_method=S256&" +
           "code_challenge=" + spotifyAuthorizationClient.generateCodeChallenge() + "&" +
           "redirect_uri=" + URLEncoder.encode(expectedRedirectUri, StandardCharsets.UTF_8) + "&" +
           "scope=" + IntegrationConstants.SPOTIFY_AUTHORIZE_SCOPES +"&" +
           "show_dialog=true";

        Assertions.assertEquals(expectedRequestUri, requestUri);
    }

    public String getRedirectUri() {
        return "http://" + IntegrationConstants.TEMP_SERVER_HOST + ":"
            + IntegrationConstants.TEMP_SERVER_PORT
            + IntegrationConstants.TEMP_SERVER_INTEGRATION_PATH
            + IntegrationConstants.TEMP_SERVER_INTEGRATION_PATH_SPOTIFY;
    }

    @Test
    public void testSpotifyAuthorizationSuccess() throws URISyntaxException, IOException, ParseException, SpotifyWebApiException {
        String code = "123";
        String accessToken = "access_token";
        String refreshToken = "refresh_token";

        spotifyAuthorizationClient.setSpotifyApi(spotifyApiMock);
        spotifyAuthorizationClient.authorize(new AuthorizationCallback() {
            @Override
            public void onSuccess(Credentials credentials) {
                Assertions.assertEquals(accessToken, credentials.getAccessToken());
                Assertions.assertEquals(refreshToken, credentials.getRefreshToken());
            }

            @Override
            public void onFailure(String errorMessage) {
                //not tested here
            }
        });

        HttpExchange httpExchangeMock = Mockito.mock(HttpExchange.class);
        OutputStream outputStreamMock = Mockito.mock(OutputStream.class);

        AuthorizationCodePKCERequest authorizationCodePKCERequestMock = Mockito.mock(AuthorizationCodePKCERequest.class);
        AuthorizationCodeCredentials authorizationCodeCredentialsMock = Mockito.mock(AuthorizationCodeCredentials.class);
        String redirectUriWithCode = getRedirectUri() + "?code=" + code;

        when(httpExchangeMock.getRequestURI()).thenReturn(new URI(redirectUriWithCode));
        when(httpExchangeMock.getResponseBody()).thenReturn(outputStreamMock);
        when(authorizationCodePKCERequestMock.execute()).thenReturn(authorizationCodeCredentialsMock);
        when(authorizationCodeCredentialsMock.getAccessToken()).thenReturn(accessToken);
        when(authorizationCodeCredentialsMock.getRefreshToken()).thenReturn(refreshToken);

        spotifyAuthorizationClient.handle(httpExchangeMock);
        verify(executorServiceMock).execute(runnableArgumentCaptor.capture());

        spotifyAuthorizationClient.getCredentials(authorizationCodePKCERequestMock);
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

        spotifyAuthorizationClient.setSpotifyApi(spotifyApiMock);
        spotifyAuthorizationClient.authorize(authorizationCallback);

        HttpExchange httpExchangeMock = Mockito.mock(HttpExchange.class);
        OutputStream outputStreamMock = Mockito.mock(OutputStream.class);
        String redirectUriWithCode = getRedirectUri();

        when(httpExchangeMock.getRequestURI()).thenReturn(new URI(redirectUriWithCode));
        when(httpExchangeMock.getResponseBody()).thenReturn(outputStreamMock);

        spotifyAuthorizationClient.handle(httpExchangeMock);
        verify(executorServiceMock).execute(runnableArgumentCaptor.capture());

        Runnable runnable = runnableArgumentCaptor.getValue();
        runnable.run();
        verify(authorizationCallback).onFailure("not authenticated");
    }

    @AfterEach
    public void tearDown() {
        spotifyAuthorizationClient.noTimeout();
        accordAppMock = null;
        executorServiceMock = null;
        httpServerMock = null;
        spotifyApiMock = null;
        authUriArgumentCaptor = null;
        runnableArgumentCaptor = null;
        spotifyAuthorizationClient = null;
    }
}
