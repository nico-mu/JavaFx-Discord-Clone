package de.uniks.stp.controller;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.StageManager;
import de.uniks.stp.component.ServerChatMessage;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.network.*;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(ApplicationExtension.class)
public class LoadOldMessagesTest {
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
    public void testLoadAllOldServerMessages(FxRobot robot) {
        // prepare start situation
        Editor editor = StageManager.getEditor();
        long timeStart = new Date().getTime();

        editor.getOrCreateAccord().setCurrentUser(new User().setName("Platti")).setUserKey("123-45");

        String serverId ="12345678";
        Server server = new Server().setName("Plattis Server").setId(serverId);
        editor.getOrCreateAccord().getCurrentUser().withAvailableServers(server);

        WebSocketService.addServerWebSocket(serverId);

        RouteArgs args = new RouteArgs().addArgument(":id", serverId);
        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
        WaitForAsyncUtils.waitForFxEvents();

        String categoryId = "4321";
        Category cat = new Category().setName("Plattis Category").setId(categoryId);
        cat.setServer(server);
        String channelId = "42";
        Channel channel = new Channel().setName("Plattis Channel").setId(channelId);
        channel.setCategory(cat);
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#chatViewMessageInput");
        String latestMessage = "Latest message";
        robot.write(latestMessage);
        robot.clickOn("#chatViewSubmitButton");
        WaitForAsyncUtils.waitForFxEvents();

        // assert correct start situation
        Assertions.assertEquals(1, editor.getChannel(channelId, server).getMessages().size());
        VBox chatVBox = robot.lookup("#chatVBox").query();
        Assertions.assertEquals(2, chatVBox.getChildren().size());  //asserts loadMessagesButton in view

        VBox messageList = robot.lookup("#messageList").query();
        ServerChatMessage firstShownChatMessage = (ServerChatMessage) messageList.getChildren().get(0);
        VBox messageContainer = (VBox) firstShownChatMessage.getChildren().get(0);
        Text messageText = (Text) messageContainer.getChildren().get(1);
        Assertions.assertEquals(latestMessage, messageText.getText());

        // load old messages
        robot.clickOn("#loadMessagesButton");

        String firstOldMessage = "first old message";
        JSONObject msg1 = new JSONObject().put("id", "78345")
            .put("channel", channelId)
            .put("timestamp", timeStart-50)
            .put("from", "Bob")
            .put("text", firstOldMessage);

        String secondOldMessage = "first old message";
        JSONObject msg2 = new JSONObject().put("id", "356743")
            .put("channel", channelId)
            .put("timestamp", timeStart-30)
            .put("from", "Eve")
            .put("text", secondOldMessage);
        JSONArray data = new JSONArray().put(msg2).put(msg1);

        JSONObject j = new JSONObject().put("status", "success").put("message", "").put("data", data);
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock).getServerChannelMessages(eq(serverId), eq(categoryId), eq(channelId), anyLong(), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        // check for correct reactions
        Assertions.assertEquals(3, editor.getChannel(channelId, server).getMessages().size());
        Assertions.assertEquals(1, chatVBox.getChildren().size());  //check for loadMessagesButton removed

        // check for correct order of messages
        firstShownChatMessage = (ServerChatMessage) messageList.getChildren().get(0);
        messageContainer = (VBox) firstShownChatMessage.getChildren().get(0);
        messageText = (Text) messageContainer.getChildren().get(1);
        Assertions.assertEquals(firstOldMessage, messageText.getText());

        ServerChatMessage secondShownChatMessage = (ServerChatMessage) messageList.getChildren().get(1);
        messageContainer = (VBox) secondShownChatMessage.getChildren().get(0);
        messageText = (Text) messageContainer.getChildren().get(1);
        Assertions.assertEquals(secondOldMessage, messageText.getText());

        ServerChatMessage thirdShownChatMessage = (ServerChatMessage) messageList.getChildren().get(2);
        messageContainer = (VBox) thirdShownChatMessage.getChildren().get(0);
        messageText = (Text) messageContainer.getChildren().get(1);
        Assertions.assertEquals(latestMessage, messageText.getText());
    }

    @Test
    public void testLoadSomeOldServerMessages(FxRobot robot) {
        // prepare start situation
        Editor editor = StageManager.getEditor();
        long timeStart = new Date().getTime();

        editor.getOrCreateAccord().setCurrentUser(new User().setName("Platti")).setUserKey("123-45");

        String serverId ="12345678";
        Server server = new Server().setName("Plattis Server").setId(serverId);
        editor.getOrCreateAccord().getCurrentUser().withAvailableServers(server);

        WebSocketService.addServerWebSocket(serverId);

        RouteArgs args = new RouteArgs().addArgument(":id", serverId);
        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
        WaitForAsyncUtils.waitForFxEvents();

        String categoryId = "4321";
        Category cat = new Category().setName("Plattis Category").setId(categoryId);
        cat.setServer(server);
        String channelId = "42";
        Channel channel = new Channel().setName("Plattis Channel").setId(channelId);
        channel.setCategory(cat);
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#chatViewMessageInput");
        String message = "Hey there";
        robot.write(message);
        robot.clickOn("#chatViewSubmitButton");
        WaitForAsyncUtils.waitForFxEvents();

        // assert correct start situation
        Assertions.assertEquals(1, editor.getChannel(channelId, server).getMessages().size());
        VBox chatVBox = robot.lookup("#chatVBox").query();
        Assertions.assertEquals(2, chatVBox.getChildren().size());  //asserts loadMessagesButton in view

        // check for message shown
        VBox messageList = robot.lookup("#messageList").query();
        ServerChatMessage firstShownChatMessage = (ServerChatMessage) messageList.getChildren().get(0);
        VBox messageContainer = (VBox) firstShownChatMessage.getChildren().get(0);
        Text messageText = (Text) messageContainer.getChildren().get(1);
        Assertions.assertEquals(message, messageText.getText());

        // load old messages
        robot.clickOn("#loadMessagesButton");

        JSONArray data = new JSONArray();
        for(int i=0;i<50;i++){
            JSONObject msg = new JSONObject().put("id", Integer.toString(i))
                .put("channel", channelId)
                .put("timestamp", timeStart-100+i)
                .put("from", "Eve")
                .put("text", Integer.toString(i));
            data.put(msg);
        }
        JSONObject j = new JSONObject().put("status", "success").put("message", "").put("data", data);
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock).getServerChannelMessages(eq(serverId), eq(categoryId), eq(channelId), anyLong(), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        // check for correct reactions
        Assertions.assertEquals(51, editor.getChannel(channelId, server).getMessages().size());
        Assertions.assertEquals(2, chatVBox.getChildren().size());  //check for loadMessagesButton still shown

        // check for first and last message correct shown
        firstShownChatMessage = (ServerChatMessage) messageList.getChildren().get(0);
        messageContainer = (VBox) firstShownChatMessage.getChildren().get(0);
        messageText = (Text) messageContainer.getChildren().get(1);
        Assertions.assertEquals("0", messageText.getText());

        ServerChatMessage lastShownChatMessage = (ServerChatMessage) messageList.getChildren().get(50);
        messageContainer = (VBox) lastShownChatMessage.getChildren().get(0);
        messageText = (Text) messageContainer.getChildren().get(1);
        Assertions.assertEquals(message, messageText.getText());
    }
}
