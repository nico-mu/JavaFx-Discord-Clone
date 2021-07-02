package de.uniks.stp.serversettings;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.StageManager;
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
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
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

import javax.json.Json;
import javax.json.JsonObject;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(ApplicationExtension.class)
public class CreateInvitationTest {
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
        endpointCallbackHashmap = new HashMap<>();
        NetworkClientInjector.setRestClient(restMock);
        NetworkClientInjector.setWebSocketClient(webSocketMock);
        StageManager.setBackupMode(false);
        app = new StageManager();
        app.start(stage);
    }

    @Test
    public void createInvitationFailed(FxRobot robot) {
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

        // assert correct start situation
        Assertions.assertEquals(0, testServer.getInvitations().size());

        robot.clickOn("#settings-gear");
        robot.point("#invite-menu-item");
        robot.clickOn("#invite-menu-item");

        JSONObject j = new JSONObject().put("status", "failure").put("message", "You are not the owner of this server")
            .put("data", new JSONObject());
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(false);

        verify(restMock).getServerInvitations(eq(serverId), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(0, testServer.getInvitations().size());
        Label invitesError = robot.lookup("#invites-error").query();
        Assertions.assertNotEquals("", invitesError.getText());

        robot.clickOn("#invites-create");
        robot.clickOn("#create-invite-create");

        JSONObject j2 = new JSONObject().put("status", "failure").put("message", "You are not the owner of this server")
            .put("data", new JSONObject());
        when(res.getBody()).thenReturn(new JsonNode(j2.toString()));
        when(res.isSuccess()).thenReturn(false);

        verify(restMock).getServerInvitations(eq(serverId), callbackCaptor.capture());
        Callback<JsonNode> callback2 = callbackCaptor.getValue();
        callback2.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#create-invite-cancel");
        robot.clickOn("#invites-cancel");
    }

    @Test
    public void createInvitation(FxRobot robot) {
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

        // assert correct start situation
        Assertions.assertEquals(0, testServer.getInvitations().size());

        robot.clickOn("#settings-gear");
        robot.point("#invite-menu-item");
        robot.clickOn("#invite-menu-item");

        JSONArray jsonArray = new JSONArray()
            .put(new JSONObject()
                .put("id", "invId1")
                .put("link", "123456789012345")
                .put("type", "count")
                .put("max", 15)
                .put("current", 0)
                .put("server", serverId))
            .put(new JSONObject()
                .put("id", "invId2")
                .put("link", "b123456789101112131415")
                .put("type", "temporal")
                .put("max", 15)
                .put("current", 0)
                .put("server", serverId));


        JSONObject j = new JSONObject().put("status", "failure").put("message", "")
            .put("data", jsonArray);
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock).getServerInvitations(eq(serverId), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(2, testServer.getInvitations().size());

        robot.clickOn("#invites-create");
        robot.clickOn("#create-invite-create");

        JSONObject j3 = new JSONObject().put("status", "success").put("message", "")
            .put("data", new JSONObject()
                .put("id", "invId3")
                .put("link", "c123456789101112131415")
                .put("type", "temporal")
                .put("max", 15)
                .put("current", 0)
                .put("server", serverId));
        when(res.getBody()).thenReturn(new JsonNode(j3.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock).createServerInvitation(eq(serverId), eq("temporal"), eq(-1), callbackCaptor.capture());
        Callback<JsonNode> callback3 = callbackCaptor.getValue();
        callback3.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(3, testServer.getInvitations().size());

        robot.clickOn("#invites-create");
        robot.clickOn("#create-invide-max");
        robot.clickOn("#create-invite-max-textfield");
        robot.write("15");
        robot.clickOn("#create-invite-create");

        JSONObject j4 = new JSONObject().put("status", "success").put("message", "")
            .put("data", new JSONObject()
                .put("id", "invId4")
                .put("link", "d123456789101112131415")
                .put("type", "count")
                .put("max", 15)
                .put("current", 0)
                .put("server", serverId));
        when(res.getBody()).thenReturn(new JsonNode(j4.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock).createServerInvitation(eq(serverId), eq("count"), eq(15), callbackCaptor.capture());
        Callback<JsonNode> callback4 = callbackCaptor.getValue();
        callback4.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(4, testServer.getInvitations().size());
        ImageView copy = robot.lookup("#copy-invite").query();
        Label link = robot.lookup("#invite-link").query();
        robot.clickOn("#copy-invite");

        Clipboard clipboard = Clipboard.getSystemClipboard();
        Platform.runLater(() -> {
            Assertions.assertEquals(clipboard.getString(), "123456789012345");
        });
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#delete-invite");

        JSONObject j5 = new JSONObject().put("status", "success").put("message", "")
            .put("data", new JSONObject()
                .put("id", "invId1")
                .put("link", "a123456789101112131415")
                .put("type", "count")
                .put("max", 15)
                .put("current", 0)
                .put("server", serverId));
        when(res.getBody()).thenReturn(new JsonNode(j5.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock).deleteServerInvitation(eq(serverId), eq("invId1"), callbackCaptor.capture());
        Callback<JsonNode> callback5 = callbackCaptor.getValue();
        callback5.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(3, testServer.getInvitations().size());

        WebSocketService.addServerWebSocket(serverId);

        verify(webSocketMock, times(4)).inject(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();

        for (int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }
        WSCallback systemCallback = endpointCallbackHashmap.get(Constants.WS_SYSTEM_PATH + Constants.WS_SERVER_SYSTEM_PATH + serverId);

        String invId = "invId4";
        JsonObject jsonObject = Json.createObjectBuilder()
            .add("action", "inviteExpired")
            .add("data",
                Json.createObjectBuilder()
                    .add("id", invId)
                    .add("server", serverId)
                    .build()
            )
            .build();
        systemCallback.handleMessage(jsonObject);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(2, testServer.getInvitations().size());

        robot.clickOn("#invites-cancel");
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
