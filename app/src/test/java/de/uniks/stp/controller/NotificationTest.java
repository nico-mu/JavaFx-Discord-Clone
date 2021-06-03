package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.StageManager;
import de.uniks.stp.component.NavBarUserElement;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.network.WSCallback;
import de.uniks.stp.network.WebSocketClient;
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
import org.mockito.internal.matchers.Not;
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

    @Mock
    private HttpResponse<JsonNode> catRes;

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> catCallbackCaptor;

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

        NotificationService.register(userOne);
        NotificationService.register(userTwo);


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
            Assertions.assertEquals(2, NotificationService.getPublisherNotificationCount(userOne));
            Assertions.assertEquals(1, NotificationService.getPublisherNotificationCount(userTwo));
        });
        robot.clickOn("#" + userOne.getId() + "-button");

        Assertions.assertEquals(0, NotificationService.getPublisherNotificationCount(userOne));
        Assertions.assertEquals(1, NotificationService.getPublisherNotificationCount(userTwo));
        currentUserCallback.handleMessage(messageOne);
        currentUserCallback.handleMessage(messageTwo);
        currentUserCallback.handleMessage(messageThree);

        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(2, NotificationService.getPublisherNotificationCount(userTwo));
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
        NotificationService.register(userOne);
        NotificationService.register(userTwo);
        final String serverName = "Test";
        final String serverId = "1";
        final String categoryName = "Cat";
        final String categoryId = "Category1";
        final String channelOneId = "C1";
        final String channelTwoId = "C2";

        editor.getOrCreateAccord()
            .setCurrentUser(currentUser)
            .setUserKey("123-45");

        editor.getOrCreateAccord()
            .withOtherUsers(userOne);

        editor.getOrCreateAccord()
            .withOtherUsers(userTwo);

        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME));
        WaitForAsyncUtils.waitForFxEvents();

        Server server = editor.getOrCreateServer(serverId, serverName);
        Category category = editor.getOrCreateCategory(categoryId, categoryName, server);
        Channel channel = editor.getOrCreateChannel(channelOneId, "ChannelOne", category);
        server.withCategories(category);
        server.withChannels(channel);
        NotificationService.register(channel);

        currentUser.withAvailableServers(server);
        userOne.withAvailableServers(server);
        userTwo.withAvailableServers(server);

        ArrayList<JSONObject> data = new ArrayList<>();
        ArrayList<String> user1 = new ArrayList<>();
        ArrayList<String> user2 = new ArrayList<>();
        user1.add(userOne.getId());
        user1.add(userTwo.getId());
        user1.add(currentUser.getId());
        user2.add(userOne.getId());
        user2.add(userTwo.getId());
        data.add(new JSONObject().put("id", channelOneId).put("name", "Channel1").put("type", "text").put("privileged", false).put("category", categoryId).put("members", user1));
        data.add(new JSONObject().put("id", channelTwoId).put("name", "Channel2").put("type", "text").put("privileged", true).put("category", categoryId).put("members", user2));
        JSONObject j = new JSONObject().put("status", "success").put("message", "")
            .put("data", data);
        ArrayList<JSONObject> dataCat = new ArrayList<>();
        ArrayList<String> channels = new ArrayList<>();
        channels.add(channelOneId);
        channels.add(channelTwoId);
        dataCat.add(new JSONObject().put("id", categoryId).put("name", categoryName).put("server", serverId).put("channels", channels));
        JSONObject jCat = new JSONObject().put("status", "success").put("message", "").put("data", dataCat);


        verify(webSocketMock, times(4)).inject(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();

        for (int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }
        WSCallback systemCallback = endpointCallbackHashmap.get(Constants.WS_USER_PATH + currentUser.getName() + Constants.WS_SERVER_CHAT_PATH + server.getId());

        JsonObject messageOne = Json.createObjectBuilder()
            .add("id", 1)
            .add("channel", channelOneId)
            .add("timestamp", 1)
            .add("from", "userOne")
            .add("text", "messageOne")
            .build();

        JsonObject messageTwo = Json.createObjectBuilder()
            .add("id", 2)
            .add("channel", channelTwoId)
            .add("timestamp", 2)
            .add("from", "userTwo")
            .add("text", "messageTwo")
            .build();

        JsonObject messageThree = Json.createObjectBuilder()
            .add("id", 3)
            .add("channel", channelTwoId)
            .add("timestamp", 3)
            .add("from", "userTwo")
            .add("text", "messageThree")
            .build();

        systemCallback.handleMessage(messageOne);
        systemCallback.handleMessage(messageTwo);
        systemCallback.handleMessage(messageThree);

        Assertions.assertEquals(3, NotificationService.getServerNotificationCount(server));
        for (Channel c : server.getChannels()) {
            if (c.getId().equals(channelOneId)) {
                Assertions.assertEquals(1, NotificationService.getPublisherNotificationCount(c));
            }
            if (c.getId().equals(channelTwoId)) {
                Assertions.assertEquals(2, NotificationService.getPublisherNotificationCount(c));
            }
        }

        RouteArgs routeArgs = new RouteArgs().addArgument(":id", serverId).addArgument(":categoryId", categoryId).addArgument(":channelId", channelOneId);
        Platform.runLater(() -> Router.routeWithArgs(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER + Constants.ROUTE_CHANNEL, routeArgs));
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(2, NotificationService.getServerNotificationCount(server));
        for (Channel c : server.getChannels()) {
            if (c.getId().equals(channelOneId)) {
                Assertions.assertEquals(0, NotificationService.getPublisherNotificationCount(c));
            }
            if (c.getId().equals(channelTwoId)) {
                Assertions.assertEquals(2, NotificationService.getPublisherNotificationCount(c));
            }
        }

        RouteArgs routeArgsChannelTwo = new RouteArgs().addArgument(":id", serverId).addArgument(":categoryId", categoryId).addArgument(":channelId", channelTwoId);
        Platform.runLater(() -> Router.routeWithArgs(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER + Constants.ROUTE_CHANNEL, routeArgsChannelTwo));
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(0, NotificationService.getServerNotificationCount(server));
        for (Channel c : server.getChannels()) {
            if (c.getId().equals(channelOneId)) {
                Assertions.assertEquals(0, NotificationService.getPublisherNotificationCount(c));
            }
            if (c.getId().equals(channelTwoId)) {
                Assertions.assertEquals(0, NotificationService.getPublisherNotificationCount(c));
            }
        }

        systemCallback.handleMessage(messageOne);
        systemCallback.handleMessage(messageTwo);
        systemCallback.handleMessage(messageThree);

        Assertions.assertEquals(1, NotificationService.getServerNotificationCount(server));
        for (Channel c : server.getChannels()) {
            if (c.getId().equals(channelOneId)) {
                Assertions.assertEquals(1, NotificationService.getPublisherNotificationCount(c));
            }
            if (c.getId().equals(channelTwoId)) {
                Assertions.assertEquals(0, NotificationService.getPublisherNotificationCount(c));
            }
        }

        when(catRes.getBody()).thenReturn(new JsonNode(jCat.toString()));
        when(catRes.isSuccess()).thenReturn(true);

        verify(restMock).getCategories(eq(serverId), catCallbackCaptor.capture());
        Callback<JsonNode> catCallback = catCallbackCaptor.getValue();
        catCallback.completed(catRes);
        WaitForAsyncUtils.waitForFxEvents();

        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock).getChannels(eq(serverId), eq(categoryId), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();
    }
}
