package de.uniks.stp.controller;

import de.uniks.stp.AccordApp;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.dagger.components.test.AppTestComponent;
import de.uniks.stp.dagger.components.test.SessionTestComponent;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.network.websocket.WSCallback;
import de.uniks.stp.network.websocket.WebSocketClientFactory;
import de.uniks.stp.network.websocket.WebSocketService;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
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
import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(ApplicationExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class SystemWebsocketTest {

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    @Captor
    private ArgumentCaptor<WSCallback> wsCallbackArgumentCaptor;

    private static HashMap<String, WSCallback> endpointCallbackHashmap;
    private AccordApp app;
    private Router router;
    private Editor editor;
    private User currentUser;
    private WebSocketClientFactory webSocketClientFactoryMock;
    private WebSocketService webSocketService;

    @Start
    public void start(Stage stage) {
        // start application
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

        webSocketClientFactoryMock = sessionTestComponent.getWebSocketClientFactory();
        webSocketService = sessionTestComponent.getWebsocketService();
        webSocketService.init();
    }

    @Test
    public void testUserJoined(FxRobot robot) {

        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_LIST_ONLINE_USERS));
        WaitForAsyncUtils.waitForFxEvents();

        verify(webSocketClientFactoryMock, times(2))
            .create(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();

        for (int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }
        WSCallback systemCallback = endpointCallbackHashmap.get(Constants.WS_SYSTEM_PATH);

        String testUserName = "testUser";
        JsonObject jsonObject = Json.createObjectBuilder()
            .add("action", "userJoined")
            .add("data",
                Json.createObjectBuilder()
                    .add("name", testUserName)
                    .add("id", "12345678")
                    .build()
            )
            .build();
        systemCallback.handleMessage(jsonObject);

        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(1, editor.getOrCreateAccord().getOtherUsers().size());
        Assertions.assertNotNull(editor.getOtherUser(testUserName));

        VBox onlineUsersContainer = robot.lookup("#online-users-container").query();
        Object[] userListEntryLabels = onlineUsersContainer.lookupAll("#user-list-entry-label").toArray();
        Assertions.assertEquals(1, userListEntryLabels.length);
        Assertions.assertEquals(testUserName, ((Label) userListEntryLabels[0]).getText());
    }

    @Test
    public void testUserLeft(FxRobot robot) {
        String otherUserName = "otherTestUser";
        String otherUserId = "12345678";
        editor.getOrCreateOtherUser(otherUserId, otherUserName);

        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_LIST_ONLINE_USERS));
        WaitForAsyncUtils.waitForFxEvents();

        // assert correct start situation
        Assertions.assertEquals(1, editor.getOrCreateAccord().getOtherUsers().size());
        Assertions.assertNotNull(editor.getOtherUser(otherUserName));

        // prepare receiving a WebSocket message
        verify(webSocketClientFactoryMock, times(2))
            .create(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();

        for (int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }
        WSCallback systemCallback = endpointCallbackHashmap.get(Constants.WS_SYSTEM_PATH);

        // receive userLeft message
        JsonObject jsonObject = Json.createObjectBuilder()
            .add("action", "userLeft")
            .add("data",
                Json.createObjectBuilder()
                    .add("name", otherUserName)
                    .add("id", otherUserId)
                    .build()
            )
            .build();
        systemCallback.handleMessage(jsonObject);

        WaitForAsyncUtils.waitForFxEvents();

        // check for correct reactions
        Assertions.assertEquals(0, editor.getOrCreateAccord().getOtherUsers().size());
        Assertions.assertNull(editor.getOtherUser(otherUserName));

        VBox onlineUsersContainer = robot.lookup("#online-users-container").query();
        int userListLength = onlineUsersContainer.lookupAll("#user-list-entry-text").toArray().length;
        Assertions.assertEquals(0, userListLength);
    }

    @Test
    public void testServerUserJoinedLeftMessage(FxRobot robot) {
        final String SERVER_ID = "server";
        final String SERVER_NAME = "server-name";
        String TEST_USER_1_NAME = "testUser";
        String TEST_USER_2_NAME = "testUser2";
        String TEST_USER_1_ID = "1";
        String TEST_USER_2_ID = "2";

        Server server = editor.getOrCreateServer(SERVER_ID, SERVER_NAME);
        editor.getOrCreateServerMember(TEST_USER_1_ID, TEST_USER_1_NAME, server).setStatus(false);
        editor.getOrCreateServerMember(TEST_USER_2_ID, TEST_USER_2_NAME, server).setStatus(true);

        webSocketService.addServerWebSocket(SERVER_ID);

        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, new RouteArgs().addArgument(":id", SERVER_ID)));
        WaitForAsyncUtils.waitForFxEvents();

        verify(webSocketClientFactoryMock, times(4))
            .create(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();

        for (int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }
        WSCallback systemCallback = endpointCallbackHashmap.get(Constants.WS_SYSTEM_PATH + Constants.WS_SERVER_SYSTEM_PATH + SERVER_ID);

        VBox onlineUserContainer = robot.lookup("#online-user-list").query();
        VBox offlineUserContainer = robot.lookup("#offline-user-list").query();
        Assertions.assertEquals(2, onlineUserContainer.getChildren().size());
        Assertions.assertEquals(1, offlineUserContainer.getChildren().size());

        JsonObject jsonObject = Json.createObjectBuilder()
            .add("action", "userJoined")
            .add("data",
                Json.createObjectBuilder()
                    .add("name", TEST_USER_1_NAME)
                    .add("id", TEST_USER_1_ID)
                    .add("serverId", SERVER_ID)
                    .build()
            )
            .build();
        systemCallback.handleMessage(jsonObject);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(3, onlineUserContainer.getChildren().size());
        Assertions.assertEquals(0, offlineUserContainer.getChildren().size());

        jsonObject = Json.createObjectBuilder()
            .add("action", "userLeft")
            .add("data",
                Json.createObjectBuilder()
                    .add("name", TEST_USER_2_NAME)
                    .add("id", TEST_USER_2_ID)
                    .add("serverId", SERVER_ID)
                    .build()
            )
            .build();
        systemCallback.handleMessage(jsonObject);
        WaitForAsyncUtils.waitForFxEvents();

        List<User> users = editor.getOrCreateServer(SERVER_ID, SERVER_NAME).getUsers();
        Assertions.assertEquals(3, users.size());

        Assertions.assertEquals(2, onlineUserContainer.getChildren().size());
        Assertions.assertEquals(1, offlineUserContainer.getChildren().size());

        jsonObject = Json.createObjectBuilder()
            .add("action", "userLeft")
            .add("data",
                Json.createObjectBuilder()
                    .add("name", TEST_USER_1_NAME)
                    .add("id", TEST_USER_1_ID)
                    .add("serverId", SERVER_ID)
                    .build()
            )
            .build();
        systemCallback.handleMessage(jsonObject);

        WaitForAsyncUtils.waitForFxEvents();

        onlineUserContainer = robot.lookup("#online-user-list").query();
        offlineUserContainer = robot.lookup("#offline-user-list").query();
        Assertions.assertEquals(1, onlineUserContainer.getChildren().size());
        Assertions.assertEquals(2, offlineUserContainer.getChildren().size());
    }

    @AfterEach
    void tear(){
        stringArgumentCaptor = null;
        webSocketService = null;
        webSocketClientFactoryMock = null;
        currentUser = null;
        router = null;
        editor = null;
        wsCallbackArgumentCaptor = null;
        endpointCallbackHashmap = null;
        Platform.runLater(app::stop);
        WaitForAsyncUtils.waitForFxEvents();
        app = null;
    }
}
