package de.uniks.stp.serversettings;

import com.jfoenix.controls.JFXTextField;
import de.uniks.stp.AccordApp;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.dagger.components.test.AppTestComponent;
import de.uniks.stp.dagger.components.test.SessionTestComponent;
import de.uniks.stp.modal.CreateChannelModal;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.network.rest.SessionRestClient;
import de.uniks.stp.network.websocket.WSCallback;
import de.uniks.stp.network.websocket.WebSocketClientFactory;
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
    private HttpResponse<JsonNode> res;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    @Captor
    private ArgumentCaptor<WSCallback> wsCallbackArgumentCaptor;

    private HashMap<String, WSCallback> endpointCallbackHashmap;

    private AccordApp app;
    private Router router;
    private Editor editor;
    private WebSocketClientFactory webSocketClientFactoryMock;
    private WebSocketService webSocketService;
    private SessionRestClient restMock;

    @Start
    public void start(Stage stage) {
        endpointCallbackHashmap = new HashMap<>();
        // start application
        MockitoAnnotations.initMocks(this);
        app = new AccordApp();
        app.setTestMode(true);
        app.start(stage);

        AppTestComponent appTestComponent = (AppTestComponent) app.getAppComponent();
        router = appTestComponent.getRouter();
        editor = appTestComponent.getEditor();
        User currentUser = editor.createCurrentUser("TestUser1", true).setId("1");
        editor.setCurrentUser(currentUser);

        SessionTestComponent sessionTestComponent = appTestComponent
            .sessionTestComponentBuilder()
            .currentUser(currentUser)
            .userKey("123-45")
            .build();
        app.setSessionComponent(sessionTestComponent);

        webSocketClientFactoryMock = sessionTestComponent.getWebSocketClientFactory();
        webSocketService = sessionTestComponent.getWebsocketService();
        restMock = sessionTestComponent.getSessionRestClient();
        webSocketService.init();
    }

    @Test
    public void createChannelFailed(FxRobot robot) {
        // prepare start situation
        String serverName = "TestServer";
        String serverId = "12345678";
        Server testServer = new Server().setName(serverName).setId(serverId);
        editor.getOrCreateAccord()
            .getCurrentUser()
            .withAvailableServers(testServer);

        RouteArgs args = new RouteArgs().addArgument(":id", serverId);
        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
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

        JFXTextField chNameTextField = robot.lookup(CreateChannelModal.ADD_CHANNEL_NAME_TEXTFIELD).query();
        chNameTextField.setText("abc");

        robot.clickOn("#add-channel-create-button");

        JSONObject j = new JSONObject()
            .put("status", "failure")
            .put("message", "Unhandled response")
            .put("data", new JSONObject());
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(false);

        verify(restMock)
            .createChannel(eq(serverId), eq(categoryId), eq("abc"), eq("text"), eq(false), eq(new ArrayList<>()), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        chNameTextField.clear();

        robot.clickOn("#add-channel-create-button");

        j = new JSONObject()
            .put("status", "failure")
            .put("message", "Missing name")
            .put("data", new JSONObject());
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(false);

        verify(restMock)
            .createChannel(eq(serverId), eq(categoryId), eq(""), eq("text"), eq(false), eq(new ArrayList<>()), callbackCaptor.capture());
        callback = callbackCaptor.getValue();
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

        verify(restMock)
            .createChannel(eq(serverId), eq(categoryId), eq(channelName), eq("text"), eq(true), eq(new ArrayList<>()), callbackCaptor.capture());
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
        String serverName = "TestServer";
        String serverId = "12345678";
        Server testServer = new Server().setName(serverName).setId(serverId);
        editor.getOrCreateAccord()
            .getCurrentUser()
            .withAvailableServers(testServer);

        String categoryName = "TestCategory";
        String categoryId = "catId123";

        RouteArgs args = new RouteArgs().addArgument(":id", serverId);
        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
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

        verify(restMock)
            .createChannel(eq(serverId), eq(categoryId), eq(channelName), eq("text"), eq(false), eq(new ArrayList<>()), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        webSocketService.addServerWebSocket(serverId);

        verify(webSocketClientFactoryMock, times(4))
            .create(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

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
        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
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

        webSocketService.addServerWebSocket(serverId);

        verify(webSocketClientFactoryMock, times(4))
            .create(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

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
        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
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

        verify(restMock).createChannel(eq(serverId), eq(categoryId), eq(channelName), eq("audio"), eq(false), eq(new ArrayList<>()), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        webSocketService.addServerWebSocket(serverId);

        verify(webSocketClientFactoryMock, times(4))
            .create(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

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
        Assertions.assertEquals("audio", editor.getServer(serverId).getCategories().get(0).getChannels().get(0).getType());
        Assertions.assertEquals(channelName, editor.getServer(serverId).getCategories().get(0).getChannels().get(0).getName());
        robot.clickOn("#logout-button");
    }

    @AfterEach
     void tear(){
        restMock = null;
        webSocketClientFactoryMock = null;
        editor = null;
        webSocketService = null;
        router = null;
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
