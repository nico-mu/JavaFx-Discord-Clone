package de.uniks.stp.serversettings;

import com.jfoenix.controls.JFXTextField;
import de.uniks.stp.AccordApp;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.component.NavBarCreateServer;
import de.uniks.stp.dagger.components.test.AppTestComponent;
import de.uniks.stp.dagger.components.test.SessionTestComponent;
import de.uniks.stp.modal.CreateServerModal;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.network.rest.SessionRestClient;
import de.uniks.stp.network.websocket.WSCallback;
import de.uniks.stp.network.websocket.WebSocketClientFactory;
import de.uniks.stp.network.websocket.WebSocketService;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(ApplicationExtension.class)
public class CreateServerTest {
    @Mock
    private HttpResponse<JsonNode> res;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    @Captor
    private ArgumentCaptor<WSCallback> wsCallbackArgumentCaptor;

    private HashMap<String, WSCallback> endpointCallbackHashmap;

    private AccordApp app;
    private Router router;
    private Editor editor;
    private WebSocketClientFactory webSocketClientFactoryMock;
    private WebSocketService webSocketService;
    private SessionRestClient restMock;

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
        User currentUser = editor.createCurrentUser("TestUser1", true).setId("1").setPassword("password");
        editor.setCurrentUser(currentUser);

        SessionTestComponent sessionTestComponent = appTestComponent
            .sessionTestComponentBuilder()
            .currentUser(currentUser)
            .userKey("123-45")
            .build();
        app.setSessionComponent(sessionTestComponent);

        webSocketClientFactoryMock = sessionTestComponent.getWebSocketClientFactory();
        webSocketService = sessionTestComponent.getWebsocketService();
        restMock = sessionTestComponent.getSessionRestClient();
        webSocketService.init();
    }

    @Test
    public void createServerTest(FxRobot robot) {
        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_LIST_ONLINE_USERS, new RouteArgs()));
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#create-server");
        robot.clickOn("#add-server-cancel-button");
        robot.clickOn("#create-server");
        robot.clickOn("#add-server-create-button");

        Label errorLabel = robot.lookup("#error-message-label").query();
        Assertions.assertNotEquals("", errorLabel.getText());

        robot.clickOn("#servername-text-field");
        robot.write("testServer");
        robot.clickOn("#add-server-create-button");
        Assertions.assertEquals("", errorLabel.getText());

        //mock rest response
        JSONObject j = new JSONObject()
            .put("status", "failure");
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(false);

        verify(restMock).createServer(eq("testServer"), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertNotEquals("", errorLabel.getText());

        JFXTextField nameTextField = robot.lookup("#servername-text-field").query();
        nameTextField.setText("");
        robot.clickOn("#servername-text-field");
        robot.write("testServer2");
        robot.clickOn("#add-server-create-button");

        JSONObject j2 = new JSONObject()
            .put("status", "success")
            .put("data", new JSONObject()
                .put("name", "testServer2")
                .put("id", "testServer2Id"));
        when(res.getBody()).thenReturn(new JsonNode(j2.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock).createServer(eq("testServer2"), callbackCaptor.capture());
        Callback<JsonNode> callback2 = callbackCaptor.getValue();
        callback2.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(1, editor.getOrCreateAccord().getCurrentUser().getAvailableServers().size());
        Assertions.assertEquals("testServer2", editor.getOrCreateAccord().getCurrentUser().getAvailableServers().get(0).getName());
        Assertions.assertEquals("testServer2Id", editor.getOrCreateAccord().getCurrentUser().getAvailableServers().get(0).getId());
    }

    @Test
    public void joinServerTest(FxRobot nico) {
        String testUserName = "TestUser1";
        String password = "password";

        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_LIST_ONLINE_USERS, new RouteArgs()));
        WaitForAsyncUtils.waitForFxEvents();

        nico.clickOn(NavBarCreateServer.CREATE_SERVER_ID);
        nico.clickOn(CreateServerModal.ADD_SERVER_JOIN_BUTTON);

        Label errorLabel = nico.lookup(CreateServerModal.ADD_SERVER_ERROR_LABEL).query();
        Assertions.assertNotEquals("", errorLabel.getText());

        nico.clickOn(CreateServerModal.ADD_SERVER_TEXT_FIELD_SERVER_ADDRESS);
        String serverId = "sId";
        String inviteId = "iId";
        nico.write(Constants.REST_SERVER_BASE_URL + Constants.SERVERS_PATH + "/" + serverId + Constants.REST_INVITES_PATH + "/" + inviteId);
        nico.clickOn(CreateServerModal.ADD_SERVER_JOIN_BUTTON);

        JSONObject j = new JSONObject()
            .put("status", "failure");
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(false);

        verify(restMock).joinServer(eq(serverId), eq(inviteId), eq(testUserName), eq(password), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertNotEquals("", errorLabel.getText());

        JFXTextField addressTextfield = nico.lookup(CreateServerModal.ADD_SERVER_TEXT_FIELD_SERVER_ADDRESS).query();
        addressTextfield.clear();
        nico.clickOn(CreateServerModal.ADD_SERVER_TEXT_FIELD_SERVER_ADDRESS);
        String serverId2 = "sId2";
        String inviteId2 = "iId2";
        nico.write(Constants.REST_SERVER_BASE_URL + Constants.SERVERS_PATH + "/" + serverId2 + Constants.REST_INVITES_PATH + "/" + inviteId2);
        nico.clickOn(CreateServerModal.ADD_SERVER_JOIN_BUTTON);
        Assertions.assertEquals("", errorLabel.getText());

        JSONObject j2 = new JSONObject()
            .put("status", "success");
        when(res.getBody()).thenReturn(new JsonNode(j2.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock).joinServer(eq(serverId2), eq(inviteId2), eq(testUserName), eq(password), callbackCaptor.capture());
        Callback<JsonNode> callback2 = callbackCaptor.getValue();
        callback2.completed(res);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterEach
    void tear(){
        restMock = null;
        webSocketClientFactoryMock = null;
        editor = null;
        webSocketService = null;
        router = null;
        res = null;
        callbackCaptor = null;
        stringArgumentCaptor = null;
        wsCallbackArgumentCaptor = null;
        endpointCallbackHashmap = null;
        Platform.runLater(app::stop);
        WaitForAsyncUtils.waitForFxEvents();
        app = null;
    }
}
