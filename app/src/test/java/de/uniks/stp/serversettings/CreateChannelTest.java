package de.uniks.stp.serversettings;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.StageManager;
import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.network.rest.AppRestClient;
import de.uniks.stp.network.websocket.WSCallback;
import de.uniks.stp.network.websocket.WebSocketClient;
import de.uniks.stp.network.websocket.WebSocketService;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(ApplicationExtension.class)
public class CreateChannelTest {
    @Mock
    private AppRestClient restMock;

    @Mock
    private WebSocketClient webSocketMock;

    @Mock
    private HttpResponse<JsonNode> res;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    @Captor
    private ArgumentCaptor<WSCallback> wsCallbackArgumentCaptor;

    private HashMap<String, WSCallback> endpointCallbackHashmap;

    private StageManager app;

    @Start
    public void start(Stage stage) {
        // start application
        MockitoAnnotations.initMocks(this);
        NetworkClientInjector.setRestClient(restMock);
        NetworkClientInjector.setWebSocketClient(webSocketMock);
        endpointCallbackHashmap = new HashMap<>();
        StageManager.setBackupMode(false);
        app = new StageManager();
        app.start(stage);
        DatabaseService.clearAllConversations();
    }

    @Test
    public void createChannelFailed(FxRobot robot) {
        // prepare start situation
        Editor editor = StageManager.getEditor();

        editor.getOrCreateAccord().setCurrentUser(new User().setName("Test")).setUserKey("123-45");

        String serverName = "TestServer";
        String serverId = "12345678";
        Server testServer = new Server().setName(serverName).setId(serverId);
        editor.getOrCreateAccord()
            .getCurrentUser()
            .withAvailableServers(testServer);

        RouteArgs args = new RouteArgs().addArgument(":id", serverId);
        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
        WaitForAsyncUtils.waitForFxEvents();

        String categoryName = "TestCategory";
        String categoryId = "catId123";
        Category category = new Category().setName(categoryName).setId(categoryId).setServer(testServer);

        WaitForAsyncUtils.waitForFxEvents();

        // assert correct start situation
        Assertions.assertEquals(1, editor.getOrCreateAccord().getCurrentUser().getAvailableServers().size());
        Assertions.assertEquals(1, editor.getServer(serverId).getCategories().size());
        Assertions.assertEquals(0, editor.getServer(serverId).getCategories().get(0).getChannels().size());

        robot.clickOn("#" + categoryId + "-ServerCategoryElementLabel");
        robot.clickOn("#add-channel-plus");
        robot.clickOn("#add-channel-create-button");

        JSONObject j = new JSONObject().put("status", "failure").put("message", "Missing name")
            .put("data", new JSONObject());
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(false);

        verify(restMock).createChannel(eq(serverId), eq(categoryId), eq(""), eq("text"), eq(false), eq(new ArrayList<String>()), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        Label errorLabel = robot.lookup("#add-channel-error").query();

        Assertions.assertEquals(0, editor.getServer(serverId).getCategories().get(0).getChannels().size());
        Assertions.assertNotEquals("", errorLabel.getText());

        String channelName = "TestChannel";

        robot.clickOn("#add-channel-name-textfield");
        robot.write(channelName);
        robot.clickOn("#privileged-checkbox");
        robot.clickOn("#add-channel-create-button");

        j = new JSONObject().put("status", "failure").put("message", "Missing members")
            .put("data", new JSONObject());
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(false);

        verify(restMock).createChannel(eq(serverId), eq(categoryId), eq(channelName), eq("text"), eq(true), eq(new ArrayList<String>()), callbackCaptor.capture());
        callback = callbackCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(0, editor.getServer(serverId).getCategories().get(0).getChannels().size());
        errorLabel = robot.lookup("#add-channel-error").query();
        Assertions.assertNotEquals("", errorLabel.getText());
        robot.clickOn("#add-channel-cancel-button");
        robot.clickOn("#logout-button");
    }

    @Test
    public void createNotPrivilegedChannel(FxRobot robot) {
        // prepare start situation
        Editor editor = StageManager.getEditor();

        editor.getOrCreateAccord().setCurrentUser(new User().setName("Test")).setUserKey("123-45");

        String serverName = "TestServer";
        String serverId = "12345678";
        Server testServer = new Server().setName(serverName).setId(serverId);
        editor.getOrCreateAccord()
            .getCurrentUser()
            .withAvailableServers(testServer);

        String categoryName = "TestCategory";
        String categoryId = "catId123";

        RouteArgs args = new RouteArgs().addArgument(":id", serverId);
        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
        WaitForAsyncUtils.waitForFxEvents();

        Category category = new Category().setName(categoryName).setId(categoryId).setServer(testServer);

        // assert correct start situation
        Assertions.assertEquals(1, editor.getOrCreateAccord().getCurrentUser().getAvailableServers().size());
        Assertions.assertEquals(1, editor.getServer(serverId).getCategories().size());
        Assertions.assertEquals(0, editor.getServer(serverId).getCategories().get(0).getChannels().size());

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#" + category.getId() + "-ServerCategoryElementLabel");
        robot.clickOn("#add-channel-plus");

        String channelName = "TestChannel";

        robot.clickOn("#add-channel-name-textfield");
        robot.write(channelName);
        robot.clickOn("#add-channel-create-button");

        JSONObject j = new JSONObject().put("status", "success")
            .put("data", new JSONObject());
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock).createChannel(eq(serverId), eq(categoryId), eq(channelName), eq("text"), eq(false), eq(new ArrayList<String>()), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        WebSocketService.addServerWebSocket(serverId);

        verify(webSocketMock, times(4)).inject(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();

        for (int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }
        WSCallback systemCallback = endpointCallbackHashmap.get(Constants.WS_SYSTEM_PATH + Constants.WS_SERVER_SYSTEM_PATH + serverId);

        String channelId = "channel1234";
        JsonObject jsonObject = Json.createObjectBuilder()
            .add("action", "channelCreated")
            .add("data",
                Json.createObjectBuilder()
                    .add("id", channelId)
                    .add("name", channelName)
                    .add("type", "text")
                    .add("privileged", false)
                    .add("category", categoryId)
                    .add("members", Json.createArrayBuilder().build())
                    .build()
            )
            .build();
        systemCallback.handleMessage(jsonObject);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(1, editor.getServer(serverId).getCategories().get(0).getChannels().size());
        Assertions.assertEquals(channelName, editor.getServer(serverId).getCategories().get(0).getChannels().get(0).getName());
        robot.clickOn("#logout-button");
    }

    @Test
    public void createPrivilegedChannel(FxRobot robot) {
        // prepare start situation
        Editor editor = StageManager.getEditor();

        editor.getOrCreateAccord().setCurrentUser(new User().setName("TestUser1").setId("1")).setUserKey("123-45");

        String serverName = "TestServer";
        String serverId = "12345678";
        Server testServer = new Server().setName(serverName).setId(serverId);
        editor.getOrCreateAccord()
            .getCurrentUser()
            .withAvailableServers(testServer);

        User testUser1 = new User().setName("TestUser2").setId("2").withAvailableServers(testServer);
        User testUser2 = new User().setName("TestUser3").setId("3").withAvailableServers(testServer);

        String categoryName = "TestCategory";
        String categoryId = "catId123";

        RouteArgs args = new RouteArgs().addArgument(":id", serverId);
        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
        WaitForAsyncUtils.waitForFxEvents();
        Category category = new Category().setName(categoryName).setId(categoryId).setServer(testServer);

        // assert correct start situation
        Assertions.assertEquals(1, editor.getOrCreateAccord().getCurrentUser().getAvailableServers().size());
        Assertions.assertEquals(1, editor.getServer(serverId).getCategories().size());
        Assertions.assertEquals(0, editor.getServer(serverId).getCategories().get(0).getChannels().size());

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#" + categoryId + "-ServerCategoryElementLabel");
        WaitForAsyncUtils.waitForFxEvents();
        robot.point("#add-channel-plus");
        robot.clickOn("#add-channel-plus");

        String channelName = "TestChannel";

        robot.clickOn("#add-channel-name-textfield");
        robot.write(channelName);
        robot.clickOn("#privileged-checkbox");

        VBox userCheckList = robot.lookup("#user-list-vbox").query();
        Assertions.assertEquals(3, userCheckList.getChildren().size());

        robot.clickOn("#filter-user-textfield");
        robot.write("2");

        Assertions.assertEquals(1, userCheckList.getChildren().size());
        robot.clickOn("#user-check-list-entry-checkbox");

        robot.doubleClickOn("#filter-user-textfield");
        robot.write("1");

        Assertions.assertEquals(1, userCheckList.getChildren().size());
        robot.clickOn("#user-check-list-entry-checkbox");

        robot.doubleClickOn("#filter-user-textfield");
        robot.write("T");

        Assertions.assertEquals(3, userCheckList.getChildren().size());

        robot.clickOn("#add-channel-create-button");

        JSONObject j = new JSONObject().put("status", "success")
            .put("data", new JSONObject());
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);

        ArrayList<String> members = new ArrayList<>();
        members.add("2");
        members.add("1");
        verify(restMock).createChannel(eq(serverId), eq(categoryId), eq(channelName), eq("text"), eq(true), eq(members), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        WebSocketService.addServerWebSocket(serverId);

        verify(webSocketMock, times(4)).inject(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();

        for (int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }
        WSCallback systemCallback = endpointCallbackHashmap.get(Constants.WS_SYSTEM_PATH + Constants.WS_SERVER_SYSTEM_PATH + serverId);

        String channelId = "channel1234";
        JsonObject jsonObject = Json.createObjectBuilder()
            .add("action", "channelCreated")
            .add("data",
                Json.createObjectBuilder()
                    .add("id", channelId)
                    .add("name", channelName)
                    .add("type", "text")
                    .add("privileged", true)
                    .add("category", categoryId)
                    .add("members", Json.createArrayBuilder().add("1").add("2").build())
                    .build()
            )
            .build();
        systemCallback.handleMessage(jsonObject);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(1, editor.getServer(serverId).getCategories().get(0).getChannels().size());
        Assertions.assertEquals(channelName, editor.getServer(serverId).getCategories().get(0).getChannels().get(0).getName());
        Assertions.assertEquals(2, editor.getServer(serverId).getCategories().get(0).getChannels().get(0).getChannelMembers().size());
        robot.clickOn("#logout-button");
    }

    @Test
    public void createAudioChannel(FxRobot robot) {
        // prepare start situation
        Editor editor = StageManager.getEditor();

        editor.getOrCreateAccord().setCurrentUser(new User().setName("Test")).setUserKey("123-45");

        String serverName = "TestServer";
        String serverId = "12345678";
        Server testServer = new Server().setName(serverName).setId(serverId);
        editor.getOrCreateAccord()
            .getCurrentUser()
            .withAvailableServers(testServer);

        User testUser1 = new User().setName("TestUser2").setId("2").withAvailableServers(testServer);

        String categoryName = "TestCategory";
        String categoryId = "catId123";

        RouteArgs args = new RouteArgs().addArgument(":id", serverId);
        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
        WaitForAsyncUtils.waitForFxEvents();

        Category category = new Category().setName(categoryName).setId(categoryId).setServer(testServer);

        // assert correct start situation
        Assertions.assertEquals(1, editor.getOrCreateAccord().getCurrentUser().getAvailableServers().size());
        Assertions.assertEquals(1, editor.getServer(serverId).getCategories().size());
        Assertions.assertEquals(0, editor.getServer(serverId).getCategories().get(0).getChannels().size());

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#" + category.getId() + "-ServerCategoryElementLabel");
        robot.clickOn("#add-channel-plus");

        String channelName = "TestChannel";

        robot.clickOn("#add-channel-name-textfield");
        robot.write(channelName);
        robot.clickOn("#type-toggle-button");
        robot.clickOn("#add-channel-create-button");

        JSONObject j = new JSONObject().put("status", "success")
            .put("data", new JSONObject());
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock).createChannel(eq(serverId), eq(categoryId), eq(channelName), eq("audio"), eq(false), eq(new ArrayList<String>()), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        WebSocketService.addServerWebSocket(serverId);

        verify(webSocketMock, times(4)).inject(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();

        for (int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }
        WSCallback systemCallback = endpointCallbackHashmap.get(Constants.WS_SYSTEM_PATH + Constants.WS_SERVER_SYSTEM_PATH + serverId);

        String channelId = "channel1234";
        JsonObject jsonObject = Json.createObjectBuilder()
            .add("action", "channelCreated")
            .add("data",
                Json.createObjectBuilder()
                    .add("id", channelId)
                    .add("name", channelName)
                    .add("type", "audio")
                    .add("privileged", false)
                    .add("category", categoryId)
                    .add("members", Json.createArrayBuilder().add("2").build())
                    .build()
            )
            .build();
        systemCallback.handleMessage(jsonObject);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(1, editor.getServer(serverId).getCategories().get(0).getChannels().size());
        Assertions.assertTrue(editor.getServer(serverId).getCategories().get(0).getChannels().get(0).getType().equals("audio"));
        Assertions.assertEquals(channelName, editor.getServer(serverId).getCategories().get(0).getChannels().get(0).getName());
        robot.clickOn("#logout-button");
    }

    @AfterEach
     void tear(){
        restMock = null;
        webSocketMock = null;
        res = null;
        callbackCaptor = null;
        stringArgumentCaptor = null;
        wsCallbackArgumentCaptor = null;
        endpointCallbackHashmap = null;
        Platform.runLater(app::stop);
        WaitForAsyncUtils.waitForFxEvents();
        app = null;
    }
}
