package de.uniks.stp.controller;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.StageManager;
import de.uniks.stp.model.DirectMessage;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.rest.AppRestClient;
import de.uniks.stp.network.websocket.WebSocketClient;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
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

import java.util.Date;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(ApplicationExtension.class)
public class InviteMessageTest {
    @Mock
    private AppRestClient restMock;

    @Mock
    private WebSocketClient webSocketMock;

    @Mock
    private HttpResponse<JsonNode> res;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;
    private StageManager app;

    @Start
    public void start(Stage stage) {
        // start application
        MockitoAnnotations.initMocks(this);
        NetworkClientInjector.setRestClient(restMock);
        NetworkClientInjector.setWebSocketClient(webSocketMock);
        StageManager.setBackupMode(false);
        app = new StageManager();
        app.start(stage);
    }

    /**
     * Tests that join Server Button is shown under message with an inivitation and checks that join Rest call is called
     * when button was pressed
     * @param robot
     */
    @Test
    public void testPressJoinServerButton(FxRobot robot) {
        // prepare start situation
        Editor editor = StageManager.getEditor();

        User currentUser = new User().setName("Platti");
        editor.getOrCreateAccord().setCurrentUser(currentUser).setUserKey("123-45");

        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_LIST_ONLINE_USERS));
        WaitForAsyncUtils.waitForFxEvents();

        // show private Chat
        String otherUserName = "otherTestUser";
        String otherUserId = "123";

        JSONObject otherUserJson = new JSONObject().put("id", otherUserId).put("name", otherUserName);
        JSONArray data = new JSONArray().put(otherUserJson);
        JSONObject j = new JSONObject().put("status", "success").put("message", "").put("data", data);
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock).requestOnlineUsers(callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);

        RouteArgs args = new RouteArgs().addArgument(Constants.ROUTE_PRIVATE_CHAT_ARGS, otherUserId);
        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_PRIVATE_CHAT, args));
        WaitForAsyncUtils.waitForFxEvents();

        // show invite message
        String newServerId = "4242";
        String inviteId = "1212123";
        String inviteLink = Constants.REST_SERVER_BASE_URL + Constants.REST_SERVER_PATH + "/" + newServerId
            + Constants.REST_INVITES_PATH + "/" + inviteId;
        String msgText = "Hey there, come and join my server:" + inviteLink;

        User chatPartner = editor.getOrCreateChatPartnerOfCurrentUser(otherUserId, otherUserName);
        DirectMessage msg = new DirectMessage();
        msg.setReceiver(currentUser).setMessage(msgText).setTimestamp(new Date().getTime()-10000).setId(UUID.randomUUID().toString());
        msg.setSender(chatPartner);
        WaitForAsyncUtils.waitForFxEvents();

        // Assert invite button is shown and invokes rest call
        JFXButton joinButton = robot.lookup("#"+newServerId+"-"+inviteId).query();
        robot.clickOn(joinButton);
        WaitForAsyncUtils.waitForFxEvents();

        verify(restMock).joinServer(eq(newServerId), eq(inviteId), eq(currentUser.getName()), eq(currentUser.getPassword()), callbackCaptor.capture());
    }

    /**
     * Tests that join server button is not shown when message contains invite link for a server the user already joined
     * @param robot
     */
    @Test
    public void testJoinButtonNotShown(FxRobot robot) {
        // prepare start situation
        Editor editor = StageManager.getEditor();

        User currentUser = new User().setName("Platti");
        editor.getOrCreateAccord().setCurrentUser(currentUser).setUserKey("123-45");

        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_LIST_ONLINE_USERS));
        WaitForAsyncUtils.waitForFxEvents();

        // add server to model before invite message is shown
        String newServerId = "4242";
        String inviteId = "1212123";
        Server server = new Server();
        server.setId(newServerId).setName("serverName");
        currentUser.withAvailableServers(server);

        // show private Chat
        String otherUserName = "otherTestUser";
        String otherUserId = "123";

        JSONObject otherUserJson = new JSONObject().put("id", otherUserId).put("name", otherUserName);
        JSONArray data = new JSONArray().put(otherUserJson);
        JSONObject j = new JSONObject().put("status", "success").put("message", "").put("data", data);
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock).requestOnlineUsers(callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);

        RouteArgs args = new RouteArgs().addArgument(Constants.ROUTE_PRIVATE_CHAT_ARGS, otherUserId);
        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_PRIVATE_CHAT, args));
        WaitForAsyncUtils.waitForFxEvents();

        // show invite message
        String inviteLink = Constants.REST_SERVER_BASE_URL + Constants.REST_SERVER_PATH + "/" + newServerId
            + Constants.REST_INVITES_PATH + "/" + inviteId;
        String msgText = "Hey there, come and join my server:" + inviteLink;

        User chatPartner = editor.getOrCreateChatPartnerOfCurrentUser(otherUserId, otherUserName);
        DirectMessage msg = new DirectMessage();
        msg.setReceiver(currentUser).setMessage(msgText).setTimestamp(new Date().getTime()-10000).setId(UUID.randomUUID().toString());
        msg.setSender(chatPartner);
        WaitForAsyncUtils.waitForFxEvents();

        // Assert invite button is not shown
        boolean shown = true;
        try{
            JFXButton joinButton = robot.lookup("#"+newServerId+"-"+inviteId).query();
        } catch(Exception e){
            shown = false;
        }
        Assertions.assertFalse(shown);
    }

    @AfterEach
    void tear(){
        restMock = null;
        webSocketMock = null;
        res = null;
        callbackCaptor = null;
        Platform.runLater(app::stop);
        WaitForAsyncUtils.waitForFxEvents();
        app = null;
    }
}
