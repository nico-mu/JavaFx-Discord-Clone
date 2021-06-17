package de.uniks.stp.serversettings;

import com.jfoenix.controls.JFXTextField;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.StageManager;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.network.*;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
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
public class EditChannelTest {
    @Mock
    private RestClient restMock;

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
        endpointCallbackHashmap = new HashMap<>();
        NetworkClientInjector.setRestClient(restMock);
        NetworkClientInjector.setWebSocketClient(webSocketMock);
        StageManager.setBackupMode(false);
        app = new StageManager();
        app.start(stage);
    }

    @Test
    public void editServerFailedTest(FxRobot robot) {
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

        String channelId = "chId1234";
        String channelName = "testChannel";
        Channel channel = new Channel().setName(channelName).setId(channelId).setPrivileged(false).setType("text");
        category.withChannels(channel);
        testServer.withChannels(channel);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(1, editor.getServer(serverId).getCategories().get(0).getChannels().size());


        robot.clickOn("#channel-container");
        robot.point("#channel-container");
        robot.point("#edit-channel");
        robot.clickOn("#edit-channel");
        JFXTextField nameTextfield = robot.lookup("#edit-channel-name-textfield").query();
        Platform.runLater(() -> nameTextfield.clear());
        WaitForAsyncUtils.waitForFxEvents();
    
        robot.clickOn("#edit-channel-create-button");

        JSONObject j = new JSONObject().put("status", "failure").put("message", "Missing name")
            .put("data", new JSONObject());
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(false);

        verify(restMock).editTextChannel(eq(serverId), eq(categoryId), eq(channelId), eq(""), eq(false), eq(new ArrayList<String>()), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        Label errorLabel = robot.lookup("#edit-channel-error").query();

        Assertions.assertEquals(channelName, editor.getServer(serverId).getCategories().get(0).getChannels().get(0).getName());
        Assertions.assertNotEquals("", errorLabel.getText());

        String newChannelName = "TestChannel";

        robot.clickOn("#edit-channel-name-textfield");
        robot.write(newChannelName);
        robot.clickOn("#privileged-checkbox");
        robot.clickOn("#edit-channel-create-button");

        j = new JSONObject().put("status", "failure").put("message", "Missing members")
            .put("data", new JSONObject());
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(false);

        verify(restMock).editTextChannel(eq(serverId), eq(categoryId), eq(channelId), eq(newChannelName), eq(true), eq(new ArrayList<String>()), callbackCaptor.capture());
        callback = callbackCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(channelName, editor.getServer(serverId).getCategories().get(0).getChannels().get(0).getName());
        errorLabel = robot.lookup("#edit-channel-error").query();
        Assertions.assertNotEquals("", errorLabel.getText());
        robot.clickOn("#edit-channel-cancel-button");
    }

    @Test
    public void editChannelTest(FxRobot robot) {
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

        String channelId = "chId1234";
        String channelName = "testChannel";
        Channel channel = new Channel().setName(channelName).setId(channelId).setPrivileged(false).setType("text");
        category.withChannels(channel);
        testServer.withChannels(channel);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(1, editor.getServer(serverId).getCategories().get(0).getChannels().size());

        robot.clickOn("#channel-container");
        robot.point("#channel-container");
        robot.point("#edit-channel");
        robot.clickOn("#edit-channel");

        String newChannelName = "edited";
        robot.doubleClickOn("#edit-channel-name-textfield");
        robot.write(newChannelName);
        robot.clickOn("#privileged-checkbox");

        VBox userCheckList = robot.lookup("#user-list-vbox").query();
        Assertions.assertEquals(2, userCheckList.getChildren().size());

        robot.clickOn("#filter-user-textfield");
        robot.write("2");

        Assertions.assertEquals(1, userCheckList.getChildren().size());
        robot.clickOn("#user-check-list-entry-checkbox");

        robot.doubleClickOn("#filter-user-textfield");
        robot.write("3");

        Assertions.assertEquals(1, userCheckList.getChildren().size());
        robot.clickOn("#user-check-list-entry-checkbox");

        robot.doubleClickOn("#filter-user-textfield");
        robot.write("T");

        Assertions.assertEquals(2, userCheckList.getChildren().size());

        robot.clickOn("#edit-channel-create-button");

        ArrayList<String> members = new ArrayList<>();
        members.add("2");
        members.add("3");

        JSONObject j = new JSONObject().put("status", "failure").put("message", "Missing name")
            .put("data", new JSONObject());
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock).editTextChannel(eq(serverId), eq(categoryId), eq(channelId), eq("edited"), eq(true), eq(members), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(channelName, editor.getServer(serverId).getCategories().get(0).getChannels().get(0).getName());

        WebSocketService.addServerWebSocket(serverId);

        verify(webSocketMock, times(4)).inject(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();

        for (int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }
        WSCallback systemCallback = endpointCallbackHashmap.get(Constants.WS_SYSTEM_PATH + Constants.WS_SERVER_SYSTEM_PATH + serverId);

        JsonObject jsonObject = Json.createObjectBuilder()
            .add("action", "channelUpdated")
            .add("data",
                Json.createObjectBuilder()
                    .add("id", channelId)
                    .add("name", newChannelName)
                    .add("type", "text")
                    .add("privileged", true)
                    .add("category", categoryId)
                    .add("members", Json.createArrayBuilder().add("1").add("2").build())
                    .build()
            )
            .build();
        systemCallback.handleMessage(jsonObject);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(newChannelName, channel.getName());
        Assertions.assertEquals(true, channel.isPrivileged());
        Assertions.assertEquals(2, channel.getChannelMembers().size());
        String channelNameId = "#" + channelId + "-ChannelElementText";
        TextFlow channelNameText = robot.lookup(channelNameId).query();
        Assertions.assertEquals(newChannelName, ((Text) channelNameText.getChildren().get(0)).getText());
    }

    @Test
    public void deleteChannelTest(FxRobot robot) {
        Editor editor = StageManager.getEditor();

        editor.getOrCreateAccord().setCurrentUser(new User().setName("TestUser1").setId("1")).setUserKey("123-45");

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

        String channelId = "chId1234";
        String channelName = "testChannel";
        Channel channel = new Channel().setName(channelName).setId(channelId).setPrivileged(false).setType("text");
        category.withChannels(channel);
        testServer.withChannels(channel);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(1, editor.getServer(serverId).getCategories().get(0).getChannels().size());

        robot.clickOn("#channel-container");
        robot.point("#channel-container");
        robot.point("#edit-channel");
        robot.clickOn("#edit-channel");

        robot.clickOn("#delete-channel");
        robot.clickOn("#no-button");
        robot.clickOn("#delete-channel");
        robot.clickOn("#yes-button");

        JSONObject j = new JSONObject().put("status", "success").put("message", "")
            .put("data", new JSONObject());
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock).deleteChannel(eq(serverId), eq(categoryId), eq(channelId), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(1, editor.getServer(serverId).getCategories().get(0).getChannels().size());

        WebSocketService.addServerWebSocket(serverId);

        verify(webSocketMock, times(4)).inject(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();

        for (int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }
        WSCallback systemCallback = endpointCallbackHashmap.get(Constants.WS_SYSTEM_PATH + Constants.WS_SERVER_SYSTEM_PATH + serverId);

        JsonObject jsonObject = Json.createObjectBuilder()
            .add("action", "channelDeleted")
            .add("data",
                Json.createObjectBuilder()
                    .add("id", channelId)
                    .add("name", channelName)
                    .add("category", categoryId)
                    .build()
            )
            .build();
        systemCallback.handleMessage(jsonObject);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(0, editor.getServer(serverId).getCategories().get(0).getChannels().size());
    }

    @AfterEach
    void tear() {
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
