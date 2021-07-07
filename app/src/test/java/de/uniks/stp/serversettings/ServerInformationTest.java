package de.uniks.stp.serversettings;

import de.uniks.stp.AccordApp;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.dagger.components.test.AppTestComponent;
import de.uniks.stp.dagger.components.test.SessionTestComponent;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.network.rest.SessionRestClient;
import de.uniks.stp.network.websocket.WebSocketService;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(ApplicationExtension.class)
public class ServerInformationTest {
    @Mock
    private HttpResponse<JsonNode> res;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;

    private AccordApp app;
    private Router router;
    private Editor editor;
    private SessionRestClient restMock;
    private User currentUser;

    @Start
    public void start(Stage stage) {
        // start application
        MockitoAnnotations.initMocks(this);
        app = new AccordApp();
        app.setTestMode(true);
        app.start(stage);

        AppTestComponent appTestComponent = (AppTestComponent) app.getAppComponent();
        router = appTestComponent.getRouter();
        editor = appTestComponent.getEditor();
        currentUser = editor.createCurrentUser("Test", true).setId("123-45");
        editor.setCurrentUser(currentUser);

        SessionTestComponent sessionTestComponent = appTestComponent
            .sessionTestComponentBuilder()
            .currentUser(currentUser)
            .userKey("123-45")
            .build();
        app.setSessionComponent(sessionTestComponent);

        WebSocketService webSocketService = sessionTestComponent.getWebsocketService();
        restMock = sessionTestComponent.getSessionRestClient();
        webSocketService.init();
    }

    @Test
    public void serverInformationTest(FxRobot robot) {
        Platform.runLater(()-> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_LIST_ONLINE_USERS));
        WaitForAsyncUtils.waitForFxEvents();

        //getServers Response
        String server1Id = "server1Id";
        String server1Name = "server1Name";
        JSONObject j = new JSONObject().put("status", "success").put("message", "")
            .put("data", new JSONArray().put(new JSONObject().put("id", server1Id).put("name", server1Name)));
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);
        verify(restMock).getServers(callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(1, editor.getOrCreateAccord().getCurrentUser().getAvailableServers().size());

        //getServerInformation Response
        String serverOwnerId = "serverOwnerId";
        String serverOwnerName = "serverOwnerName";
        boolean serverOwnerStatus = false;
        String category1Id = "category1Id";
        String category2Id = "category2Id";
        JSONObject server1Information = new JSONObject().put("status", "success").put("message", "")
            .put("data", new JSONObject().put("id", server1Id)
                .put("name", server1Name)
                .put("owner", serverOwnerId)
                .put("categories", new JSONArray()
                    .put(category1Id)
                    .put(category2Id))
                .put("members", new JSONArray()
                    .put(new JSONObject()
                        .put("id", currentUser.getId())
                        .put("name", currentUser.getName())
                        .put("online", true))
                    .put(new JSONObject()
                        .put("id", serverOwnerId)
                        .put("name", serverOwnerName)
                        .put("online", serverOwnerStatus))));
        when(res.getBody()).thenReturn(new JsonNode(server1Information.toString()));
        when(res.isSuccess()).thenReturn(true);
        verify(restMock).getServerInformation(eq(server1Id), callbackCaptor.capture());
        Callback<JsonNode> server1InformationCallback = callbackCaptor.getValue();
        server1InformationCallback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(2, editor.getOrCreateAccord().getCurrentUser().getAvailableServers().get(0).getUsers().size());
        Assertions.assertNotEquals(currentUser, editor.getOrCreateAccord().getCurrentUser().getAvailableServers().get(0).getOwner());

        //getCategories Response
        String category1Name = "category1Name";
        String category2Name = "category2Name";
        String textChannelId = "textChannelId";
        String voiceChannelId = "voiceChannelId";
        JSONObject serverCategories = new JSONObject()
            .put("status", "success")
            .put("message", "")
            .put("data", new JSONArray()
                .put(new JSONObject()
                    .put("id", category1Id)
                    .put("name", category1Name)
                    .put("server", server1Id)
                    .put("channels", new JSONArray()
                        .put(textChannelId)
                        .put(voiceChannelId)))
                .put(new JSONObject()
                    .put("id", category2Id)
                    .put("name", category2Name)
                    .put("server", server1Id)
                    .put("channels", new JSONArray())));
        when(res.getBody()).thenReturn(new JsonNode(serverCategories.toString()));
        when(res.isSuccess()).thenReturn(true);
        verify(restMock).getCategories(eq(server1Id), callbackCaptor.capture());
        Callback<JsonNode> serverCategoriesCallback = callbackCaptor.getValue();
        serverCategoriesCallback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(2, editor.getOrCreateAccord().getCurrentUser().getAvailableServers().get(0).getCategories().size());
        Server server = editor.getOrCreateAccord().getCurrentUser().getAvailableServers().get(0);
        Category category1 = server.getCategories().get(0).getName().equals(category1Name) ? server.getCategories().get(0) : server.getCategories().get(1);
        Category category2 = server.getCategories().get(0).getName().equals(category2Name) ? server.getCategories().get(0) : server.getCategories().get(1);
        Assertions.assertEquals(category1Name, category1.getName());
        Assertions.assertEquals(category2Name, category2.getName());

        //getChannels Response
        String textChannelName = "textChannelName";
        String voiceChannelName = "voiceChannelName";
        JSONObject category1Channels = new JSONObject()
            .put("status", "success")
            .put("message", "")
            .put("data", new JSONArray()
                .put(new JSONObject()
                    .put("id", textChannelId)
                    .put("name", textChannelName)
                    .put("type", "text")
                    .put("privileged", false)
                    .put("category", category1Id)
                    .put("members", new JSONArray())
                    .put("audioMembers", new JSONArray()))
                .put(new JSONObject()
                    .put("id", voiceChannelId)
                    .put("name", voiceChannelName)
                    .put("type", "audio")
                    .put("privileged", false)
                    .put("category", category1Id)
                    .put("members", new JSONArray())
                    .put("audioMembers", new JSONArray()
                        .put(currentUser.getId()))));
        when(res.getBody()).thenReturn(new JsonNode(category1Channels.toString()));
        when(res.isSuccess()).thenReturn(true);
        verify(restMock).getChannels(eq(server1Id), eq(category1Id), callbackCaptor.capture());
        Callback<JsonNode> category1ChannelsCallback = callbackCaptor.getValue();
        category1ChannelsCallback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        String privilegedChannelId = "privilegedChannelId";
        String privilegedChannelName = "privilegedChannelName";
        JSONObject category2Channels = new JSONObject()
            .put("status", "success")
            .put("message", "")
            .put("data", new JSONArray()
                .put(new JSONObject()
                    .put("id", privilegedChannelId)
                    .put("name", privilegedChannelName)
                    .put("type", "text")
                    .put("privileged", true)
                    .put("category", category2Id)
                    .put("members", new JSONArray()
                        .put(currentUser.getId()))
                    .put("audioMembers", new JSONArray())));
        when(res.getBody()).thenReturn(new JsonNode(category2Channels.toString()));
        when(res.isSuccess()).thenReturn(true);
        verify(restMock).getChannels(eq(server1Id), eq(category1Id), callbackCaptor.capture());
        Callback<JsonNode> category2ChannelsCallback = callbackCaptor.getValue();
        category2ChannelsCallback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        //Check category1 channels
        Assertions.assertEquals(2, category1.getChannels().size());
        Channel textChannel = category1.getChannels().get(0).getName().equals(textChannelName) ? category1.getChannels().get(0) : category1.getChannels().get(1);
        Channel voiceChannel = category1.getChannels().get(0).getName().equals(voiceChannelName) ? category1.getChannels().get(0) : category1.getChannels().get(1);

        Assertions.assertEquals(textChannelName, textChannel.getName());
        Assertions.assertEquals(textChannelId, textChannel.getId());
        Assertions.assertEquals("text", textChannel.getType());
        Assertions.assertEquals(0, textChannel.getChannelMembers().size());

        Assertions.assertEquals(voiceChannelName, voiceChannel.getName());
        Assertions.assertEquals(voiceChannelId, voiceChannel.getId());
        Assertions.assertEquals("audio", voiceChannel.getType());
        Assertions.assertEquals(1, voiceChannel.getAudioMembers().size());
        Assertions.assertEquals(currentUser.getId(), voiceChannel.getAudioMembers().get(0).getId());

        //Check category2 channel
        Assertions.assertEquals(1, category2.getChannels().size());
        Channel privilegedChannel = category2.getChannels().get(0);
        Assertions.assertEquals(privilegedChannelName, privilegedChannel.getName());
        Assertions.assertEquals(privilegedChannelId, privilegedChannel.getId());
        Assertions.assertEquals("text", privilegedChannel.getType());
        Assertions.assertEquals(1, privilegedChannel.getChannelMembers().size());
        Assertions.assertEquals(currentUser.getId(), privilegedChannel.getChannelMembers().get(0).getId());

        //Check view
        robot.clickOn("#" + server.getId()+"-navBarElement");

        Set<Node> categoryElements =  robot.lookup("#category-head-pane").queryAll();
        Assertions.assertEquals(2, categoryElements.size());
        Set<Node> channelElements = robot.lookup("#channel-element-marker").queryAll();
        Assertions.assertEquals(3, channelElements.size());
    }

    @AfterEach
    void tear() {
        restMock = null;
        router = null;
        editor = null;
        currentUser = null;
        res = null;
        callbackCaptor = null;
        Platform.runLater(app::stop);
        WaitForAsyncUtils.waitForFxEvents();
        app = null;
    }
}
