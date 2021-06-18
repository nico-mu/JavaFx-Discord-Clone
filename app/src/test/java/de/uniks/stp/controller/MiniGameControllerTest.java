package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.StageManager;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.emote.EmoteTextArea;
import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.jpa.model.DirectMessageDTO;
import de.uniks.stp.model.User;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.network.WSCallback;
import de.uniks.stp.network.WebSocketClient;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
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
import org.testfx.api.FxRobotException;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.service.query.EmptyNodeQueryException;
import org.testfx.service.query.NodeQuery;
import org.testfx.util.WaitForAsyncUtils;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(ApplicationExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class MiniGameControllerTest {
    @Mock
    private RestClient restMock;

    @Mock
    private WebSocketClient webSocketMock;

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
        DatabaseService.clearAllConversations();
    }

    private String generateRandomString() {
        return UUID.randomUUID().toString();
    }

    @Test
    public void testMiniGameStart(FxRobot robot) {
        Editor editor = StageManager.getEditor();

        User currentUser = new User().setName(generateRandomString()).setId(generateRandomString());
        User otherUser = new User().setName(generateRandomString()).setId(generateRandomString());

        editor.getOrCreateAccord()
            .setCurrentUser(currentUser)
            .setUserKey(generateRandomString())
            .withOtherUsers(otherUser);

        Platform.runLater(() -> Router.route(
            Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_PRIVATE_CHAT,
            new RouteArgs().addArgument(Constants.ROUTE_PRIVATE_CHAT_ARGS, otherUser.getId())));
        WaitForAsyncUtils.waitForFxEvents();

        verify(webSocketMock, times(2)).inject(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());
        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();
        for (int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }
        WSCallback currentUserCallback = endpointCallbackHashmap.get(Constants.WS_USER_PATH + currentUser.getName());

        // Other user initializes the game
        JsonObject message = Json.createObjectBuilder()
            .add("channel", "private")
            .add("timestamp", new Date().getTime())
            .add("message", MiniGameController.GameCommand.PLAY.command)
            .add("from", otherUser.getName())
            .add("to", currentUser.getName())
            .build();
        currentUserCallback.handleMessage(message);
        WaitForAsyncUtils.waitForFxEvents();

        VBox chatViewMessageInput = robot.lookup("#chatViewMessageInput").query();
        EmoteTextArea messageInput = (EmoteTextArea) (((VirtualizedScrollPane<?>) chatViewMessageInput.getChildren().get(0))).getContent();

        Platform.runLater(() -> {
            messageInput.clear();
            messageInput.appendText(MiniGameController.GameCommand.PLAY.command);
        });
        robot.clickOn("#chatViewSubmitButton");
        WaitForAsyncUtils.waitForFxEvents();

        // MiniGame should open
        Node query = robot.lookup("#paper-button").query();
        Assertions.assertNotNull(query);

        robot.clickOn("#paper-button");
        // Check for correct outgoing websocket message
        JsonObject sentObject = Json.createObjectBuilder()
            .add("channel", "private")
            .add("to", otherUser.getName())
            .add("message", MiniGameController.GameCommand.CHOOSE_PAPER.command)
            .build();
        try {
            verify(webSocketMock).sendMessage(sentObject.toString());
        } catch (IOException e) {
            Assertions.fail();
        }

        // get choose message of opponent
        message = Json.createObjectBuilder()
            .add("channel", "private")
            .add("timestamp", new Date().getTime())
            .add("message", MiniGameController.GameCommand.CHOOSE_SCISSOR.command)
            .add("from", otherUser.getName())
            .add("to", currentUser.getName())
            .build();
        currentUserCallback.handleMessage(message);
        WaitForAsyncUtils.waitForFxEvents();

        Label actionLabel = robot.lookup("#action-label").query();

        Assertions.assertEquals(ViewLoader.loadLabel(Constants.LBL_RESULT_LOSS), actionLabel.getText());

        robot.clickOn("#revanche-button");
        // Check for correct outgoing websocket message
        sentObject = Json.createObjectBuilder()
            .add("channel", "private")
            .add("to", otherUser.getName())
            .add("message", MiniGameController.GameCommand.REVANCHE.command)
            .build();
        try {
            verify(webSocketMock).sendMessage(sentObject.toString());
        } catch (IOException e) {
            Assertions.fail();
        }

        // get revanche message of opponent
        message = Json.createObjectBuilder()
            .add("channel", "private")
            .add("timestamp", new Date().getTime())
            .add("message", MiniGameController.GameCommand.REVANCHE.command)
            .add("from", otherUser.getName())
            .add("to", currentUser.getName())
            .build();
        currentUserCallback.handleMessage(message);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(ViewLoader.loadLabel(Constants.LBL_CHOOSE_ACTION), actionLabel.getText());

        robot.clickOn("#rock-button");
        // Check for correct outgoing websocket message
        sentObject = Json.createObjectBuilder()
            .add("channel", "private")
            .add("to", otherUser.getName())
            .add("message", MiniGameController.GameCommand.CHOOSE_ROCK.command)
            .build();
        try {
            verify(webSocketMock).sendMessage(sentObject.toString());
        } catch (IOException e) {
            Assertions.fail();
        }

        // get choose message of opponent
        message = Json.createObjectBuilder()
            .add("channel", "private")
            .add("timestamp", new Date().getTime())
            .add("message", MiniGameController.GameCommand.CHOOSE_SCISSOR.command)
            .add("from", otherUser.getName())
            .add("to", currentUser.getName())
            .build();
        currentUserCallback.handleMessage(message);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(ViewLoader.loadLabel(Constants.LBL_RESULT_WIN), actionLabel.getText());

        // Close mini game
        robot.clickOn("#cancel-button");
        // Check for correct outgoing websocket message
        sentObject = Json.createObjectBuilder()
            .add("channel", "private")
            .add("to", otherUser.getName())
            .add("message", MiniGameController.GameCommand.LEAVE.command)
            .build();
        try {
            verify(webSocketMock).sendMessage(sentObject.toString());
        } catch (IOException e) {
            Assertions.fail();
        }

        // Assert modal is closed
        Assertions.assertThrows(FxRobotException.class, () -> {
            robot.clickOn("#cancel-button");
        });
    }

    @AfterEach
    void tear(){
        restMock = null;
        webSocketMock = null;
        stringArgumentCaptor = null;
        wsCallbackArgumentCaptor = null;
        endpointCallbackHashmap = null;
        Platform.runLater(app::stop);
        WaitForAsyncUtils.waitForFxEvents();
        app = null;
    }
}
