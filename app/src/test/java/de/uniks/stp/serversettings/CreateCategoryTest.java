package de.uniks.stp.serversettings;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.StageManager;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.network.*;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
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

import javax.json.Json;
import javax.json.JsonObject;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(ApplicationExtension.class)
public class CreateCategoryTest {
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

    private static final HashMap<String, WSCallback> endpointCallbackHashmap = new HashMap<>();

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
    public void testCreateCategory(FxRobot robot) {
        // prepare start situation
        Editor editor = StageManager.getEditor();
        editor.getOrCreateAccord().setCurrentUser(new User().setName("Test")).setUserKey("123-45");

        String serverName = "Plattis Server";
        String serverId = "12345678";
        Server server = new Server().setName(serverName).setId(serverId);
        editor.getOrCreateAccord().getCurrentUser().withAvailableServers(server);

        RouteArgs args = new RouteArgs().addArgument(":id", serverId);
        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
        WaitForAsyncUtils.waitForFxEvents();

        // assert correct start situation
        Assertions.assertEquals(1, editor.getOrCreateAccord().getCurrentUser().getAvailableServers().size());
        Assertions.assertEquals(0, editor.getServer(serverId).getCategories().size());

        // prepare creating category
        robot.clickOn("#settings-label");
        robot.clickOn("#create-menu-item");

        // insert name
        String categoryName = "useful category";
        robot.clickOn("#category-name-text-field");
        robot.write(categoryName);
        robot.clickOn("#create-button");

        JSONObject data = new JSONObject().put("id", "999")
            .put("name", categoryName)
            .put("server", serverId)
            .put("channels", new JSONArray());
        JSONObject j = new JSONObject().put("status", "success").put("message", "").put("data", data);
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock).createCategory(eq(serverId), eq(categoryName), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        // check for correct reactions
        Assertions.assertEquals(1, editor.getServer(serverId).getCategories().size());
        Assertions.assertEquals(categoryName, editor.getServer(serverId).getCategories().get(0).getName());

        Label categoryNameLabel = robot.lookup("#category-head-label").query();
        Assertions.assertEquals(categoryName, categoryNameLabel.getText());
    }

    @Test
    public void testCategoryCreatedMessage(FxRobot robot) {
        final String CATEGORY_NAME = "Useful Category";

        // prepare start situation
        Editor editor = StageManager.getEditor();
        editor.getOrCreateAccord().setCurrentUser(new User().setName("Test")).setUserKey("123-45");

        String serverName = "Plattis Server";
        String serverId = "12345678";
        Server server = new Server().setName(serverName).setId(serverId);
        editor.getOrCreateAccord().getCurrentUser().withAvailableServers(server);

        WebSocketService.addServerWebSocket(serverId);

        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, new RouteArgs().addArgument(":id", serverId)));
        WaitForAsyncUtils.waitForFxEvents();

        // assert correct start situation
        Assertions.assertEquals(1, editor.getOrCreateAccord().getCurrentUser().getAvailableServers().size());
        Assertions.assertEquals(0, editor.getServer(serverId).getCategories().size());

        // prepare receiving websocket message
        verify(webSocketMock, times(4)).inject(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();

        for (int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }
        WSCallback systemCallback = endpointCallbackHashmap.get(Constants.WS_SYSTEM_PATH + Constants.WS_SERVER_SYSTEM_PATH + serverId);

        // receive message
        JsonObject jsonObject = Json.createObjectBuilder()
            .add("action", "categoryCreated")
            .add("data",
                Json.createObjectBuilder()
                    .add("id", "1111")
                    .add("name", CATEGORY_NAME)
                    .add("server", serverId)
                    .build()
            )
            .build();
        systemCallback.handleMessage(jsonObject);

        WaitForAsyncUtils.waitForFxEvents();

        // check for correct reactions
        Assertions.assertEquals(1, editor.getServer(serverId).getCategories().size());
        Assertions.assertEquals(CATEGORY_NAME, editor.getServer(serverId).getCategories().get(0).getName());

        Label categoryNameLabel = robot.lookup("#category-head-label").query();
        Assertions.assertEquals(CATEGORY_NAME, categoryNameLabel.getText());
    }
}
