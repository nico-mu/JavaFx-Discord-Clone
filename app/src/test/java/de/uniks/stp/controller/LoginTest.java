package de.uniks.stp.controller;

import com.jfoenix.controls.JFXTextField;
import de.uniks.stp.AccordApp;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.dagger.components.test.AppTestComponent;
import de.uniks.stp.network.rest.AppRestClient;
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

import javax.json.Json;
import javax.json.JsonObject;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(ApplicationExtension.class)
public class LoginTest {

    @Mock
    private HttpResponse<JsonNode> res;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;
    private AccordApp app;
    private ViewLoader viewLoader;
    private AppRestClient restMock;
    private Router router;

    @Start
    public void start(Stage stage) {
        // start application
        MockitoAnnotations.initMocks(this);
        app = new AccordApp();
        app.setTestMode(true);
        app.start(stage);
        AppTestComponent appTestComponent = (AppTestComponent) app.getAppComponent();
        restMock = appTestComponent.getAppRestClient();
        router = appTestComponent.getRouter();
        viewLoader = appTestComponent.getViewLoader();
    }

    private void clearNameField(FxRobot robot) {
        JFXTextField nameField = robot.lookup("#name-field").query();
        Platform.runLater(nameField::clear);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testLoginEmptyFields(FxRobot robot) {
       clearNameField(robot);

        robot.clickOn("#name-field");
        robot.write("Guave");
        robot.clickOn("#login-button");
        Label errorLabel = robot.lookup("#error-message").query();
        Assertions.assertEquals(viewLoader.loadLabel(Constants.LBL_MISSING_FIELDS), errorLabel.getText());
    }

    @Test
    public void testLoginSuccess(FxRobot robot) {
        clearNameField(robot);

        robot.clickOn("#name-field");
        robot.write("Guave");
        robot.clickOn("#password-field");
        robot.write("evauG");
        robot.clickOn("#login-button");
        when(res.getBody()).thenReturn(new JsonNode(buildSuccessLoginMessage().toString()));

        when(res.isSuccess()).thenReturn(true);

        verify(restMock).login(eq("Guave"), eq("evauG"), callbackCaptor.capture());

        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#home-button");
        Assertions.assertEquals(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_LIST_ONLINE_USERS, router.getCurrentRoute());
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
        Assertions.assertEquals(viewLoader.loadLabel(Constants.LBL_LOGIN_WRONG_CREDENTIALS), errorLabel.getText());
    }

    public JsonObject buildSuccessLoginMessage() {
        return Json.createObjectBuilder()
            .add("status", "success")
            .add("message", "")
            .add("data", Json.createObjectBuilder()
                .add("userKey", "123-45")
                .build()
            )
            .build();
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

        WaitForAsyncUtils.waitForFxEvents();

        when(res.getBody()).thenReturn(new JsonNode(buildSuccessLoginMessage().toString()));

        when(res.isSuccess()).thenReturn(true);

        verify(restMock).login(eq(username), eq("Spock"), callbackCaptor.capture());

        callback = callbackCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#home-button");

        Assertions.assertEquals(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_LIST_ONLINE_USERS, router.getCurrentRoute());
        Assertions.assertTrue(Objects.nonNull(app.getSessionComponent()));

        Label usernameLabel = robot.lookup("#username-label").query();
        Assertions.assertEquals(username, usernameLabel.getText());

        response = new JSONObject()
            .put("status", "success")
            .put("message", "")
            .put("message", "Logged out")
            .put("data", new JSONObject());
        when(res.getBody()).thenReturn(new JsonNode(response.toString()));

        robot.clickOn("#logout-button");
        verify(app.getSessionComponent().getSessionRestClient()).sendLogoutRequest(callbackCaptor.capture());

        callback = callbackCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        JFXTextField nameField = robot.lookup("#name-field").query();
        Assertions.assertNotEquals(username, nameField.getText());
    }

    @AfterEach
    void tear(){
        restMock = null;
        viewLoader = null;
        router = null;
        res = null;
        callbackCaptor = null;
        Platform.runLater(app::stop);
        WaitForAsyncUtils.waitForFxEvents();
        app = null;
    }
}
