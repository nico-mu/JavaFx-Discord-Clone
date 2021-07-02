package de.uniks.stp.controller;

import com.jfoenix.controls.JFXTextField;
import de.uniks.stp.Constants;
import de.uniks.stp.StageManager;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.rest.AppRestClient;
import de.uniks.stp.network.UserKeyProvider;
import de.uniks.stp.network.websocket.WebSocketClient;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(ApplicationExtension.class)
public class LoginTest {
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
        DatabaseService.clearAllConversations();
    }

    private void clearNameField(FxRobot robot) {
        JFXTextField nameField = robot.lookup("#name-field").query();
        Platform.runLater(() -> {
            nameField.clear();
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testLoginEmptyFields(FxRobot robot) {
       clearNameField(robot);

        robot.clickOn("#name-field");
        robot.write("Guave");
        robot.clickOn("#login-button");
        Label errorLabel = robot.lookup("#error-message").query();
        Assertions.assertEquals(ViewLoader.loadLabel(Constants.LBL_MISSING_FIELDS), errorLabel.getText());
    }

    @Test
    public void testLoginSuccess(FxRobot robot) {
        clearNameField(robot);

        robot.clickOn("#name-field");
        robot.write("Guave");
        robot.clickOn("#password-field");
        robot.write("evauG");
        robot.clickOn("#login-button");
        JSONObject j = new JSONObject().put("status", "success").put("message", "").put("data", new JSONObject().put("userKey", "123-45"));
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));

        when(res.isSuccess()).thenReturn(true);

        verify(restMock).login(eq("Guave"), eq("evauG"), callbackCaptor.capture());

        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#home-button");
        Assertions.assertEquals(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_LIST_ONLINE_USERS, Router.getCurrentRoute());
    }

    @Test
    public void testLoginFailure(FxRobot robot) {
        clearNameField(robot);

        robot.clickOn("#name-field");
        robot.write("Guave");
        robot.clickOn("#password-field");
        robot.write("evauG");
        robot.clickOn("#login-button");
        JSONObject j = new JSONObject().put(Constants.MESSAGE, "Invalid credentials");
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));

        when(res.isSuccess()).thenReturn(false);

        verify(restMock).login(eq("Guave"), eq("evauG"), callbackCaptor.capture());

        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        Label errorLabel = robot.lookup("#error-message").query();

        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(Constants.ROUTE_LOGIN, Router.getCurrentRoute());
        Assertions.assertEquals(ViewLoader.loadLabel(Constants.LBL_LOGIN_WRONG_CREDENTIALS), errorLabel.getText());
    }

    @Test
    public void testTempUserLogin(FxRobot robot) {
        clearNameField(robot);

        robot.clickOn("#temp-login-button");
        final String username = "Mr";
        JSONObject response = new JSONObject()
            .put("status", "success")
            .put("message", "")
            .put("data",
                new JSONObject()
                    .put("name", username)
                    .put("password", "Spock")
            );

        when(res.getBody()).thenReturn(new JsonNode(response.toString()));

        when(res.isSuccess()).thenReturn(true);

        verify(restMock).tempRegister(callbackCaptor.capture());

        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);

        final String userKey = "123-45";
        response = new JSONObject()
            .put("status", "success")
            .put("message", "")
            .put("data",
                new JSONObject()
                    .put("userKey", userKey)
            );

        WaitForAsyncUtils.waitForFxEvents();

        when(res.getBody()).thenReturn(new JsonNode(response.toString()));

        when(res.isSuccess()).thenReturn(true);

        verify(restMock).login(eq(username), eq("Spock"), callbackCaptor.capture());

        callback = callbackCaptor.getValue();
        callback.completed(res);


        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#home-button");

        Assertions.assertEquals(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_LIST_ONLINE_USERS, Router.getCurrentRoute());

        Assertions.assertEquals(userKey, UserKeyProvider.getUserKey());

        Label usernameLabel = robot.lookup("#username-label").query();
        Assertions.assertEquals(username, usernameLabel.getText());

        response = new JSONObject()
            .put("status", "success")
            .put("message", "")
            .put("message", "Logged out")
            .put("data", new JSONObject());
        when(res.getBody()).thenReturn(new JsonNode(response.toString()));

        robot.clickOn("#logout-button");
        verify(restMock).sendLogoutRequest(callbackCaptor.capture());

        callback = callbackCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        JFXTextField nameField = robot.lookup("#name-field").query();
        Assertions.assertNotEquals(username, nameField.getText());
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
