package de.uniks.stp.serversettings;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.StageManager;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.network.*;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
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
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(ApplicationExtension.class)
public class LeaveServerTest {
    @Mock
    private RestClient restMock;

    @Mock
    private WebSocketClient webSocketMock;

    @Mock
    private HttpResponse<JsonNode> res;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    @Captor
    private ArgumentCaptor<WSCallback> wsCallbackArgumentCaptor;

    private HashMap<String, WSCallback> endpointCallbackHashmap = new HashMap<>();
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
    }

    /**
     * Tests the ServerSettingsModal and ConfirmationModal: Checks for correct Rest call, opening and closing of the modals
     * @param robot
     */
    @Test
    public void testLeaveServerModal(FxRobot robot) {
        // prepare start situation
        Editor editor = StageManager.getEditor();
        editor.getOrCreateAccord().setCurrentUser(new User().setName("Test")).setUserKey("123-45");

        String serverName ="Plattis Server";
        String serverId ="12345678";
        Server server = new Server().setName(serverName).setId(serverId);
        editor.getOrCreateAccord().getCurrentUser().withAvailableServers(server);

        RouteArgs args = new RouteArgs().addArgument(":id", serverId);
        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
        WaitForAsyncUtils.waitForFxEvents();

        // prepare deleting server
        robot.clickOn("#settings-label");
        robot.clickOn("#edit-menu-item");

        // assert that ServerSettingsModal is shown
        Label modalNameLabel = robot.lookup("#enter-servername-label").query();
        Assertions.assertEquals("Name", modalNameLabel.getText());

        robot.clickOn("#delete-button");

        // assert that ConfirmationModal is shown
        Label confiModalTitleLabel = robot.lookup("#title-label").query();
        Assertions.assertEquals(ViewLoader.loadLabel(Constants.LBL_LEAVE_SERVER), confiModalTitleLabel.getText());

        robot.clickOn("#yes-button");

        // check that ConfirmationModal is no longer shown
        boolean modalShown = true;
        try{
            modalNameLabel = robot.lookup("#title-label").query();
        } catch (Exception e) {
            modalShown = false;
        }
        Assertions.assertFalse(modalShown);


        JSONObject j = new JSONObject().put("status", "success").put("message", "")
            .put("data", new JSONObject().put("id", serverId).put("name", serverName));
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock).leaveServer(eq(serverId), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        // check that ServerSettingsModal is no longer shown
        modalShown = true;
        try{
            modalNameLabel = robot.lookup("#enter-servername-label").query();
        } catch (Exception e) {
            modalShown = false;
        }
        Assertions.assertFalse(modalShown);
    }

    /**
     * Tests error message in ServerSettingsModal
     * @param robot
     */
    @Test
    public void testDeleteServerFailed(FxRobot robot) {
        // prepare start situation
        Editor editor = StageManager.getEditor();
        editor.getOrCreateAccord().setCurrentUser(new User().setName("Test")).setUserKey("123-45");

        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME));
        WaitForAsyncUtils.waitForFxEvents();

        String serverId ="12345678";
        Server server = new Server().setName("Plattis Server").setId(serverId);
        editor.getOrCreateAccord().getCurrentUser().withAvailableServers(server);

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#"+serverId+"-navBarElement");

        // prepare deleting server
        robot.clickOn("#settings-label");
        robot.clickOn("#edit-menu-item");

        // assert that ServerSettingsModal is shown
        Label modalNameLabel = robot.lookup("#enter-servername-label").query();
        Assertions.assertEquals("Name", modalNameLabel.getText());

        robot.clickOn("#delete-button");

        // assert that ConfirmationModal is shown
        Label confiModalTitleLabel = robot.lookup("#title-label").query();
        Assertions.assertEquals(ViewLoader.loadLabel(Constants.LBL_LEAVE_SERVER), confiModalTitleLabel.getText());

        robot.clickOn("#yes-button");

        // check that ConfirmationModal is no longer shown
        boolean modalShown = true;
        try{
            modalNameLabel = robot.lookup("#title-label").query();
        } catch (Exception e) {
            modalShown = false;
        }
        Assertions.assertFalse(modalShown);


        JSONObject j = new JSONObject().put("status", "success").put("message", "")
            .put("data", new JSONObject());
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(false);

        verify(restMock).leaveServer(eq(serverId), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        // check that ServerSettingsModal is shown with error message
        modalNameLabel = robot.lookup("#enter-servername-label").query();
        Assertions.assertEquals("Name", modalNameLabel.getText());
        Label errorLabel = robot.lookup("#error-message-label").query();
        Assertions.assertEquals(ViewLoader.loadLabel(Constants.LBL_LEAVE_SERVER_FAILED), errorLabel.getText());

        robot.clickOn("#cancel-button");
    }

    /**
     * Tests leave of the opened server by receiving a websocket message
     * @param robot
     */
    @Test
    public void testCurrentServerLeftMessage(FxRobot robot) {
        // prepare start situation
        Editor editor = StageManager.getEditor();
        String currentUserName = "Test";
        String currentUserId = "1";
        editor.getOrCreateAccord().setCurrentUser(new User().setName(currentUserName).setId(currentUserId)).setUserKey("123-45");

        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME));
        WaitForAsyncUtils.waitForFxEvents();

        String serverName = "Plattis Server";
        String serverId ="12345678";
        Server server = new Server().setName(serverName).setId(serverId);
        editor.getOrCreateAccord().getCurrentUser().withAvailableServers(server);

        String userName = "user1";
        String userId = "2";
        User user1 = new User().setName(userName).setId(userId);
        server.withUsers(user1);

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#"+serverId+"-navBarElement");

        // assert correct start situation
        Assertions.assertEquals(1, editor.getOrCreateAccord().getCurrentUser().getAvailableServers().size());
        Pane serverNavBarElement = robot.lookup("#"+serverId+"-navBarElement").query();
        Assertions.assertNotNull(serverNavBarElement);
        TextFlow serverLabel = robot.lookup("#server-name").query();
        Assertions.assertEquals(serverName, ((Text) serverLabel.getChildren().get(0)).getText());
        Assertions.assertEquals(2, editor.getOrCreateAccord().getCurrentUser().getAvailableServers().get(0).getUsers().size());


        // prepare receiving websocket message
        verify(webSocketMock, times(4)).inject(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();

        for(int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }
        WSCallback systemCallback = endpointCallbackHashmap.get(Constants.WS_SYSTEM_PATH + Constants.WS_SERVER_SYSTEM_PATH + serverId);

        // receive message
        JsonObject jsonObject = Json.createObjectBuilder()
            .add("action", "userExited")
            .add("data",
                Json.createObjectBuilder()
                    .add("id", currentUserId)
                    .add("name", currentUserName)
                    .build()
            )
            .build();
        systemCallback.handleMessage(jsonObject);

        WaitForAsyncUtils.waitForFxEvents();

        // check for correct reactions
        Assertions.assertEquals(0, editor.getOrCreateAccord().getCurrentUser().getAvailableServers().size());

        //check that Home Screen is shown
        JFXButton onlineUsersLabel = robot.lookup("#toggle-online-button").query();
        Assertions.assertEquals("Online", onlineUsersLabel.getText());

        // check that ServerNavBarElement is no longer shown
        boolean shown = true;
        try{
            serverNavBarElement = robot.lookup("#"+serverId+"-navBarElement").query();
        } catch (Exception e) {
            shown = false;
        }
        Assertions.assertFalse(shown);
    }

    @AfterEach
    void tear(){
        restMock = null;
        webSocketMock = null;
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
