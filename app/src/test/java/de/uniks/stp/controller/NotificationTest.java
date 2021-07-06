package de.uniks.stp.controller;

import com.jfoenix.controls.JFXToggleButton;
import de.uniks.stp.AccordApp;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.dagger.components.test.AppTestComponent;
import de.uniks.stp.dagger.components.test.SessionTestComponent;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.network.websocket.WSCallback;
import de.uniks.stp.network.websocket.WebSocketClientFactory;
import de.uniks.stp.notification.NotificationService;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.stage.Stage;
import kong.unirest.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(ApplicationExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class NotificationTest {

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    @Captor
    private ArgumentCaptor<WSCallback> wsCallbackArgumentCaptor;

    private HashMap<String, WSCallback> endpointCallbackHashmap;

    private AccordApp app;
    private Router router;
    private Editor editor;
    private User currentUser;
    private NotificationService notificationService;
    private WebSocketClientFactory webSocketClientFactoryMock;
    private SessionDatabaseService sessionDatabaseService;

    @Start
    public void start(Stage stage) {
        endpointCallbackHashmap = new HashMap<>();
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

        notificationService = sessionTestComponent.getNotificationService();
        webSocketClientFactoryMock = sessionTestComponent.getWebSocketClientFactory();
        sessionDatabaseService = sessionTestComponent.getSessionDatabaseService();
        sessionDatabaseService.clearAllConversations();
        sessionTestComponent.getWebsocketService().init();
    }

    @Test
    public void testPrivateMessageNotification(FxRobot robot) {
        final String userNameOne = "UserOne";
        final String userNameTwo = "UserTwo";

        final User userOne = new User().setName(userNameOne).setId("111-11");
        final User userTwo = new User().setName(userNameTwo).setId("222-22");

        editor.getOrCreateAccord()
            .withOtherUsers(userOne);

        editor.getOrCreateAccord()
            .withOtherUsers(userTwo);

        notificationService.register(userOne);
        notificationService.register(userTwo);


        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME));
        WaitForAsyncUtils.waitForFxEvents();

        verify(webSocketClientFactoryMock, times(2))
            .create(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();

        for (int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }
        WSCallback currentUserCallback = endpointCallbackHashmap.get(Constants.WS_USER_PATH + currentUser.getName());
        JsonObject messageOne = Json.createObjectBuilder()
            .add("channel", "private")
            .add("timestamp", 1)
            .add("message", "Test Nachicht 1")
            .add("from", userNameOne)
            .add("to", currentUser.getName())
            .build();

        JsonObject messageTwo = Json.createObjectBuilder()
            .add("channel", "private")
            .add("timestamp", 2)
            .add("message", "Test Nachicht 2")
            .add("from", userNameTwo)
            .add("to", currentUser.getName())
            .build();

        JsonObject messageThree = Json.createObjectBuilder()
            .add("channel", "private")
            .add("timestamp", 3)
            .add("message", "Test Nachicht")
            .add("from", userNameOne)
            .add("to", currentUser.getName())
            .build();

        currentUserCallback.handleMessage(messageOne);
        currentUserCallback.handleMessage(messageTwo);
        currentUserCallback.handleMessage(messageThree);

        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            Assertions.assertEquals(2, notificationService.getPublisherNotificationCount(userOne));
            Assertions.assertEquals(1, notificationService.getPublisherNotificationCount(userTwo));
        });
        robot.clickOn("#" + userOne.getId() + "-button");

        Assertions.assertEquals(0, notificationService.getPublisherNotificationCount(userOne));
        Assertions.assertEquals(1, notificationService.getPublisherNotificationCount(userTwo));
        currentUserCallback.handleMessage(messageOne);
        currentUserCallback.handleMessage(messageTwo);
        currentUserCallback.handleMessage(messageThree);

        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(2, notificationService.getPublisherNotificationCount(userTwo));
        robot.clickOn("#home-button");
        robot.clickOn("#" + userTwo.getId() + "-DirectMessageEntry");


        Assertions.assertThrows(EmptyNodeQueryException.class, () -> {
            robot.lookup("#" + userTwo.getId() + "-button").query();
        });
    }

    @Test
    public void testChannelNotification(FxRobot robot) {
        final User userOne = new User().setName("userOne").setId("111-11");
        final User userTwo = new User().setName("userTwo").setId("222-22");
        notificationService.register(userOne);
        notificationService.register(userTwo);

        final String serverName = "Test";
        final String serverId = "1";
        final String categoryName = "Cat";
        final String categoryId = "Category1";
        final String channelOneId = "C1";
        final String channelTwoId = "C2";

        editor.getOrCreateAccord()
            .withOtherUsers(userOne);

        editor.getOrCreateAccord()
            .withOtherUsers(userTwo);

        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME));
        WaitForAsyncUtils.waitForFxEvents();

        Server server = editor.getOrCreateServer(serverId, serverName);
        Category category = editor.getOrCreateCategory(categoryId, categoryName, server);
        Channel channel = editor.getOrCreateChannel(channelOneId, "ChannelOne", "text", category);
        server.withCategories(category);
        Channel channel2 = editor.getOrCreateChannel(channelTwoId, "Channel2", "text", category);
        server.withChannels(channel2);
        server.withChannels(channel);

        notificationService.register(channel);
        notificationService.register(channel2);

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

        data.add(new JSONObject()
            .put("id", channelOneId)
            .put("name", "Channel1")
            .put("type", "text")
            .put("privileged", false)
            .put("category", categoryId)
            .put("members", user1)
        );

        data.add(new JSONObject()
            .put("id", channelTwoId)
            .put("name", "Channel2")
            .put("type", "text")
            .put("privileged", true)
            .put("category", categoryId)
            .put("members", user2)
        );

        JSONObject j = new JSONObject()
            .put("status", "success")
            .put("message", "")
            .put("data", data);

        verify(webSocketClientFactoryMock, times(4))
            .create(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

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

        Assertions.assertEquals(3, notificationService.getServerNotificationCount(server));
        for (Channel c : server.getChannels()) {
            if (c.getId().equals(channelOneId)) {
                Assertions.assertEquals(1, notificationService.getPublisherNotificationCount(c));
            }
            if (c.getId().equals(channelTwoId)) {
                Assertions.assertEquals(2, notificationService.getPublisherNotificationCount(c));
            }
        }

        RouteArgs routeArgs = new RouteArgs().addArgument(":id", serverId).addArgument(":categoryId", categoryId).addArgument(":channelId", channelOneId);
        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER + Constants.ROUTE_CHANNEL, routeArgs));
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(2, notificationService.getServerNotificationCount(server));
        for (Channel c : server.getChannels()) {
            if (c.getId().equals(channelOneId)) {
                Assertions.assertEquals(0, notificationService.getPublisherNotificationCount(c));
            }
            if (c.getId().equals(channelTwoId)) {
                Assertions.assertEquals(2, notificationService.getPublisherNotificationCount(c));
            }
        }

        RouteArgs routeArgsChannelTwo = new RouteArgs().addArgument(":id", serverId).addArgument(":categoryId", categoryId).addArgument(":channelId", channelTwoId);
        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER + Constants.ROUTE_CHANNEL, routeArgsChannelTwo));
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(0, notificationService.getServerNotificationCount(server));
        for (Channel c : server.getChannels()) {
            if (c.getId().equals(channelOneId)) {
                Assertions.assertEquals(0, notificationService.getPublisherNotificationCount(c));
            }
            if (c.getId().equals(channelTwoId)) {
                Assertions.assertEquals(0, notificationService.getPublisherNotificationCount(c));
            }
        }

        systemCallback.handleMessage(messageOne);
        systemCallback.handleMessage(messageTwo);
        systemCallback.handleMessage(messageThree);

        Assertions.assertEquals(1, notificationService.getServerNotificationCount(server));
        for (Channel c : server.getChannels()) {
            if (c.getId().equals(channelOneId)) {
                Assertions.assertEquals(1, notificationService.getPublisherNotificationCount(c));
            }
            if (c.getId().equals(channelTwoId)) {
                Assertions.assertEquals(0, notificationService.getPublisherNotificationCount(c));
            }
        }
    }

    @Test
    public void muteChannelTest(FxRobot robot) {

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

        String channelId = "chId1234";
        String channelName = "testChannel";
        Channel channel = new Channel().setName(channelName).setId(channelId).setPrivileged(false).setType("text");
        category.withChannels(channel);
        testServer.withChannels(channel);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(1, editor.getServer(serverId).getCategories().get(0).getChannels().size());
        sessionDatabaseService.removeMutedChannelId(channelId);
        Assertions.assertFalse(sessionDatabaseService.isChannelMuted(channelId));

        robot.clickOn("#channel-container");
        robot.point("#channel-container");
        robot.point("#edit-channel");
        robot.clickOn("#edit-channel");

        JFXToggleButton notificationToggleButton =  robot.lookup("#notifications-toggle-button").query();
        Assertions.assertTrue(notificationToggleButton.isSelected());

        robot.clickOn("#notifications-toggle-button");
        robot.clickOn("#edit-channel-create-button");

        Assertions.assertTrue(sessionDatabaseService.isChannelMuted(channelId));

        robot.clickOn("#channel-container");
        robot.point("#channel-container");
        robot.point("#edit-channel");
        robot.clickOn("#edit-channel");

        robot.clickOn("#notifications-toggle-button");
        robot.clickOn("#edit-channel-create-button");

        Assertions.assertFalse(sessionDatabaseService.isChannelMuted(channelId));
    }

    @Test
    public void muteCategoryTest(FxRobot robot) {
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

        new Category()
            .setName(categoryName)
            .setId(categoryId)
            .setServer(testServer);
        WaitForAsyncUtils.waitForFxEvents();

        sessionDatabaseService.removeMutedCategoryId(categoryId);
        Assertions.assertFalse(sessionDatabaseService.isCategoryMuted(categoryId));

        robot.clickOn("#" + categoryId + "-ServerCategoryElementLabel");
        robot.point("#edit-category-gear");
        robot.clickOn("#edit-category-gear");

        JFXToggleButton notificationToggleButton =  robot.lookup("#notifications-toggle-button").query();
        Assertions.assertTrue(notificationToggleButton.isSelected());

        robot.clickOn("#notifications-toggle-button");
        robot.clickOn("#save-button");

        Assertions.assertTrue(sessionDatabaseService.isCategoryMuted(categoryId));

        robot.clickOn("#" + categoryId + "-ServerCategoryElementLabel");
        robot.point("#edit-category-gear");
        robot.clickOn("#edit-category-gear");

        Assertions.assertFalse(notificationToggleButton.isSelected());

        robot.clickOn("#notifications-toggle-button");
        robot.clickOn("#save-button");

        Assertions.assertFalse(sessionDatabaseService.isCategoryMuted(categoryId));
    }

    @Test
    public void muteServerTest(FxRobot robot) {
        String serverName = "TestServer";
        String serverId = "12345678";
        Server testServer = new Server().setName(serverName).setId(serverId);

        editor.getOrCreateAccord()
            .getCurrentUser()
            .withAvailableServers(testServer);

        RouteArgs args = new RouteArgs().addArgument(":id", serverId);
        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
        WaitForAsyncUtils.waitForFxEvents();

        sessionDatabaseService.removeMutedServerId(serverId);
        Assertions.assertFalse(sessionDatabaseService.isServerMuted(serverId));

        robot.clickOn("#settings-label");
        robot.clickOn("#edit-menu-item");

        JFXToggleButton notificationToggleButton =  robot.lookup("#notifications-toggle-button").query();
        Assertions.assertTrue(notificationToggleButton.isSelected());

        robot.clickOn("#notifications-toggle-button");
        robot.clickOn("#save-button");

        Assertions.assertTrue(sessionDatabaseService.isServerMuted(serverId));

        robot.clickOn("#settings-label");
        robot.clickOn("#edit-menu-item");

        Assertions.assertFalse(notificationToggleButton.isSelected());

        robot.clickOn("#notifications-toggle-button");
        robot.clickOn("#save-button");

        Assertions.assertFalse(sessionDatabaseService.isServerMuted(serverId));
    }

    @AfterEach
    void tear(){
        router = null;
        webSocketClientFactoryMock = null;
        sessionDatabaseService = null;
        notificationService = null;
        editor = null;
        currentUser = null;
        stringArgumentCaptor = null;
        wsCallbackArgumentCaptor = null;
        endpointCallbackHashmap = null;
        Platform.runLater(app::stop);
        WaitForAsyncUtils.waitForFxEvents();
        app = null;
    }
}
