package de.uniks.stp.controller;

import de.uniks.stp.AccordApp;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.component.EmoteTextArea;
import de.uniks.stp.dagger.components.test.AppTestComponent;
import de.uniks.stp.dagger.components.test.SessionTestComponent;
import de.uniks.stp.minigame.GameAction;
import de.uniks.stp.minigame.GameCommand;
import de.uniks.stp.modal.EasterEggModal;
import de.uniks.stp.model.User;
import de.uniks.stp.network.websocket.WSCallback;
import de.uniks.stp.network.websocket.WebSocketClientFactory;
import de.uniks.stp.network.websocket.WebSocketService;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.testfx.api.FxRobot;
import org.testfx.api.FxRobotException;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(ApplicationExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class MiniGameControllerTest {

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    @Captor
    private ArgumentCaptor<WSCallback> wsCallbackArgumentCaptor;

    private WebSocketClientFactory webSocketClientFactoryMock;
    private HashMap<String, WSCallback> endpointCallbackHashmap;
    private AccordApp app;
    private Router router;
    private Editor editor;
    private User currentUser;
    private ViewLoader viewLoader;
    private WebSocketService webSocketServiceSpy;

    @Start
    public void start(Stage stage) {
        endpointCallbackHashmap = new HashMap<>();
        // start application
        MockitoAnnotations.initMocks(this);
        app = new AccordApp();
        app.setTestMode(true);
        app.start(stage);

        AppTestComponent appTestComponent = (AppTestComponent) app.getAppComponent();
        router = appTestComponent.getRouter();
        editor = appTestComponent.getEditor();
        viewLoader = appTestComponent.getViewLoader();
        currentUser = editor.createCurrentUser(generateRandomString(), true).setId(generateRandomString());
        editor.setCurrentUser(currentUser);

        SessionTestComponent sessionTestComponent = appTestComponent
            .sessionTestComponentBuilder()
            .currentUser(currentUser)
            .userKey("123-45")
            .build();
        app.setSessionComponent(sessionTestComponent);

        sessionTestComponent.getSessionDatabaseService().clearAllConversations();
        sessionTestComponent.getWebsocketService().init();
        webSocketClientFactoryMock = sessionTestComponent.getWebSocketClientFactory();
        webSocketServiceSpy = sessionTestComponent.getWebsocketService();
    }

    private String generateRandomString() {
        return UUID.randomUUID().toString();
    }

    private JsonObject buildOpponentCommand(User otherUser, GameCommand action) {
        // get choose message of opponent
        return Json.createObjectBuilder()
            .add("channel", "private")
            .add("timestamp", new Date().getTime())
            .add("message", action.command)
            .add("from", otherUser.getName())
            .add("to", currentUser.getName())
            .build();
    }

    @Test
    public void testMiniGameStart(FxRobot robot) {
        User otherUser = new User().setName(generateRandomString()).setId(generateRandomString());
        editor.getOrCreateAccord()
            .withOtherUsers(otherUser);

        Platform.runLater(() -> router.route(
            Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_PRIVATE_CHAT,
            new RouteArgs().addArgument(Constants.ROUTE_PRIVATE_CHAT_ARGS, otherUser.getId())));
        WaitForAsyncUtils.waitForFxEvents();

        verify(webSocketClientFactoryMock, times(2))
            .create(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();
        for (int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }
        WSCallback currentUserCallback = endpointCallbackHashmap.get(Constants.WS_USER_PATH + currentUser.getName());

        // Other user initializes the game
        currentUserCallback.handleMessage(buildOpponentCommand(otherUser, GameCommand.PLAY));
        WaitForAsyncUtils.waitForFxEvents();

        VBox chatViewMessageInput = robot.lookup("#chatViewMessageInput").query();
        EmoteTextArea messageInput = (EmoteTextArea) (((VirtualizedScrollPane<?>) chatViewMessageInput.getChildren().get(0))).getContent();

        Platform.runLater(() -> {
            messageInput.clear();
            messageInput.appendText(GameCommand.PLAY.command);
        });
        messageInput.setHasPlaceholder(false);
        robot.clickOn("#chatViewSubmitButton");
        WaitForAsyncUtils.waitForFxEvents();

        // MiniGame should open
        Label actionLabel = robot.lookup(EasterEggModal.ACTION_LABEL).query();
        Assertions.assertNotNull(actionLabel);
        Assertions.assertEquals(viewLoader.loadLabel(Constants.LBL_CHOOSE_ACTION), actionLabel.getText());

        Button paperButton = robot.lookup(EasterEggModal.PAPER_BUTTON).query();
        Button scissorsButton = robot.lookup(EasterEggModal.SCISSORS_BUTTON).query();
        robot.clickOn(paperButton);

        // Check for correct outgoing websocket message
        verify(webSocketServiceSpy).sendPrivateMessage(otherUser.getName(), GameCommand.CHOOSE_PAPER.command);
        Assertions.assertEquals("-fx-background-color: " + EasterEggModal.SELECTED_ACTION_COLOR + ";", paperButton.getStyle());

        // get choose message of opponent
        currentUserCallback.handleMessage(buildOpponentCommand(otherUser, GameCommand.CHOOSE_SCISSOR));
        WaitForAsyncUtils.waitForFxEvents();

        // check for correct result
        Assertions.assertEquals("-fx-background-color: " + EasterEggModal.LOSS_COLOR + ";", paperButton.getStyle());

        robot.clickOn(paperButton);
        currentUserCallback.handleMessage(buildOpponentCommand(otherUser, GameCommand.CHOOSE_SCISSOR));
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn(paperButton);
        currentUserCallback.handleMessage(buildOpponentCommand(otherUser, GameCommand.CHOOSE_SCISSOR));
        WaitForAsyncUtils.waitForFxEvents();

        // check for loss
        Assertions.assertEquals("0 : 3" + ", " + viewLoader.loadLabel(Constants.LBL_RESULT_LOSS),actionLabel.getText());

        robot.clickOn(EasterEggModal.REVANCHE_BUTTON);

        // Check for correct outgoing websocket message
        verify(webSocketServiceSpy).sendPrivateMessage(otherUser.getName(), GameCommand.REVANCHE.command);
        Assertions.assertEquals(viewLoader.loadLabel(Constants.LBL_REVANCHE_WAIT), actionLabel.getText());

        // get revanche message of opponent
        currentUserCallback.handleMessage(buildOpponentCommand(otherUser, GameCommand.REVANCHE));
        WaitForAsyncUtils.waitForFxEvents();

        // check for view reset
        Assertions.assertEquals(viewLoader.loadLabel(Constants.LBL_CHOOSE_ACTION), actionLabel.getText());
        Assertions.assertEquals("-fx-background-color: transparent;", paperButton.getStyle());
        Assertions.assertEquals("-fx-background-color: transparent;", scissorsButton.getStyle());

        Button rockButton = robot.lookup(EasterEggModal.ROCK_BUTTON).query();
        robot.clickOn(rockButton);

        // Check for correct outgoing websocket message
        verify(webSocketServiceSpy).sendPrivateMessage(otherUser.getName(), GameCommand.CHOOSE_ROCK.command);
        Assertions.assertEquals("-fx-background-color: " + EasterEggModal.SELECTED_ACTION_COLOR + ";", rockButton.getStyle());

        // get choose message of opponent
        currentUserCallback.handleMessage(buildOpponentCommand(otherUser, GameCommand.CHOOSE_SCISSOR));
        WaitForAsyncUtils.waitForFxEvents();

        // check for correct result
        Assertions.assertEquals("1 : 0" + ", " + viewLoader.loadLabel(Constants.LBL_CHOOSE_ACTION), actionLabel.getText());

        // get leave message of opponent
        currentUserCallback.handleMessage(buildOpponentCommand(otherUser, GameCommand.LEAVE));
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(viewLoader.loadLabel(Constants.LBL_GAME_LEFT), actionLabel.getText());

        // Close mini game
        robot.clickOn(EasterEggModal.CANCEL_BUTTON);
        // Check for correct outgoing websocket message
        verify(webSocketServiceSpy).sendPrivateMessage(otherUser.getName(), GameCommand.LEAVE.command);

        // Assert modal is closed
        Assertions.assertThrows(FxRobotException.class, () -> {
            robot.clickOn(EasterEggModal.CANCEL_BUTTON);
        });
    }

    @AfterEach
    void tear(){
        router = null;
        editor = null;
        viewLoader = null;
        currentUser = null;
        webSocketServiceSpy = null;
        webSocketClientFactoryMock = null;
        stringArgumentCaptor = null;
        wsCallbackArgumentCaptor = null;
        endpointCallbackHashmap = null;
        Platform.runLater(app::stop);
        WaitForAsyncUtils.waitForFxEvents();
        app = null;
    }
}
