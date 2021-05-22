package de.uniks.stp.controller;

import com.jfoenix.controls.JFXTextField;
import de.uniks.stp.Constants;
import de.uniks.stp.StageManager;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.network.WebSocketClient;
import de.uniks.stp.router.Router;
import de.uniks.stp.network.NetworkClientInjector;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
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
    private RestClient restMock;

    @Mock
    private WebSocketClient webSocketMock;

    @Mock
    private HttpResponse<JsonNode> res;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;

    @Start
    public void start(Stage stage) {
        // start application
        MockitoAnnotations.initMocks(this);
        NetworkClientInjector.setRestClient(restMock);
        NetworkClientInjector.setWebSocketClient(webSocketMock);
        StageManager app = new StageManager();
        app.start(stage);
    }

    private void clearNameField(FxRobot robot) {
        ((JFXTextField) robot.lookup("#name-field").query()).clear();
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

        Platform.runLater(() -> {
            robot.clickOn("#home-button");
            Assertions.assertEquals(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_LIST_ONLINE_USERS, Router.getCurrentRoute());
        });
        WaitForAsyncUtils.waitForFxEvents();
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

        Platform.runLater(() -> {
            Assertions.assertEquals(Constants.ROUTE_LOGIN, Router.getCurrentRoute());
            Assertions.assertEquals(ViewLoader.loadLabel(Constants.LBL_LOGIN_WRONG_CREDENTIALS), errorLabel.getText());
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

}
