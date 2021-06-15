package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.StageManager;
import de.uniks.stp.component.ChatMessage;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.network.WebSocketClient;
import de.uniks.stp.network.WebSocketService;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.scene.layout.VBox;
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

import java.util.Date;

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
    private StageManager app;

    @Start
    public void start(Stage stage) {
        // start application
        MockitoAnnotations.initMocks(this);
        NetworkClientInjector.setRestClient(restMock);
        NetworkClientInjector.setWebSocketClient(webSocketMock);
        StageManager.setBackupMode(false);
        app = new StageManager();
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

        // old messages will be loaded automatically
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

        String thirdOldMessage = "third old message";
        JSONObject msg3 = new JSONObject().put("id", "78345")
            .put("channel", channelId)
            .put("timestamp", timeStart-10)
            .put("from", "Bob")
            .put("text", thirdOldMessage);

        JSONArray data = new JSONArray().put(msg2).put(msg1).put(msg3);

        JSONObject j = new JSONObject().put("status", "success").put("message", "").put("data", data);
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock).getServerChannelMessages(eq(serverId), eq(categoryId), eq(channelId), anyLong(), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        // check for correct reactions
        Assertions.assertEquals(3, editor.getChannel(channelId, server).getMessages().size());
        VBox chatVBox = robot.lookup("#chatVBox").query();
        Assertions.assertEquals(1, chatVBox.getChildren().size());  //check for loadMessagesButton removed

        // check for correct order of messages
        VBox messageList = robot.lookup("#messageList").query();
        ChatMessage firstShownChatMessage = (ChatMessage) messageList.getChildren().get(0);
        VBox messageContainer = (VBox) firstShownChatMessage.getChildren().get(0);
        TextFlow messageText = (TextFlow) messageContainer.getChildren().get(1);
        Assertions.assertEquals(firstOldMessage, ((Text) messageText.getChildren().get(0)).getText());

        ChatMessage secondShownChatMessage = (ChatMessage) messageList.getChildren().get(1);
        messageContainer = (VBox) secondShownChatMessage.getChildren().get(0);
        messageText = (TextFlow) messageContainer.getChildren().get(1);
        Assertions.assertEquals(secondOldMessage, ((Text) messageText.getChildren().get(0)).getText());

        ChatMessage thirdShownChatMessage = (ChatMessage) messageList.getChildren().get(2);
        messageContainer = (VBox) thirdShownChatMessage.getChildren().get(0);
        messageText = (TextFlow) messageContainer.getChildren().get(1);
        Assertions.assertEquals(thirdOldMessage, ((Text) messageText.getChildren().get(0)).getText());
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

        // old messages will be loaded automatically
        JSONArray data = new JSONArray();
        for(int i=0;i<50;i++){
            JSONObject msg = new JSONObject().put("id", Integer.toString(6734-i))
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
        Assertions.assertEquals(50, editor.getChannel(channelId, server).getMessages().size());
        VBox chatVBox = robot.lookup("#chatVBox").query();
        Assertions.assertEquals(2, chatVBox.getChildren().size());  //check for loadMessagesButton still shown

        // check for first and last message correct shown
        VBox messageList = robot.lookup("#messageList").query();
        ChatMessage firstShownChatMessage = (ChatMessage) messageList.getChildren().get(0);
        VBox messageContainer = (VBox) firstShownChatMessage.getChildren().get(0);
        TextFlow messageText = (TextFlow) messageContainer.getChildren().get(1);
        Assertions.assertEquals("0", ((Text) messageText.getChildren().get(0)).getText());

        ChatMessage lastShownChatMessage = (ChatMessage) messageList.getChildren().get(49);
        messageContainer = (VBox) lastShownChatMessage.getChildren().get(0);
        messageText = (TextFlow) messageContainer.getChildren().get(1);
        Assertions.assertEquals("49", ((Text) messageText.getChildren().get(0)).getText());
    }

    @AfterEach
    void tear(){
        restMock = null;
        webSocketMock = null;
        res = null;
        callbackCaptor = null;
        Platform.runLater(app::stop);
        WaitForAsyncUtils.waitForFxEvents();
        app = null;
    }
}
