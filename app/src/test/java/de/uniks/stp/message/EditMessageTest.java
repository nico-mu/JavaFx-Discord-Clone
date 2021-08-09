package de.uniks.stp.message;

import com.jfoenix.controls.JFXTextField;
import de.uniks.stp.AccordApp;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.dagger.components.test.AppTestComponent;
import de.uniks.stp.dagger.components.test.SessionTestComponent;
import de.uniks.stp.modal.ConfirmationModal;
import de.uniks.stp.modal.EditMessageModal;
import de.uniks.stp.model.*;
import de.uniks.stp.network.rest.SessionRestClient;
import de.uniks.stp.network.websocket.WSCallback;
import de.uniks.stp.network.websocket.WebSocketClientFactory;
import de.uniks.stp.network.websocket.WebSocketService;
import de.uniks.stp.notification.NotificationService;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
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

import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class EditMessageTest {
    @Mock
    private HttpResponse<JsonNode> res;

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;

    @Captor
    private ArgumentCaptor<WSCallback> wsCallbackArgumentCaptor;

    private HashMap<String, WSCallback> endpointCallbackHashmap;
    private AccordApp app;
    private Router router;
    private Editor editor;
    private User currentUser;
    private NotificationService notificationService;
    private WebSocketClientFactory webSocketClientFactoryMock;
    private SessionRestClient restMock;
    private WebSocketService webSocketService;

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
        currentUser = editor.createCurrentUser("Test", true).setId("123-45");
        editor.setCurrentUser(currentUser);

        SessionTestComponent sessionTestComponent = appTestComponent
            .sessionTestComponentBuilder()
            .currentUser(currentUser)
            .userKey("123-45")
            .build();
        app.setSessionComponent(sessionTestComponent);

        notificationService = sessionTestComponent.getNotificationService();
        webSocketClientFactoryMock = sessionTestComponent.getWebSocketClientFactory();
        restMock = sessionTestComponent.getSessionRestClient();
        webSocketService = sessionTestComponent.getWebsocketService();
        webSocketService.init();
    }

    @Test
    public void testEditMessage(FxRobot robot) {
        final User userOne = new User().setName("userOne").setId("111-11");
        final User userTwo = new User().setName("userTwo").setId("222-22");

        notificationService.register(userOne);
        notificationService.register(userTwo);

        final String serverName = "Test";
        final String serverId = "1";
        final String categoryName = "Cat";
        final String categoryId = "Category1";
        final String channelOneId = "C1";

        editor.getOrCreateAccord()
            .withOtherUsers(userOne);

        editor.getOrCreateAccord()
            .withOtherUsers(userTwo);

        Server server = editor.getOrCreateServer(serverId, serverName);
        Category category = editor.getOrCreateCategory(categoryId, categoryName, server);
        Channel channel = editor.getOrCreateChannel(channelOneId, "ChannelOne", "text", category);
        server.withCategories(category);
        server.withChannels(channel);
        notificationService.register(channel);

        RouteArgs args = new RouteArgs().addArgument(":id", serverId);
        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
        WaitForAsyncUtils.waitForFxEvents();

        webSocketService.addServerWebSocket(serverId);

        currentUser.withAvailableServers(server);
        userOne.withAvailableServers(server);
        userTwo.withAvailableServers(server);

        RouteArgs routeArgs = new RouteArgs()
            .addArgument(":id", serverId)
            .addArgument(":categoryId", categoryId)
            .addArgument(":channelId", channelOneId);
        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER + Constants.ROUTE_CHANNEL, routeArgs));
        WaitForAsyncUtils.waitForFxEvents();

        verify(webSocketClientFactoryMock, times(4))
            .create(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();

        for (int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }
        WSCallback systemCallback = endpointCallbackHashmap
            .get(Constants.WS_USER_PATH + currentUser.getName() + Constants.WS_SERVER_CHAT_PATH + server.getId());

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

        String messageId = editableMessages.get(0).getId();

        robot.point("#message-text-" + messageId);
        robot.point("#edit-message-" + messageId);
        ImageView button = robot.lookup("#edit-message-" + messageId).query();
        button.setVisible(true);
        robot.clickOn("#edit-message-" + messageId);
        robot.clickOn(EditMessageModal.CANCEL_BUTTON);
        button.setVisible(true);
        robot.clickOn("#edit-message-" + messageId);
        JFXTextField messageTextField = robot.lookup(EditMessageModal.ENTER_MESSAGE_TEXT_FIELD).query();
        messageTextField.clear();
        robot.clickOn("#save-button");
        WaitForAsyncUtils.waitForFxEvents();
        String newMessage = "bla";
        robot.clickOn("#message-text-field");
        robot.write(newMessage);
        robot.clickOn("#save-button");
        WaitForAsyncUtils.waitForFxEvents();

        JSONObject j = new JSONObject().put("status", "failure").put("message", "")
            .put("data", new JSONObject());

        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(false);

        verify(restMock)
            .updateMessage(eq(serverId), eq(categoryId), eq(channelOneId), eq(messageId), eq(newMessage), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        newMessage = "edited";
        robot.doubleClickOn("#message-text-field");
        robot.write(newMessage);
        robot.clickOn("#save-button");
        WaitForAsyncUtils.waitForFxEvents();

        j = new JSONObject().put("status", "success").put("message", "")
            .put("data", new JSONObject());

        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock)
            .updateMessage(eq(serverId), eq(categoryId), eq(channelOneId), eq(messageId), eq(newMessage), callbackCaptor.capture());
        callback = callbackCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        JsonObject jsonObject = Json.createObjectBuilder()
            .add("action", "messageUpdated")
            .add("data", Json.createObjectBuilder()
                .add("id", messageId)
                .add("category", categoryId)
                .add("channel", channelOneId)
                .add("text", newMessage)
                .build())
            .build();

        systemCallback = endpointCallbackHashmap.get(Constants.WS_SYSTEM_PATH + Constants.WS_SERVER_SYSTEM_PATH + serverId);
        systemCallback.handleMessage(jsonObject);
        WaitForAsyncUtils.waitForFxEvents();

        Assertions.assertEquals(newMessage, editableMessages.get(0).getMessage());
    }

    @AfterEach
    void tear() {
        restMock = null;
        webSocketClientFactoryMock = null;
        router = null;
        editor = null;
        currentUser = null;
        notificationService = null;
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
