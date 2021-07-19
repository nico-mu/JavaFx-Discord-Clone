package de.uniks.stp.network.rest;

import de.uniks.stp.Constants;
import kong.unirest.HttpMethod;
import kong.unirest.HttpRequest;
import org.junit.jupiter.api.*;
import org.mockito.*;

import javax.json.Json;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

public class AppRestClientTest {

    @Spy
    private AppRestClient appRestClientSpy;

    @Captor
    private ArgumentCaptor<HttpRequest<?>> httpRequestArgumentCaptor;

    private static final String USERNAME = "test";
    private static final String PASSWORD = "password";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        doNothing().when(appRestClientSpy).sendRequest(any(), any());
    }

    @Test
    public void testLoginRequest() {
        appRestClientSpy.login(USERNAME, PASSWORD, null);
        verify(appRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());

        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();
        Assertions.assertEquals(HttpMethod.POST, request.getHttpMethod());
        Assertions.assertEquals(Constants.REST_SERVER_BASE_URL + Constants.REST_USERS_PATH + Constants.REST_LOGIN_PATH, request.getUrl());

        String expectedLoginBody = Json.createObjectBuilder()
            .add("name", USERNAME)
            .add("password", PASSWORD)
            .build()
            .toString();

        Assertions.assertTrue(request.getBody().isPresent());
        Assertions.assertEquals(expectedLoginBody, request.getBody().get().uniPart().toString());
    }

    @Test
    public void testRegisterRequest() {
        appRestClientSpy.register(USERNAME, PASSWORD, null);
        verify(appRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());

        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();
        Assertions.assertEquals(HttpMethod.POST, request.getHttpMethod());
        Assertions.assertEquals(Constants.REST_SERVER_BASE_URL + Constants.REST_USERS_PATH + Constants.REST_REGISTER_PATH, request.getUrl());

        String expectedLoginBody = Json.createObjectBuilder()
            .add("name", USERNAME)
            .add("password", PASSWORD)
            .build()
            .toString();

        Assertions.assertTrue(request.getBody().isPresent());
        Assertions.assertEquals(expectedLoginBody, request.getBody().get().uniPart().toString());
    }

    @Test
    public void testTempRegister() {
        appRestClientSpy.tempRegister(null);
        verify(appRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());

        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();
        Assertions.assertEquals(HttpMethod.POST, request.getHttpMethod());
        Assertions.assertEquals(Constants.REST_SERVER_BASE_URL + Constants.REST_USERS_PATH + Constants.REST_TEMP_REGISTER_PATH, request.getUrl());
        Assertions.assertFalse(request.getBody().isPresent());
    }

    @AfterEach
    public void tearDown() {
        appRestClientSpy.stop();
        appRestClientSpy = null;
        httpRequestArgumentCaptor = null;
    }
}
