package de.uniks.stp.serversettings;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.StageManager;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.network.rest.AppRestClient;
import de.uniks.stp.network.websocket.WSCallback;
import de.uniks.stp.network.websocket.WebSocketClient;
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
public class DeleteServerTest {
    @Mock
    private AppRestClient restMock;

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
    public void testDeleteServerModal(FxRobot robot) {
        // prepare start situation
        Editor editor = StageManager.getEditor();
        editor.getOrCreateAccord().setCurrentUser(new User().setName("Test").setId("1")).setUserKey("123-45");

        String serverName ="Plattis Server";
        String serverId ="12345678";
        Server server = new Server().setName(serverName).setId(serverId).setOwner(editor.getOrCreateAccord().getCurrentUser());
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
        Assertions.assertEquals(ViewLoader.loadLabel(Constants.LBL_DELETE_SERVER), confiModalTitleLabel.getText());

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

        verify(restMock).deleteServer(eq(serverId), callbackCaptor.capture());
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
        editor.getOrCreateAccord().setCurrentUser(new User().setName("Test").setId("1")).setUserKey("123-45");

        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME));
        WaitForAsyncUtils.waitForFxEvents();

        String serverId ="12345678";
        Server server = new Server().setName("Plattis Server").setId(serverId).setOwner(editor.getOrCreateAccord().getCurrentUser());
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
        Assertions.assertEquals(ViewLoader.loadLabel(Constants.LBL_DELETE_SERVER), confiModalTitleLabel.getText());

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

        verify(restMock).deleteServer(eq(serverId), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        // check that ServerSettingsModal is shown with error message
        modalNameLabel = robot.lookup("#enter-servername-label").query();
        Assertions.assertEquals("Name", modalNameLabel.getText());
        Label errorLabel = robot.lookup("#error-message-label").query();
        Assertions.assertEquals(ViewLoader.loadLabel(Constants.LBL_DELETE_SERVER_FAILED), errorLabel.getText());

        robot.clickOn("#cancel-button");
    }

    /**
     * Tests deletion of the opened server by receiving a websocket message
     * @param robot
     */
    @Test
    public void testCurrentServerDeletedMessage(FxRobot robot) {
        // prepare start situation
        Editor editor = StageManager.getEditor();
        editor.getOrCreateAccord().setCurrentUser(new User().setName("Test")).setUserKey("123-45");

        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME));
        WaitForAsyncUtils.waitForFxEvents();

        String serverName = "Plattis Server";
        String serverId ="12345678";
        Server server = new Server().setName(serverName).setId(serverId).setOwner(editor.getOrCreateAccord().getCurrentUser());
        editor.getOrCreateAccord().getCurrentUser().withAvailableServers(server);

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#"+serverId+"-navBarElement");

        // assert correct start situation
        Assertions.assertEquals(1, editor.getOrCreateAccord().getCurrentUser().getAvailableServers().size());
        Pane serverNavBarElement = robot.lookup("#"+serverId+"-navBarElement").query();
        Assertions.assertNotNull(serverNavBarElement);
        TextFlow serverLabel = robot.lookup("#server-name").query();
        Assertions.assertEquals(serverName, ((Text) serverLabel.getChildren().get(0)).getText());


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
            .add("action", "serverDeleted")
            .add("data",
                Json.createObjectBuilder()
                    .add("id", serverId)
                    .add("name", serverName)
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

    /**
     * Tests deleting server by receiving a websocket message while other server is shown
     * @param robot
     */
    @Test
    public void testServerDeletedMessage(FxRobot robot) {
        // prepare start situation
        Editor editor = StageManager.getEditor();
        editor.getOrCreateAccord().setCurrentUser(new User().setName("Test")).setUserKey("123-45");

        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME));
        WaitForAsyncUtils.waitForFxEvents();

        String deleteServerName = "Plattis outdated Server";
        String deleteServerId ="1111111";
        Server deleteServer = new Server().setName(deleteServerName).setId(deleteServerId).setOwner(editor.getOrCreateAccord().getCurrentUser());
        editor.getOrCreateAccord().getCurrentUser().withAvailableServers(deleteServer);

        String serverTwoName = "Plattis useful Server";
        String serverTwoId ="42";
        Server serverTwo = new Server().setName(serverTwoName).setId(serverTwoId);
        editor.getOrCreateAccord().getCurrentUser().withAvailableServers(serverTwo);

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#"+serverTwoId+"-navBarElement");

        // assert correct start situation
        Assertions.assertEquals(2, editor.getOrCreateAccord().getCurrentUser().getAvailableServers().size());
        Pane deleteServerNavBarElement = robot.lookup("#"+deleteServerId+"-navBarElement").query();
        Assertions.assertNotNull(deleteServerNavBarElement);
        TextFlow shownServerLabel = robot.lookup("#server-name").query();
        Assertions.assertEquals(serverTwoName, ((Text) shownServerLabel.getChildren().get(0)).getText());


        // prepare receiving websocket message
        verify(webSocketMock, times(6)).inject(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();

        for(int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }
        WSCallback systemCallback = endpointCallbackHashmap.get(Constants.WS_SYSTEM_PATH + Constants.WS_SERVER_SYSTEM_PATH + deleteServerId);

        // receive message
        JsonObject jsonObject = Json.createObjectBuilder()
            .add("action", "serverDeleted")
            .add("data",
                Json.createObjectBuilder()
                    .add("id", deleteServerId)
                    .add("name", deleteServerName)
                    .build()
            )
            .build();
        systemCallback.handleMessage(jsonObject);

        WaitForAsyncUtils.waitForFxEvents();

        // check for correct reactions
        Assertions.assertEquals(1, editor.getOrCreateAccord().getCurrentUser().getAvailableServers().size());

        //check that ServerTwo is still shown
        shownServerLabel = robot.lookup("#server-name").query();
        Assertions.assertEquals(serverTwoName, ((Text) shownServerLabel.getChildren().get(0)).getText());

        // check that ServerNavBarElement of deleted server is no longer shown
        boolean shown = true;
        try{
            deleteServerNavBarElement = robot.lookup("#"+deleteServerId+"-navBarElement").query();
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
