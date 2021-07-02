package de.uniks.stp.controller;

import com.jfoenix.controls.JFXTextField;
import de.uniks.stp.Constants;
import de.uniks.stp.StageManager;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.network.rest.AppRestClient;
import de.uniks.stp.network.websocket.WebSocketClient;
import de.uniks.stp.router.Router;
import de.uniks.stp.network.NetworkClientInjector;
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
public class RegisterTest {
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
    public void testRegisterEmptyFields(FxRobot robot) {
        clearNameField(robot);

        robot.clickOn("#name-field");
        robot.write("Guave");
        robot.clickOn("#register-button");

        Label errorLabel = robot.lookup("#error-message").query();
        Assertions.assertEquals(ViewLoader.loadLabel(Constants.LBL_MISSING_FIELDS), errorLabel.getText());
    }

    @Test
    public void testRegisterSuccess(FxRobot robot) {
        clearNameField(robot);

        robot.clickOn("#name-field");
        robot.write("Guave");
        robot.clickOn("#password-field");
        robot.write("evauG");
        robot.clickOn("#register-button");

        // register
        JSONObject j = new JSONObject().put("status", "success").put("message", "User created");
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);
        verify(restMock).register(eq("Guave"), eq("evauG"), callbackCaptor.capture());

        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);

        // login
        j = new JSONObject().put("status", "success").put("message", "").put("data", new JSONObject().put("userKey", "123-45"));
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock).login(eq("Guave"), eq("evauG"), callbackCaptor.capture());
        callback = callbackCaptor.getValue();
        callback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#home-button");
        Assertions.assertEquals(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_LIST_ONLINE_USERS, Router.getCurrentRoute());
    }

    @Test
    public void testRegisterFailure(FxRobot robot) {
        clearNameField(robot);

        robot.clickOn("#name-field");
        robot.write("Guave");
        robot.clickOn("#password-field");
        robot.write("evauG");
        robot.clickOn("#register-button");

        JSONObject j = new JSONObject().put(Constants.MESSAGE, "Name already taken");
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(false);

        verify(restMock).register(eq("Guave"), eq("evauG"), callbackCaptor.capture());

        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        Label errorLabel = robot.lookup("#error-message").query();

        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(Constants.ROUTE_LOGIN, Router.getCurrentRoute());
        Assertions.assertEquals(ViewLoader.loadLabel(Constants.LBL_REGISTRATION_NAME_TAKEN), errorLabel.getText());
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
