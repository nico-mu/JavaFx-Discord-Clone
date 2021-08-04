package de.uniks.stp.message;

import de.uniks.stp.AccordApp;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.dagger.components.test.AppTestComponent;
import de.uniks.stp.dagger.components.test.SessionTestComponent;
import de.uniks.stp.model.*;
import de.uniks.stp.network.websocket.WSCallback;
import de.uniks.stp.network.websocket.WebSocketClientFactory;
import de.uniks.stp.network.websocket.WebSocketService;
import de.uniks.stp.notification.NotificationService;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.stage.Stage;
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
import org.testfx.util.WaitForAsyncUtils;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class CopyMessageTest {

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
    private WebSocketService webSocketService;

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
        webSocketService = sessionTestComponent.getWebsocketService();
        webSocketService.init();
    }

    /**
     * Tests receiving message and copying it then (tests for correct String stored in clipboard)
     * @param robot
     */
    @Test
    public void testCopyMessage(FxRobot robot) {
        // create start situation
        final User otherUser = new User().setName("otherUser").setId("111-11");
        notificationService.register(otherUser);
        editor.getOrCreateAccord().withOtherUsers(otherUser);

        final String serverName = "Test";
        final String serverId = "1";
        final String categoryName = "Cat";
        final String categoryId = "Category1";
        final String channelOneId = "C1";

        Server server = editor.getOrCreateServer(serverId, serverName);
        Category category = editor.getOrCreateCategory(categoryId, categoryName, server);
        Channel channel = editor.getOrCreateChannel(channelOneId, "ChannelOne", "text", category);
        server.withCategories(category);
        server.withChannels(channel);

        RouteArgs args = new RouteArgs().addArgument(":id", serverId);
        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
        WaitForAsyncUtils.waitForFxEvents();

        webSocketService.addServerWebSocket(serverId);
        notificationService.register(channel);
        currentUser.withAvailableServers(server);
        otherUser.withAvailableServers(server);

        RouteArgs routeArgs = new RouteArgs()
            .addArgument(":id", serverId)
            .addArgument(":categoryId", categoryId)
            .addArgument(":channelId", channelOneId);
        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER + Constants.ROUTE_CHANNEL, routeArgs));
        WaitForAsyncUtils.waitForFxEvents();

        // prepare recveiving message
        verify(webSocketClientFactoryMock, times(4))
            .create(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());
        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();

        for (int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }
        WSCallback systemCallback = endpointCallbackHashmap
            .get(Constants.WS_USER_PATH + currentUser.getName() + Constants.WS_SERVER_CHAT_PATH + server.getId());

        // recveive message
        Date date = new Date();
        String dateMsg = "Time: " + date.toString() + ", as long: " + date.getTime();

        JsonObject message = Json.createObjectBuilder()
            .add("id", 1)
            .add("channel", channelOneId)
            .add("timestamp", 1)
            .add("from", "otherUser")
            .add("text", dateMsg)
            .build();
        systemCallback.handleMessage(message);

        // check for message not copied yet
        Platform.runLater(() -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            Assertions.assertNotEquals(dateMsg, clipboard.getContent(DataFormat.PLAIN_TEXT));
        });

        // copy message
        ServerMessage msg = server.getChannels().get(0).getMessages().last();
        String messageId = msg.getId();

        robot.point("#message-text-" + messageId);
        ImageView button = robot.lookup("#copy-message-" + messageId).query();
        robot.clickOn(button);
        WaitForAsyncUtils.waitForFxEvents();

        // check for message copied now
        Platform.runLater(() -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            Assertions.assertEquals(dateMsg, clipboard.getContent(DataFormat.PLAIN_TEXT));
        });
    }

    @AfterEach
    void tear() {
        webSocketClientFactoryMock = null;
        router = null;
        editor = null;
        currentUser = null;
        notificationService = null;
        stringArgumentCaptor = null;
        wsCallbackArgumentCaptor = null;
        webSocketService = null;
        endpointCallbackHashmap = null;
        Platform.runLater(app::stop);
        WaitForAsyncUtils.waitForFxEvents();
        app = null;
    }
}
