package de.uniks.stp.controller;

import com.jfoenix.controls.JFXTextField;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.StageManager;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.network.WebSocketClient;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
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
public class ServerSettingsTest {
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

    @Test
    public void testChangeServerName(FxRobot robot){
        // prepare start situation
        Editor editor = StageManager.getEditor();

        editor.getOrCreateAccord().setCurrentUser(new User().setName("Test")).setUserKey("123-45");

        String oldName ="Shitty Name";
        String serverId ="12345678";
        editor.getOrCreateAccord()
            .getCurrentUser()
            .withAvailableServers(new Server().setName(oldName).setId(serverId));

        RouteArgs args = new RouteArgs().addArgument(":id", serverId);
        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
        WaitForAsyncUtils.waitForFxEvents();

        // assert correct start situation
        Assertions.assertEquals(1, editor.getOrCreateAccord().getCurrentUser().getAvailableServers().size());
        Assertions.assertEquals(oldName, editor.getServer(serverId).getName());

        Label serverLabel = robot.lookup("#server-name-label").query();
        Assertions.assertEquals(oldName, serverLabel.getText());

        // prepare changing server name
        robot.clickOn("#settings-label");
        robot.clickOn("#edit-menu-item");

        // change name
        String newName = "Nice Name";
        robot.clickOn("#servername-text-field");
        robot.write(newName);
        robot.clickOn("#save-button");

        JSONObject j = new JSONObject().put("status", "success").put("message", "")
            .put("data", new JSONObject().put("id", serverId).put("name", newName));
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock).renameServer(eq(serverId), eq(newName), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        // check for correct reactions
        Assertions.assertEquals(newName, serverLabel.getText());
        Assertions.assertEquals(newName, editor.getServer(serverId).getName());
    }
}
