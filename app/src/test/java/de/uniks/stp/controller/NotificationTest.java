package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.StageManager;
import de.uniks.stp.component.NavBarUserElement;
import de.uniks.stp.component.ServerChannelElement;
import de.uniks.stp.modal.AddServerModal;
import de.uniks.stp.model.*;
import de.uniks.stp.network.*;
import de.uniks.stp.notification.NotificationService;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONObject;
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
import org.testfx.service.query.EmptyNodeQueryException;
import org.testfx.util.WaitForAsyncUtils;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class NotificationTest {

    @Mock
    private RestClient restMock;

    @Mock
    private WebSocketClient webSocketMock;

    @Mock
    private HttpResponse<JsonNode> res;

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;

    @Captor
    private ArgumentCaptor<WSCallback> wsCallbackArgumentCaptor;

    private static final HashMap<String, WSCallback> endpointCallbackHashmap = new HashMap<>();

    @Start
    public void start(Stage stage) {
        // start application
        MockitoAnnotations.initMocks(this);
        NetworkClientInjector.setRestClient(restMock);
        NetworkClientInjector.setWebSocketClient(webSocketMock);
        StageManager app = new StageManager();
        app.start(stage);
    }

    @Test
    public void testPrivateMessageNotification(FxRobot robot) {
        Editor editor = StageManager.getEditor();

        final String currentUserName = "Test";
        final String userNameOne = "UserOne";
        final String userNameTwo = "UserTwo";

        final User currentUser = new User().setName(currentUserName).setId("123-45");
        final User userOne = new User().setName(userNameOne).setId("111-11");
        final User userTwo = new User().setName(userNameTwo).setId("222-22");

        editor.getOrCreateAccord()
            .setCurrentUser(currentUser)
            .setUserKey("123-45");

        editor.getOrCreateAccord()
            .withOtherUsers(userOne);

        editor.getOrCreateAccord()
            .withOtherUsers(userTwo);


        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME));
        WaitForAsyncUtils.waitForFxEvents();

        verify(webSocketMock, times(2)).inject(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();

        for (int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }
        WSCallback currentUserCallback = endpointCallbackHashmap.get(Constants.WS_USER_PATH + currentUserName);
        JsonObject messageOne = Json.createObjectBuilder()
            .add("channel", "private")
            .add("timestamp", 1)
            .add("message", "Test Nachicht 1")
            .add("from", userNameOne)
            .add("to", currentUserName)
            .build();

        JsonObject messageTwo = Json.createObjectBuilder()
            .add("channel", "private")
            .add("timestamp", 2)
            .add("message", "Test Nachicht 2")
            .add("from", userNameTwo)
            .add("to", currentUserName)
            .build();

        JsonObject messageThree = Json.createObjectBuilder()
            .add("channel", "private")
            .add("timestamp", 3)
            .add("message", "Test Nachicht")
            .add("from", userNameOne)
            .add("to", currentUserName)
            .build();

        currentUserCallback.handleMessage(messageOne);
        currentUserCallback.handleMessage(messageTwo);
        currentUserCallback.handleMessage(messageThree);

        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            NavBarUserElement userOneElement = robot.lookup("#" + userOne.getId() + "-button").query();
            NavBarUserElement userTwoElement = robot.lookup("#" + userTwo.getId() + "-button").query();
            Assertions.assertEquals(2, userOne.getSentUserNotification().getNotificationCounter());
            Assertions.assertEquals(1, userTwo.getSentUserNotification().getNotificationCounter());
        });
        robot.clickOn("#" + userOne.getId() + "-button");

        Assertions.assertNull(userOne.getSentUserNotification());
        Assertions.assertEquals(1, userTwo.getSentUserNotification().getNotificationCounter());
        currentUserCallback.handleMessage(messageOne);
        currentUserCallback.handleMessage(messageTwo);
        currentUserCallback.handleMessage(messageThree);

        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(2, userTwo.getSentUserNotification().getNotificationCounter());
        robot.clickOn("#home-button");
        robot.clickOn("#" + userTwo.getId() + "-UserListSideBarEntry");


        Assertions.assertThrows(EmptyNodeQueryException.class, () -> {
            robot.lookup("#" + userTwo.getId() + "-button").query();
        });
    }

    @Test
    public void testChannelNotification(FxRobot robot) {
        Editor editor = StageManager.getEditor();

        final User currentUser = new User().setName("Test").setId("123-45");
        final User userOne = new User().setName("userOne").setId("111-11");
        final User userTwo = new User().setName("userTwo").setId("222-22");
        final String serverId = "1";
        final String serverName = "Test";

        JSONObject j = new JSONObject().put("status", "success").put("message", "")
            .put("data", new JSONObject().put("id", "1").put("name", "Test"));
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock).createServer(eq(serverName), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);

        NotificationService.register(userOne);
        NotificationService.register(userTwo);

        editor.getOrCreateAccord()
            .setCurrentUser(currentUser)
            .setUserKey("123-45");

        editor.getOrCreateAccord()
            .withOtherUsers(userOne);

        editor.getOrCreateAccord()
            .withOtherUsers(userTwo);

        currentUser.withAvailableServers(server);
        userOne.withAvailableServers(server);
        userTwo.withAvailableServers(server);

        final Channel channelOne = new Channel().setName("ChannelOne").setId("C1").withChannelMembers(currentUser, userOne, userTwo);
        final Channel channelTwo = new Channel().setName("ChannelTwo").setId("C2").withChannelMembers(currentUser, userTwo);
        final Category category = new Category().setId("Category1").setName("TestCategory").withChannels(channelOne, channelTwo);
        server.withCategories(category);
        server.withoutChannels(channelOne, channelTwo);

        NotificationService.register(channelOne);
        NotificationService.register(channelTwo);

        RouteArgs args = new RouteArgs().addArgument(":id", server.getId());
        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
        WaitForAsyncUtils.waitForFxEvents();

        WebSocketService.addServerWebSocket(server.getId());

        verify(webSocketMock, times(4)).inject(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();

        for (int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }
        WSCallback systemCallback = endpointCallbackHashmap.get(Constants.WS_USER_PATH + currentUser.getName() + Constants.WS_SERVER_CHAT_PATH + server.getId());

        JsonObject messageOne = Json.createObjectBuilder()
            .add("id", 1)
            .add("channel", channelOne.getId())
            .add("timestamp", 1)
            .add("from", "userOne")
            .add("text", "messageOne")
            .build();

        JsonObject messageTwo = Json.createObjectBuilder()
            .add("id", 2)
            .add("channel", channelOne.getId())
            .add("timestamp", 2)
            .add("from", "userTwo")
            .add("text", "messageTwo")
            .build();

        JsonObject messageThree = Json.createObjectBuilder()
            .add("id", 3)
            .add("channel", channelTwo.getId())
            .add("timestamp", 3)
            .add("from", "userTwo")
            .add("text", "messageThree")
            .build();

        systemCallback.handleMessage(messageOne);
        systemCallback.handleMessage(messageTwo);
        systemCallback.handleMessage(messageThree);

        Assertions.assertEquals(3, NotificationService.getServerNotificationCount(server));
        Assertions.assertEquals(2, NotificationService.getPublisherNotificationCount(channelOne));
        Assertions.assertEquals(1, NotificationService.getPublisherNotificationCount(channelTwo));

        RouteArgs routeArgs = new RouteArgs().addArgument(":id", "1").addArgument(":categoryId", "Category1").addArgument(":channelId", "C1");
        Platform.runLater(() -> Router.routeWithArgs(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER + Constants.ROUTE_CHANNEL, routeArgs));
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            ServerChannelElement queryChannelOne = robot.lookup("#" + channelOne.getId() + "-channelElement").query();
            ServerChannelElement queryChannelTwo = robot.lookup("#" + channelTwo.getId() + "-channelElement").query();
        });
        WaitForAsyncUtils.waitForFxEvents();
    }
}
