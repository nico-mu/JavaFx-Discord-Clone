package de.uniks.stp.serversettings;

import de.uniks.stp.AccordApp;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.dagger.components.test.AppTestComponent;
import de.uniks.stp.dagger.components.test.SessionTestComponent;
import de.uniks.stp.modal.ConfirmationModal;
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
public class DeleteCategoryTest {
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
    private ViewLoader viewLoader;

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
        viewLoader = appTestComponent.getViewLoader();
        User currentUser = editor.createCurrentUser("Test", true).setId("123-45");
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

    /**
     * Tests the EditCategoryModal & ConfirmationModal: Checks for correct Rest call and opening/closing the modals
     * @param robot
     */
    @Test
    public void testDeleteCategoryModals(FxRobot robot){
        // prepare start situation
        String serverName ="Plattis Server";
        String serverId ="12345678";
        String catId = "111";
        String catName = "CatName";
        Server server = new Server().setName(serverName).setId(serverId);
        editor.getOrCreateAccord().getCurrentUser().withAvailableServers(server);

        webSocketService.addServerWebSocket(serverId);

        RouteArgs args = new RouteArgs().addArgument(":id", serverId);
        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
        WaitForAsyncUtils.waitForFxEvents();

        verify(webSocketClientFactoryMock, times(4))
            .create(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

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

        // assert that EditCategoryModal is shown
        Label modalNameLabel = robot.lookup("#enter-category-name-label").query();
        Assertions.assertEquals("Name", modalNameLabel.getText());

        robot.clickOn("#delete-button");

        // assert that ConfirmationModal is shown
        Label confiModalTitleLabel = robot.lookup("#title-label").query();
        Assertions.assertEquals(viewLoader.loadLabel(Constants.LBL_DELETE_CATEGORY), confiModalTitleLabel.getText());

        robot.clickOn(ConfirmationModal.NO_BUTTON);
        robot.clickOn("#delete-button");
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
            .put("data", new JSONObject().put("id", catId).put("name", catName).put("server", serverId).put("channels", new JSONArray()));
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock).deleteCategory(eq(serverId), eq(catId), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        // check that EditCategoryModal is no longer shown
        modalShown = true;
        try{
            modalNameLabel = robot.lookup("#enter-category-name-label").query();
        } catch (Exception e) {
            modalShown = false;
        }
        Assertions.assertFalse(modalShown);
    }

    /**
     * Tests error message in EditCategoryModal
     * @param robot
     */
    @Test
    public void testDeleteCategoryFailed(FxRobot robot){
        // prepare start situation
        String serverName ="Plattis Server";
        String serverId ="12345678";
        String catId = "111";
        String catName = "CatName";
        Server server = new Server().setName(serverName).setId(serverId);
        editor.getOrCreateAccord().getCurrentUser().withAvailableServers(server);

        webSocketService.addServerWebSocket(serverId);

        RouteArgs args = new RouteArgs().addArgument(":id", serverId);
        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
        WaitForAsyncUtils.waitForFxEvents();

        verify(webSocketClientFactoryMock, times(4))
            .create(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

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

        // assert that EditCategoryModal is shown
        Label modalNameLabel = robot.lookup("#enter-category-name-label").query();
        Assertions.assertEquals("Name", modalNameLabel.getText());

        robot.clickOn("#delete-button");

        // assert that ConfirmationModal is shown
        Label confiModalTitleLabel = robot.lookup("#title-label").query();
        Assertions.assertEquals(viewLoader.loadLabel(Constants.LBL_DELETE_CATEGORY), confiModalTitleLabel.getText());

        robot.clickOn("#yes-button");

        // check that ConfirmationModal is no longer shown
        boolean modalShown = true;
        try{
            modalNameLabel = robot.lookup("#title-label").query();
        } catch (Exception e) {
            modalShown = false;
        }
        Assertions.assertFalse(modalShown);


        JSONObject j = new JSONObject().put("status", "failure").put("message", "")
            .put("data", new JSONObject());
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(false);

        verify(restMock).deleteCategory(eq(serverId), eq(catId), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        // check that EditCategoryModal is shown with error message
        modalNameLabel = robot.lookup("#enter-category-name-label").query();
        Assertions.assertEquals("Name", modalNameLabel.getText());
        Label errorLabel = robot.lookup("#error-message-label").query();
        Assertions.assertEquals(viewLoader.loadLabel(Constants.LBL_DELETE_CATEGORY_FAILED), errorLabel.getText());

        robot.clickOn("#cancel-button");
    }

    /**
     * Tests deleting category by receiving a websocket message
     * @param robot
     */
    @Test
    public void testCategoryDeletedMessage(FxRobot robot){
        // prepare start situation

        String serverName ="Plattis Server";
        String serverId ="12345678";
        String catId = "111";
        String catName = "CatName";
        Server server = new Server().setName(serverName).setId(serverId);
        editor.getOrCreateAccord().getCurrentUser().withAvailableServers(server);

        webSocketService.addServerWebSocket(serverId);

        RouteArgs args = new RouteArgs().addArgument(":id", serverId);
        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
        WaitForAsyncUtils.waitForFxEvents();

        Category cat = new Category().setId(catId).setName(catName).setServer(server);
        WaitForAsyncUtils.waitForFxEvents();

        // assert correct start situation
        Assertions.assertEquals(1, editor.getOrCreateAccord().getCurrentUser().getAvailableServers().size());
        Assertions.assertEquals(1, editor.getServer(serverId).getCategories().size());

        TextFlow catLabel = robot.lookup("#" + catId + "-ServerCategoryElementLabel").query();
        Assertions.assertEquals(catName, ((Text) catLabel.getChildren().get(0)).getText());

        // prepare receiving websocket message
        verify(webSocketClientFactoryMock, times(4))
            .create(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();

        for (int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }
        WSCallback systemCallback = endpointCallbackHashmap.get(Constants.WS_SYSTEM_PATH + Constants.WS_SERVER_SYSTEM_PATH + serverId);

        // receive message
        JsonObject jsonObject = Json.createObjectBuilder()
            .add("action", "categoryDeleted")
            .add("data",
                Json.createObjectBuilder()
                    .add("id", catId)
                    .add("server", serverId)
                    .build()
            )
            .build();
        systemCallback.handleMessage(jsonObject);

        WaitForAsyncUtils.waitForFxEvents();

        // check for correct reactions
        Assertions.assertEquals(1, editor.getOrCreateAccord().getCurrentUser().getAvailableServers().size());
        Assertions.assertEquals(0, editor.getServer(serverId).getCategories().size());
        // check that categoryElement is no longer shown
        boolean shown = true;
        try{
            catLabel = robot.lookup("#" + catId + "-ServerCategoryElementLabel").query();
        } catch (Exception e) {
            shown = false;
        }
        Assertions.assertFalse(shown);
    }

    @AfterEach
    void tear(){
        restMock = null;
        webSocketClientFactoryMock = null;
        editor = null;
        webSocketService = null;
        router = null;
        viewLoader = null;
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
