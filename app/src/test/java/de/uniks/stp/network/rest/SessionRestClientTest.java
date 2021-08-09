package de.uniks.stp.network.rest;

import de.uniks.stp.Constants;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import kong.unirest.HttpMethod;
import kong.unirest.HttpRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SessionRestClientTest {

    private SessionRestClient sessionRestClientSpy;

    @Captor
    private ArgumentCaptor<HttpRequest<?>> httpRequestArgumentCaptor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        sessionRestClientSpy = Mockito.spy(new SessionRestClient(new HttpRequestInterceptor("123")));
        doNothing().when(sessionRestClientSpy).sendRequest(any(), any());
        doReturn(null).when(sessionRestClientSpy).sendSyncRequest(any());
    }

    @Test
    public void testGetServers() {
        sessionRestClientSpy.getServers(null);
        verify(sessionRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());
        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();

        String expectedUrl = Constants.REST_SERVER_BASE_URL + Constants.SERVERS_PATH;

        Assertions.assertEquals(expectedUrl, request.getUrl());
        Assertions.assertEquals(HttpMethod.GET, request.getHttpMethod());
        Assertions.assertFalse(request.getBody().isPresent());
    }

    @Test
    public void testGetServerInformation() {
        String serverId = "123";
        sessionRestClientSpy.getServerInformation(serverId, null);
        verify(sessionRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());
        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();

        String expectedUrl = Constants.REST_SERVER_BASE_URL + Constants.SERVERS_PATH + "/" + serverId;

        Assertions.assertEquals(expectedUrl, request.getUrl());
        Assertions.assertEquals(HttpMethod.GET, request.getHttpMethod());
        Assertions.assertFalse(request.getBody().isPresent());
    }

    @Test
    public void testRenameServer() {
        String serverId = "123";
        String serverName = "1234";
        sessionRestClientSpy.renameServer(serverId, serverName, null);
        verify(sessionRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());
        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();

        String expectedUrl = Constants.REST_SERVER_BASE_URL + Constants.SERVERS_PATH + "/" + serverId;

        Assertions.assertEquals(expectedUrl, request.getUrl());
        Assertions.assertEquals(HttpMethod.PUT, request.getHttpMethod());
        Assertions.assertTrue(request.getBody().isPresent());
    }

    @Test
    public void testDeleteServer() {
        String serverId = "123";
        sessionRestClientSpy.deleteServer(serverId, null);
        verify(sessionRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());
        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();

        String expectedUrl = Constants.REST_SERVER_BASE_URL + Constants.SERVERS_PATH + "/" + serverId;

        Assertions.assertEquals(expectedUrl, request.getUrl());
        Assertions.assertEquals(HttpMethod.DELETE, request.getHttpMethod());
        Assertions.assertFalse(request.getBody().isPresent());
    }

    @Test
    public void testCreateCategory() {
        String serverId = "123";
        sessionRestClientSpy.createCategory(serverId, "testCategory", null);
        verify(sessionRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());
        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();

        String expectedUrl = Constants.REST_SERVER_BASE_URL + Constants.SERVERS_PATH + "/" + serverId + Constants.REST_CATEGORY_PATH;

        Assertions.assertEquals(expectedUrl, request.getUrl());
        Assertions.assertEquals(HttpMethod.POST, request.getHttpMethod());
        Assertions.assertTrue(request.getBody().isPresent());
    }

    @Test
    public void testUpdateCategory() {
        String serverId = "123";
        String categoryId = "1234";
        sessionRestClientSpy.updateCategory(serverId, categoryId, "testCategory", null);
        verify(sessionRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());
        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();

        String expectedUrl = Constants.REST_SERVER_BASE_URL +
            Constants.SERVERS_PATH + "/" + serverId +
            Constants.REST_CATEGORY_PATH + "/" + categoryId;

        Assertions.assertEquals(expectedUrl, request.getUrl());
        Assertions.assertEquals(HttpMethod.PUT, request.getHttpMethod());
        Assertions.assertTrue(request.getBody().isPresent());
    }

    @Test
    public void testDeleteCategory() {
        String serverId = "123";
        String categoryId = "1234";
        sessionRestClientSpy.deleteCategory(serverId, categoryId, null);
        verify(sessionRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());
        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();

        String expectedUrl = Constants.REST_SERVER_BASE_URL +
            Constants.SERVERS_PATH + "/" + serverId +
            Constants.REST_CATEGORY_PATH + "/" + categoryId;

        Assertions.assertEquals(expectedUrl, request.getUrl());
        Assertions.assertEquals(HttpMethod.DELETE, request.getHttpMethod());
        Assertions.assertFalse(request.getBody().isPresent());
    }

    @Test
    public void testLogout() {
        sessionRestClientSpy.sendLogoutRequest(null);
        verify(sessionRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());
        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();

        String expectedUrl = Constants.REST_SERVER_BASE_URL +
            Constants.REST_USERS_PATH + Constants.REST_LOGOUT_PATH;

        Assertions.assertEquals(expectedUrl, request.getUrl());
        Assertions.assertEquals(HttpMethod.POST, request.getHttpMethod());
        Assertions.assertFalse(request.getBody().isPresent());
    }

    @Test
    public void testGetOnlineUsers() {
        sessionRestClientSpy.requestOnlineUsers(null);
        verify(sessionRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());
        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();

        String expectedUrl = Constants.REST_SERVER_BASE_URL + Constants.REST_USERS_PATH;

        Assertions.assertEquals(expectedUrl, request.getUrl());
        Assertions.assertEquals(HttpMethod.GET, request.getHttpMethod());
        Assertions.assertFalse(request.getBody().isPresent());
    }

    @Test
    public void testCreateServer() {
        String name = "server";
        sessionRestClientSpy.createServer(name,null);
        verify(sessionRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());
        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();

        String expectedUrl = Constants.REST_SERVER_BASE_URL + Constants.REST_SERVER_PATH;

        Assertions.assertEquals(expectedUrl, request.getUrl());
        Assertions.assertEquals(HttpMethod.POST, request.getHttpMethod());
        Assertions.assertTrue(request.getBody().isPresent());
    }

    @Test
    public void testGetServerChannelMessages() {
        String serverId = "123";
        String categoryId = "1234";
        String channelId =  "12345";
        long timestamp = 1234567890L;

        sessionRestClientSpy.getServerChannelMessages(serverId, categoryId, channelId, timestamp, null);
        verify(sessionRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());
        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();

        String expectedUrl = Constants.REST_SERVER_BASE_URL +
            Constants.SERVERS_PATH + "/" + serverId +
            Constants.REST_CATEGORY_PATH + "/" + categoryId +
            Constants.REST_CHANNEL_PATH + "/" + channelId +
            Constants.REST_MESSAGES_PATH +
            Constants.REST_TIMESTAMP_PATH + timestamp;

        Assertions.assertEquals(expectedUrl, request.getUrl());
        Assertions.assertEquals(HttpMethod.GET, request.getHttpMethod());
        Assertions.assertFalse(request.getBody().isPresent());
    }

    @Test
    public void testCreateChannel() {
        String serverId = "123";
        String categoryId = "1234";
        sessionRestClientSpy.createChannel(serverId, categoryId, "channel",
            "voice",
            false,
            new ArrayList<>(),
            null);

        verify(sessionRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());
        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();

        String expectedUrl = Constants.REST_SERVER_BASE_URL +
            Constants.SERVERS_PATH + "/" + serverId +
            Constants.REST_CATEGORY_PATH + "/" + categoryId +
            Constants.REST_CHANNEL_PATH;

        Assertions.assertEquals(expectedUrl, request.getUrl());
        Assertions.assertEquals(HttpMethod.POST, request.getHttpMethod());
        Assertions.assertTrue(request.getBody().isPresent());
    }

    @Test
    public void testEditChannel() {
        String serverId = "123";
        String categoryId = "1234";
        String channelId = "12345";
        sessionRestClientSpy.editTextChannel(serverId, categoryId, channelId,
            "testChannel",
            false,
            new ArrayList<>(),
            null);

        verify(sessionRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());
        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();

        String expectedUrl = Constants.REST_SERVER_BASE_URL +
            Constants.SERVERS_PATH + "/" + serverId +
            Constants.REST_CATEGORY_PATH + "/" + categoryId +
            Constants.REST_CHANNEL_PATH + "/" + channelId;

        Assertions.assertEquals(expectedUrl, request.getUrl());
        Assertions.assertEquals(HttpMethod.PUT, request.getHttpMethod());
        Assertions.assertTrue(request.getBody().isPresent());
    }

    @Test
    public void testDeleteChannel() {
        String serverId = "123";
        String categoryId = "1234";
        String channelId = "12345";
        sessionRestClientSpy.deleteChannel(serverId, categoryId, channelId, null);

        verify(sessionRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());
        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();

        String expectedUrl = Constants.REST_SERVER_BASE_URL +
            Constants.SERVERS_PATH + "/" + serverId +
            Constants.REST_CATEGORY_PATH + "/" + categoryId +
            Constants.REST_CHANNEL_PATH + "/" + channelId;

        Assertions.assertEquals(expectedUrl, request.getUrl());
        Assertions.assertEquals(HttpMethod.DELETE, request.getHttpMethod());
        Assertions.assertFalse(request.getBody().isPresent());
    }



    @Test
    public void testGetServerInvitations() {
        String serverId = "123";
        sessionRestClientSpy.getServerInvitations(serverId, null);
        verify(sessionRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());
        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();

        String expectedUrl = Constants.REST_SERVER_BASE_URL + Constants.REST_SERVER_PATH + "/"
            + serverId + Constants.REST_INVITES_PATH;

        Assertions.assertEquals(expectedUrl, request.getUrl());
        Assertions.assertEquals(HttpMethod.GET, request.getHttpMethod());
        Assertions.assertFalse(request.getBody().isPresent());
    }

    @Test
    public void testCreateServerInvitation() {
        String serverId = "123";
        sessionRestClientSpy.createServerInvitation(serverId, "max", 20,  null);
        verify(sessionRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());
        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();

        String expectedUrl = Constants.REST_SERVER_BASE_URL + Constants.REST_SERVER_PATH + "/"
            + serverId + Constants.REST_INVITES_PATH;

        Assertions.assertEquals(expectedUrl, request.getUrl());
        Assertions.assertEquals(HttpMethod.POST, request.getHttpMethod());
        Assertions.assertTrue(request.getBody().isPresent());
    }

    @Test
    public void testDeleteServerInvitation() {
        String serverId = "123";
        String invitationId = "1234";
        sessionRestClientSpy.deleteServerInvitation(serverId, invitationId,  null);
        verify(sessionRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());
        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();

        String expectedUrl = Constants.REST_SERVER_BASE_URL + Constants.REST_SERVER_PATH + "/"
            + serverId + Constants.REST_INVITES_PATH + "/" + invitationId;

        Assertions.assertEquals(expectedUrl, request.getUrl());
        Assertions.assertEquals(HttpMethod.DELETE, request.getHttpMethod());
        Assertions.assertFalse(request.getBody().isPresent());
    }

    @Test
    public void testJoinServer() {
        String serverId = "123";
        String invitationId = "1234";
        sessionRestClientSpy.joinServer(serverId, invitationId, "name", "password",  null);
        verify(sessionRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());
        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();

        String expectedUrl = Constants.REST_SERVER_BASE_URL + Constants.REST_SERVER_PATH + "/"
            + serverId + Constants.REST_INVITES_PATH + "/" + invitationId;

        Assertions.assertEquals(expectedUrl, request.getUrl());
        Assertions.assertEquals(HttpMethod.POST, request.getHttpMethod());
        Assertions.assertTrue(request.getBody().isPresent());
    }

    @Test
    public void testLeaveServer() {
        String serverId = "123";
        sessionRestClientSpy.leaveServer(serverId, null);
        verify(sessionRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());
        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();

        String expectedUrl = Constants.REST_SERVER_BASE_URL + Constants.REST_SERVER_PATH + "/"
            + serverId + "/leave";

        Assertions.assertEquals(expectedUrl, request.getUrl());
        Assertions.assertEquals(HttpMethod.POST, request.getHttpMethod());
        Assertions.assertFalse(request.getBody().isPresent());
    }

    @Test
    public void testJoinAudioChannel() {
        String serverId = "123";
        String categoryId = "1234";
        String channelId = "12345";
        Server server = new Server().setId(serverId);
        Category category = new Category().setId(categoryId).setServer(server);

        sessionRestClientSpy.joinAudioChannel(new Channel().setId(channelId).setCategory(category).setServer(server), null);

        verify(sessionRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());
        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();

        String expectedUrl = Constants.REST_SERVER_BASE_URL +
            Constants.SERVERS_PATH + "/" + serverId +
            Constants.REST_CATEGORY_PATH + "/" + categoryId +
            Constants.REST_CHANNEL_PATH + "/" + channelId + "/join";

        Assertions.assertEquals(expectedUrl, request.getUrl());
        Assertions.assertEquals(HttpMethod.POST, request.getHttpMethod());
        Assertions.assertFalse(request.getBody().isPresent());
    }

    @Test
    public void testLeaveAudioChannel() {
        String serverId = "123";
        String categoryId = "1234";
        String channelId = "12345";
        Server server = new Server().setId(serverId);
        Category category = new Category().setId(categoryId).setServer(server);

        sessionRestClientSpy.leaveAudioChannel(new Channel().setId(channelId).setCategory(category).setServer(server), null);

        verify(sessionRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());
        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();

        String expectedUrl = Constants.REST_SERVER_BASE_URL +
            Constants.SERVERS_PATH + "/" + serverId +
            Constants.REST_CATEGORY_PATH + "/" + categoryId +
            Constants.REST_CHANNEL_PATH + "/" + channelId + "/leave";

        Assertions.assertEquals(expectedUrl, request.getUrl());
        Assertions.assertEquals(HttpMethod.POST, request.getHttpMethod());
        Assertions.assertFalse(request.getBody().isPresent());
    }

    @Test
    public void testUpdateServerChannelMessage() {
        String serverId = "123";
        String categoryId = "1234";
        String channelId =  "12345";
        String messageId = "123456";

        sessionRestClientSpy.updateMessage(serverId, categoryId, channelId, messageId, "hello", null);
        verify(sessionRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());
        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();

        String expectedUrl = Constants.REST_SERVER_BASE_URL +
            Constants.SERVERS_PATH + "/" + serverId +
            Constants.REST_CATEGORY_PATH + "/" + categoryId +
            Constants.REST_CHANNEL_PATH + "/" + channelId +
            Constants.REST_MESSAGES_PATH + "/" + messageId;

        Assertions.assertEquals(expectedUrl, request.getUrl());
        Assertions.assertEquals(HttpMethod.PUT, request.getHttpMethod());
        Assertions.assertTrue(request.getBody().isPresent());
    }

    @Test
    public void testDeleteServerChannelMessage() {
        String serverId = "123";
        String categoryId = "1234";
        String channelId =  "12345";
        String messageId = "123456";

        sessionRestClientSpy.deleteMessage(serverId, categoryId, channelId, messageId, null);
        verify(sessionRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());
        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();

        String expectedUrl = Constants.REST_SERVER_BASE_URL +
            Constants.SERVERS_PATH + "/" + serverId +
            Constants.REST_CATEGORY_PATH + "/" + categoryId +
            Constants.REST_CHANNEL_PATH + "/" + channelId +
            Constants.REST_MESSAGES_PATH + "/" + messageId;

        Assertions.assertEquals(expectedUrl, request.getUrl());
        Assertions.assertEquals(HttpMethod.DELETE, request.getHttpMethod());
        Assertions.assertFalse(request.getBody().isPresent());
    }

    @Test
    public void testGetCategories() {
        String serverId = "123";
        sessionRestClientSpy.getCategories(serverId, null);

        verify(sessionRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());
        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();

        String expectedUrl = Constants.REST_SERVER_BASE_URL +
            Constants.SERVERS_PATH + "/" + serverId +
            Constants.REST_CATEGORY_PATH;

        Assertions.assertEquals(expectedUrl, request.getUrl());
        Assertions.assertEquals(HttpMethod.GET, request.getHttpMethod());
        Assertions.assertFalse(request.getBody().isPresent());
    }

    @Test
    public void testGetChannels() {
        String serverId = "123";
        String categoryId = "1234";
        sessionRestClientSpy.getChannels(serverId, categoryId, null);

        verify(sessionRestClientSpy).sendRequest(httpRequestArgumentCaptor.capture(), any());
        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();

        String expectedUrl = Constants.REST_SERVER_BASE_URL +
            Constants.SERVERS_PATH + "/" + serverId +
            Constants.REST_CATEGORY_PATH + "/" + categoryId +
            Constants.REST_CHANNEL_PATH;

        Assertions.assertEquals(expectedUrl, request.getUrl());
        Assertions.assertEquals(HttpMethod.GET, request.getHttpMethod());
        Assertions.assertFalse(request.getBody().isPresent());
    }

    @Test
    public void testUpdateDescription() {
        String userId = "123";
        String description = "1234";
        sessionRestClientSpy.updateDescriptionSync(userId, description);

        verify(sessionRestClientSpy).sendSyncRequest(httpRequestArgumentCaptor.capture());
        HttpRequest<?> request = httpRequestArgumentCaptor.getValue();

        String expectedUrl = Constants.REST_SERVER_BASE_URL +
            Constants.REST_USERS_PATH + "/" + userId +
            Constants.REST_DESCRIPTION_PATH;

        Assertions.assertEquals(expectedUrl, request.getUrl());
        Assertions.assertEquals(HttpMethod.POST, request.getHttpMethod());
        Assertions.assertTrue(request.getBody().isPresent());
    }



    @AfterEach
    public void tearDown() {
        sessionRestClientSpy.stop();
        sessionRestClientSpy = null;
        httpRequestArgumentCaptor = null;
    }
}
