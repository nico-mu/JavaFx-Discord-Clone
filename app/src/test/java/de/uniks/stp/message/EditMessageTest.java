package de.uniks.stp.message;

import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.StageManager;
import de.uniks.stp.component.NavBarUserElement;
import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.model.*;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.network.WSCallback;
import de.uniks.stp.network.WebSocketClient;
import de.uniks.stp.notification.NotificationService;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.scene.Node;
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
import org.mockito.internal.matchers.Not;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.service.query.EmptyNodeQueryException;
import org.testfx.util.WaitForAsyncUtils;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class EditMessageTest {

    @Mock
    private RestClient restMock;

    @Mock
    private WebSocketClient webSocketMock;

    @Mock
    private HttpResponse<JsonNode> res;

    @Mock
    private HttpResponse<JsonNode> catRes;

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> catCallbackCaptor;

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
        DatabaseService.clearAllConversations();
    }

    @Test
    public void testEditMessage(FxRobot robot) {
        Editor editor = StageManager.getEditor();

        final User currentUser = new User().setName("Test").setId("123-45");
        final User userOne = new User().setName("userOne").setId("111-11");
        final User userTwo = new User().setName("userTwo").setId("222-22");
        NotificationService.register(userOne);
        NotificationService.register(userTwo);
        final String serverName = "Test";
        final String serverId = "1";
        final String categoryName = "Cat";
        final String categoryId = "Category1";
        final String channelOneId = "C1";

        editor.getOrCreateAccord()
            .setCurrentUser(currentUser)
            .setUserKey("123-45");

        editor.getOrCreateAccord()
            .withOtherUsers(userOne);

        editor.getOrCreateAccord()
            .withOtherUsers(userTwo);


        RouteArgs args = new RouteArgs().addArgument(":id", serverId);
        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
        WaitForAsyncUtils.waitForFxEvents();

        Server server = editor.getOrCreateServer(serverId, serverName);
        Category category = editor.getOrCreateCategory(categoryId, categoryName, server);
        Channel channel = editor.getOrCreateChannel(channelOneId, "ChannelOne", "text", category);
        server.withCategories(category);
        server.withChannels(channel);
        NotificationService.register(channel);

        currentUser.withAvailableServers(server);
        userOne.withAvailableServers(server);
        userTwo.withAvailableServers(server);

        RouteArgs routeArgs = new RouteArgs().addArgument(":id", serverId).addArgument(":categoryId", categoryId).addArgument(":channelId", channelOneId);
        Platform.runLater(() -> Router.routeWithArgs(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER + Constants.ROUTE_CHANNEL, routeArgs));
        WaitForAsyncUtils.waitForFxEvents();

        verify(webSocketMock, times(4)).inject(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();

        for (int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }
        WSCallback systemCallback = endpointCallbackHashmap.get(Constants.WS_USER_PATH + currentUser.getName() + Constants.WS_SERVER_CHAT_PATH + server.getId());

        JsonObject messageOne = Json.createObjectBuilder()
            .add("id", 1)
            .add("channel", channelOneId)
            .add("timestamp", 1)
            .add("from", "userOne")
            .add("text", "messageOne")
            .build();

        JsonObject messageTwo = Json.createObjectBuilder()
            .add("id", 2)
            .add("channel", channelOneId)
            .add("timestamp", 2)
            .add("from", currentUser.getName())
            .add("text", "messageTwo")
            .build();

        JsonObject messageThree = Json.createObjectBuilder()
            .add("id", 3)
            .add("channel", channelOneId)
            .add("timestamp", 3)
            .add("from", currentUser.getName())
            .add("text", "messageThree")
            .build();

        systemCallback.handleMessage(messageOne);
        systemCallback.handleMessage(messageTwo);
        systemCallback.handleMessage(messageThree);

        List<ServerMessage> editableMessages = new ArrayList<>();

        for (Channel c : server.getChannels()) {
            for (ServerMessage message : c.getMessages()) {
                if (message.getSender().getId().equals(currentUser.getId())) {
                    editableMessages.add(message);
                }
            }
        }

        robot.point("#message-text-" + editableMessages.get(0).getId());
        robot.point("#edit-message-" + editableMessages.get(0).getId());
        robot.clickOn("#edit-message-" + editableMessages.get(0).getId());
        String newMessage = "edited";
        robot.doubleClickOn("#message-text-field");
        robot.write(newMessage);
        robot.clickOn("#save-button");
        WaitForAsyncUtils.waitForFxEvents();

        JSONObject j = new JSONObject().put("status", "failure").put("message", "Missing name")
            .put("data", new JSONObject());

        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock).updateMessage(eq(serverId), eq(categoryId), eq(channelOneId), eq(editableMessages.get(0).getId()), eq(newMessage), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(editableMessages.get(0).getMessage(), newMessage);
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
