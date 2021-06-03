package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.StageManager;
import de.uniks.stp.component.NavBarHomeElement;
import de.uniks.stp.component.NavBarUserElement;
import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.model.User;
import de.uniks.stp.network.*;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
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

import javax.json.*;
import java.util.*;
import java.util.List;

import static org.mockito.Mockito.*;


@ExtendWith(ApplicationExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class SystemWebsocketTest {
    @Mock
    private RestClient restMock;

    @Mock
    private WebSocketClient webSocketMock;

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

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
    public void testUserJoined(FxRobot robot) {
        DatabaseService.clearAllConversations();

        Editor editor = StageManager.getEditor();

        editor.getOrCreateAccord()
            .setCurrentUser(new User().setName("Test"))
            .setUserKey("123-45");

        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_LIST_ONLINE_USERS));
        WaitForAsyncUtils.waitForFxEvents();

        verify(webSocketMock, times(2)).inject(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

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
        Object[] userListEntryLabels = onlineUsersContainer.lookupAll("#user-list-entry-text").toArray();
        Assertions.assertEquals(1, userListEntryLabels.length);
        Assertions.assertEquals(testUserName, ((Text) userListEntryLabels[0]).getText());
    }

    @Test
    public void testUserLeft(FxRobot robot) {
        // prepare start situation
        Editor editor = StageManager.getEditor();
        DatabaseService.clearAllConversations();

        editor.getOrCreateAccord()
            .setCurrentUser(new User().setName("Test"))
            .setUserKey("123-45");

        String otherUserName = "otherTestUser";
        String otherUserId = "12345678";
        editor.getOrCreateOtherUser(otherUserId, otherUserName);

        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_LIST_ONLINE_USERS));
        WaitForAsyncUtils.waitForFxEvents();

        // assert correct start situation
        Assertions.assertEquals(1, editor.getOrCreateAccord().getOtherUsers().size());
        Assertions.assertNotNull(editor.getOtherUser(otherUserName));

        // prepare receiving a WebSocket message
        verify(webSocketMock, times(2)).inject(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

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
        DatabaseService.clearAllConversations();
        final String SERVER_ID = "server";
        final String SERVER_NAME = "server-name";

        Editor editor = StageManager.getEditor();

        editor.getOrCreateAccord()
            .setCurrentUser(new User().setName("Test"))
            .setUserKey("123-45");

        editor.getOrCreateServer(SERVER_ID, SERVER_NAME);

        WebSocketService.addServerWebSocket(SERVER_ID);

        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, new RouteArgs().addArgument(":id", SERVER_ID)));
        WaitForAsyncUtils.waitForFxEvents();

        verify(webSocketMock, times(4)).inject(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();

        for (int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }
        WSCallback systemCallback = endpointCallbackHashmap.get(Constants.WS_SYSTEM_PATH + Constants.WS_SERVER_SYSTEM_PATH + SERVER_ID);

        String testUserName = "testUser";
        JsonObject jsonObject = Json.createObjectBuilder()
            .add("action", "userJoined")
            .add("data",
                Json.createObjectBuilder()
                    .add("name", testUserName)
                    .add("id", "12345678")
                    .add("serverId", SERVER_ID)
                    .build()
            )
            .build();
        systemCallback.handleMessage(jsonObject);
        String testUserName2 = "testUser2";
        jsonObject = Json.createObjectBuilder()
            .add("action", "userLeft")
            .add("data",
                Json.createObjectBuilder()
                    .add("name", testUserName2)
                    .add("id", "123456789")
                    .add("serverId", SERVER_ID)
                    .build()
            )
            .build();
        systemCallback.handleMessage(jsonObject);

        WaitForAsyncUtils.waitForFxEvents();

        List<User> users = editor.getOrCreateServer(SERVER_ID, SERVER_NAME).getUsers();
        Assertions.assertEquals(3, users.size());

        VBox onlineUserContainer = robot.lookup("#online-user-list").query();
        VBox offlineUserContainer = robot.lookup("#offline-user-list").query();
        Assertions.assertEquals(1, onlineUserContainer.getChildren().size());
        Assertions.assertEquals(1, offlineUserContainer.getChildren().size());

        jsonObject = Json.createObjectBuilder()
            .add("action", "userLeft")
            .add("data",
                Json.createObjectBuilder()
                    .add("name", testUserName)
                    .add("id", "12345678")
                    .add("serverId", SERVER_ID)
                    .build()
            )
            .build();
        systemCallback.handleMessage(jsonObject);

        WaitForAsyncUtils.waitForFxEvents();

        onlineUserContainer = robot.lookup("#online-user-list").query();
        offlineUserContainer = robot.lookup("#offline-user-list").query();
        Assertions.assertEquals(0, onlineUserContainer.getChildren().size());
        Assertions.assertEquals(2, offlineUserContainer.getChildren().size());
    }
}
