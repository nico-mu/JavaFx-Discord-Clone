package de.uniks.stp.serversettings;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.StageManager;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.network.*;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
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
public class EditCategoryTest {
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

    private HashMap<String, WSCallback> endpointCallbackHashmap;
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
     * Tests the EditCategoryModal: Checks for correct Rest call and closing the modal on success
     * @param robot
     */
    @Test
    public void testEditCategoryName(FxRobot robot){
        // prepare start situation
        Editor editor = StageManager.getEditor();
        editor.getOrCreateAccord().setCurrentUser(new User().setName("Test")).setUserKey("123-45");

        String serverName ="Plattis Server";
        String serverId ="12345678";
        String catId = "111";
        String catName = "CatName";
        Server server = new Server().setName(serverName).setId(serverId);
        editor.getOrCreateAccord().getCurrentUser().withAvailableServers(server);

        WebSocketService.addServerWebSocket(serverId);

        RouteArgs args = new RouteArgs().addArgument(":id", serverId);
        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
        WaitForAsyncUtils.waitForFxEvents();

        verify(webSocketMock, times(4)).inject(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();

        for (int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }

        Category cat = new Category().setId(catId).setName(catName).setServer(server);
        WaitForAsyncUtils.waitForFxEvents();

        // assert correct start situation
        Assertions.assertEquals(1, editor.getOrCreateAccord().getCurrentUser().getAvailableServers().size());
        Assertions.assertEquals(1, editor.getServer(serverId).getCategories().size());
        Assertions.assertEquals(0, editor.getServer(serverId).getCategories().get(0).getChannels().size());

        TextFlow label = robot.lookup("#" + cat.getId() + "-ServerCategoryElementLabel").query();
        Assertions.assertEquals(catName, ((Text) label.getChildren().get(0)).getText());

        robot.clickOn("#" + cat.getId() + "-ServerCategoryElementLabel");
        robot.point("#edit-category-gear");
        robot.clickOn("#edit-category-gear");

        // assert that modal is shown
        Label modalNameLabel = robot.lookup("#enter-category-name-label").query();
        Assertions.assertEquals("Name", modalNameLabel.getText());

        // create category
        String categoryName = "useful category";
        robot.clickOn("#category-name-text-field");
        robot.write(categoryName);
        robot.clickOn("#save-button");

        JsonObject jsonObject = Json.createObjectBuilder()
            .add("action", "categoryUpdated")
            .add("data",
                Json.createObjectBuilder()
                    .add("id", "111")
                    .add("name", categoryName)
                    .add("server", serverId)
                    .build()
            )
            .build();

        when(res.getBody()).thenReturn(new JsonNode(jsonObject.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock).updateCategory(eq(serverId), eq(catId), eq(categoryName), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        WSCallback systemCallback = endpointCallbackHashmap.get(Constants.WS_SYSTEM_PATH + Constants.WS_SERVER_SYSTEM_PATH + serverId);
        systemCallback.handleMessage(jsonObject);
        WaitForAsyncUtils.waitForFxEvents();
        // check that modal is no longer shown
        boolean modalShown = true;
        try{
            modalNameLabel = robot.lookup("#enter-category-name-label").query();
        } catch (Exception e) {
            modalShown = false;
        }
        Assertions.assertFalse(modalShown);

        label = robot.lookup("#" + cat.getId() + "-ServerCategoryElementLabel").query();
        Assertions.assertEquals(categoryName, ((Text) label.getChildren().get(0)).getText());
    }

    /**
     * Tests error message in EditCategoryModal
     * @param robot
     */
    @Test
    public void testEditCategoryFailed(FxRobot robot){
        // prepare start situation
        Editor editor = StageManager.getEditor();
        editor.getOrCreateAccord().setCurrentUser(new User().setName("Test")).setUserKey("123-45");

        String serverName ="Plattis Server";
        String serverId ="12345678";
        String catName = "CatName";
        String catId = "111";
        Server server = new Server().setName(serverName).setId(serverId);
        editor.getOrCreateAccord().getCurrentUser().withAvailableServers(server);

        RouteArgs args = new RouteArgs().addArgument(":id", serverId);
        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
        WaitForAsyncUtils.waitForFxEvents();

        Category cat = new Category().setName(catName).setId(catId).setServer(server);
        WaitForAsyncUtils.waitForFxEvents();

        // assert correct start situation
        Assertions.assertEquals(1, editor.getOrCreateAccord().getCurrentUser().getAvailableServers().size());
        Assertions.assertEquals(1, editor.getServer(serverId).getCategories().size());

        // prepare creating category
        robot.clickOn("#" + cat.getId() + "-ServerCategoryElementLabel");
        robot.point("#edit-category-gear");
        robot.clickOn("#edit-category-gear");

        // insert name
        String categoryName = "useful category";
        robot.clickOn("#category-name-text-field");
        robot.write(categoryName);
        robot.clickOn("#save-button");

        JSONObject j = new JSONObject().put("status", "failure").put("message", "something went wrong");
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(false);

        verify(restMock).updateCategory(eq(serverId), eq(catId), eq(categoryName), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        // check for correct reactions
        Category queryCat = editor.getOrCreateCategory(catId, catName, server);
        Assertions.assertEquals(1, editor.getOrCreateAccord().getCurrentUser().getAvailableServers().size());
        Assertions.assertEquals(1, editor.getServer(serverId).getCategories().size());
        Assertions.assertEquals(catName, queryCat.getName());


        Label errorLabel = robot.lookup("#error-message-label").query();
        Assertions.assertEquals(ViewLoader.loadLabel(Constants.LBL_RENAME_CATEGORY_FAILED), errorLabel.getText());

        robot.clickOn("#cancel-button");
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
