package de.uniks.stp.controller;

import de.uniks.stp.AccordApp;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.component.ChatMessage;
import de.uniks.stp.component.ListComponent;
import de.uniks.stp.dagger.components.test.AppTestComponent;
import de.uniks.stp.dagger.components.test.SessionTestComponent;
import de.uniks.stp.model.*;
import de.uniks.stp.network.rest.SessionRestClient;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.scene.layout.VBox;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(ApplicationExtension.class)
public class LoadOldMessagesTest {
    @Mock
    private HttpResponse<JsonNode> res;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;

    private static int idCounter = 0;

    private AccordApp app;
    private Router router;
    private Editor editor;
    private SessionRestClient restMock;

    @Start
    public void start(Stage stage) {
        // start application
        MockitoAnnotations.initMocks(this);
        app = new AccordApp();
        app.setTestMode(true);
        app.start(stage);

        AppTestComponent appTestComponent = (AppTestComponent) app.getAppComponent();
        router = appTestComponent.getRouter();
        editor = appTestComponent.getEditor();
        User currentUser = editor.createCurrentUser("Platti", true).setId("1-1");
        editor.setCurrentUser(currentUser);

        SessionTestComponent sessionTestComponent = appTestComponent
            .sessionTestComponentBuilder()
            .currentUser(currentUser)
            .userKey("123-45")
            .build();
        app.setSessionComponent(sessionTestComponent);

        restMock = sessionTestComponent.getSessionRestClient();
    }

    @Test
    public void testLoadAllOldServerMessages(FxRobot robot) {
        // prepare start situation
        long timeStart = new Date().getTime();
        String userNameOne = "Bob";
        String userNameTwo = "Eve";

        String serverId ="12345678";
        User userOne = new User().setName(userNameOne).setId("1");
        User userTwo = new User().setName(userNameTwo).setId("2");
        editor.getOrCreateAccord().withOtherUsers(userOne, userTwo);
        Server server = new Server().setName("Plattis Server").setId(serverId).withUsers(userOne, userTwo);
        editor.getOrCreateAccord().getCurrentUser().withAvailableServers(server);

        RouteArgs args = new RouteArgs().addArgument(":id", serverId);
        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
        WaitForAsyncUtils.waitForFxEvents();

        String categoryId = "4321";
        Category cat = new Category().setName("Plattis Category").setId(categoryId);
        cat.setServer(server);
        String channelId = "42";
        Channel channel = new Channel().setName("Plattis Channel").setType("text").setId(channelId);
        channel.setCategory(cat);
        WaitForAsyncUtils.waitForFxEvents();

        // old messages will be loaded automatically
        JSONObject msg1 = getMessageAsJson(channelId, timeStart - 50, "first old message", userNameOne);
        JSONObject msg2 = getMessageAsJson(channelId, timeStart - 30, "first old message", userNameTwo);
        JSONObject msg3 = getMessageAsJson(channelId, timeStart - 10, "third old message", userNameOne);
        JSONArray data = new JSONArray().put(msg2).put(msg1).put(msg3);

        JSONObject j = new JSONObject().put("status", "success").put("message", "").put("data", data);
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock).getServerChannelMessages(eq(serverId), eq(categoryId), eq(channelId), anyLong(), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        SortedSet<ServerMessage> channelMessages = editor.getChannel(channelId, server).getMessages();
        List<ServerMessage> channelMessagesList = new ArrayList<>(channelMessages);
        // check for correct reactions
        Assertions.assertEquals(3, channelMessages.size());

        // check for correct order of messages
        ListComponent<ServerMessage, ChatMessage> messageList = robot.lookup("#message-list").query();
        VBox messageContainer = (VBox) messageList.lookup("#container");


        ChatMessage firstShownChatMessage = (ChatMessage) messageContainer.getChildren().get(0);
        ServerMessage serverMessage1 = channelMessagesList.get(0);
        Assertions.assertEquals(messageList.getElement(serverMessage1), firstShownChatMessage);

        ChatMessage secondShownChatMessage = (ChatMessage) messageContainer.getChildren().get(1);
        ServerMessage serverMessage2 = channelMessagesList.get(1);
        Assertions.assertEquals(messageList.getElement(serverMessage2), secondShownChatMessage);

        ChatMessage thirdShownChatMessage = (ChatMessage) messageContainer.getChildren().get(2);
        ServerMessage serverMessage3 = channelMessagesList.get(2);
        Assertions.assertEquals(messageList.getElement(serverMessage3), thirdShownChatMessage);
    }

    private JSONObject getMessageAsJson(String channelId, long timestamp, String text, String from) {
        return new JSONObject().put("id", Integer.toString(idCounter++))
            .put("channel", channelId)
            .put("timestamp", timestamp)
            .put("from", from)
            .put("text", text);
    }

    @Test
    public void testLoadSomeOldServerMessages(FxRobot robot) {
        // prepare start situation
        long timeStart = new Date().getTime();
        String userNameOne = "Eve";
        String userNameTwo = "Bob";

        String serverId ="12345678";
        User user = new User().setName(userNameOne).setId("1");
        User userTwo = new User().setName(userNameTwo).setId("2");
        Server server = new Server().setName("Plattis Server").setId(serverId).withUsers(user);
        editor.getOrCreateAccord().getCurrentUser().withAvailableServers(server);
        editor.getOrCreateAccord().withOtherUsers(user, userTwo);

        RouteArgs args = new RouteArgs().addArgument(":id", serverId);
        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
        WaitForAsyncUtils.waitForFxEvents();

        String categoryId = "4321";
        Category cat = new Category().setName("Plattis Category").setId(categoryId);
        cat.setServer(server);
        String channelId = "42";
        Channel channel = new Channel().setName("Plattis Channel").setType("text").setId(channelId);
        channel.setCategory(cat);
        WaitForAsyncUtils.waitForFxEvents();

        // old messages will be loaded automatically
        JSONArray data = new JSONArray();
        JSONObject msg;

        for(int i = 0; i < 50; i++){
            msg = getMessageAsJson(channelId, timeStart-100+i, Integer.toString(i), userNameOne);
            data.put(msg);
        }

        JSONObject j = new JSONObject().put("status", "success").put("message", "").put("data", data);
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock).getServerChannelMessages(eq(serverId), eq(categoryId), eq(channelId), anyLong(), callbackCaptor.capture());
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        SortedSet<ServerMessage> channelMessages = editor.getChannel(channelId, server).getMessages();
        List<ServerMessage> channelMessagesList = new ArrayList<>(channelMessages);

        // check for correct reactions
        Assertions.assertEquals(50, channelMessages.size());

        // check for correct order of messages
        ListComponent<ServerMessage, ChatMessage> messageList = robot.lookup("#message-list").query();
        VBox messageContainer = (VBox) messageList.lookup("#container");

        // check for first and last message correct shown
        ChatMessage firstShownChatMessage = (ChatMessage) messageContainer.getChildren().get(0);
        ServerMessage serverMessage1 = channelMessagesList.get(0);
        Assertions.assertEquals(messageList.getElement(serverMessage1), firstShownChatMessage);

        ChatMessage lastShownMessage = (ChatMessage) messageContainer.getChildren().get(49);
        ServerMessage serverMessage49 = channelMessagesList.get(49);
        Assertions.assertEquals(messageList.getElement(serverMessage49), lastShownMessage);
    }

    @AfterEach
    void tear(){
        restMock = null;
        res = null;
        callbackCaptor = null;
        editor = null;
        router = null;
        Platform.runLater(app::stop);
        WaitForAsyncUtils.waitForFxEvents();
        app = null;
    }
}
