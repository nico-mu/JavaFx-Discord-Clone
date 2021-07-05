package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.StageManager;
import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.network.*;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.scene.Parent;
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
import java.util.Objects;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(ApplicationExtension.class)
public class ServerVoiceChannelTest {
    @Mock
    private RestClient restMock;

    @Mock
    private WebSocketClient webSocketMock;

    @Mock
    private VoiceChatClient voiceChatClientMock;

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
    private Editor editor;

    @Start
    public void start(Stage stage) {
        // start application
        MockitoAnnotations.initMocks(this);
        NetworkClientInjector.setRestClient(restMock);
        NetworkClientInjector.setWebSocketClient(webSocketMock);
        NetworkClientInjector.setVoiceChatClient(voiceChatClientMock);
        StageManager.setBackupMode(false);
        endpointCallbackHashmap = new HashMap<>();
        app = new StageManager();
        app.start(stage);
        editor = StageManager.getEditor();
        DatabaseService.clearAllConversations();
    }

    @Test
    public void testAudioMemberJoin(FxRobot robot) {
        final String serverId = UUID.randomUUID().toString();
        final String categoryId = UUID.randomUUID().toString();
        final String channelId = UUID.randomUUID().toString();
        final String currentUserId = UUID.randomUUID().toString();
        final Channel channel = initAndEnterVoiceChannel(currentUserId, serverId, categoryId, channelId);

        WebSocketService.addServerWebSocket(serverId);

        JsonObject jsonObject = Json.createObjectBuilder()
            .add("action", "audioJoined")
            .add("data",
                Json.createObjectBuilder()
                    .add("id", currentUserId)
                    .add("category", categoryId)
                    .add("channel", channelId)
                    .build()
            )
            .build();

        verify(webSocketMock, times(4)).inject(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());
        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();

        for (int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }

        Parent voiceChannelUsersContainer = robot.lookup(ServerVoiceChatController.VOICE_CHANNEL_USER_CONTAINER_ID).query();
        int voiceChannelUserCount = voiceChannelUsersContainer.getChildrenUnmodifiable().size();
        Assertions.assertEquals(0, voiceChannelUserCount);

        JSONObject response = new JSONObject()
            .put("status", "success")
            .put("message", "Successfully joined audio channel, please open an UDP connection to ")
            .put("data", new JSONObject());

        verify(restMock).joinAudioChannel(eq(channel), callbackCaptor.capture());
        when(res.getBody()).thenReturn(new JsonNode(response.toString()));
        when(res.isSuccess()).thenReturn(true);
        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);


        WSCallback systemCallback = endpointCallbackHashmap.get(Constants.WS_SYSTEM_PATH + Constants.WS_SERVER_SYSTEM_PATH + serverId);
        systemCallback.handleMessage(jsonObject);

        WaitForAsyncUtils.waitForFxEvents();

        voiceChannelUserCount = voiceChannelUsersContainer.getChildrenUnmodifiable().size();
        Assertions.assertEquals(1, voiceChannelUserCount);
    }

    private Channel initAndEnterVoiceChannel(String currentUserId, String serverId, String categoryId, String channelId) {
        final Channel channel = initServerWithVoiceChannel(currentUserId, serverId, categoryId, channelId);

        final RouteArgs args = new RouteArgs()
            .addArgument(":id", serverId)
            .addArgument(":categoryId", categoryId)
            .addArgument(":channelId", channelId);
        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER + Constants.ROUTE_CHANNEL, args));
        WaitForAsyncUtils.waitForFxEvents();

        return channel;
    }

    private Channel initServerWithVoiceChannel(String currentUserId, String serverId, String categoryId, String channelId) {
        final Server server = new Server().setId(serverId).setName("Audio");
        final Category category = new Category().setId(categoryId).setName("Placeholder").setServer(server);
        final Channel channel = new Channel().setName("a1").setType("audio").setId(channelId)
            .setCategory(category).setServer(server);
        editor.getOrCreateAccord().setCurrentUser(
            new User().setName("Test").setId(currentUserId).withAvailableServers(server)
        ).setUserKey(currentUserId);

        return channel;
    }

    @Test
    public void testAudioMemberLeave(FxRobot robot) {
        final String serverId = UUID.randomUUID().toString();
        final String categoryId = UUID.randomUUID().toString();
        final String channelId = UUID.randomUUID().toString();
        final String currentUserId = UUID.randomUUID().toString();
        final String otherUserId = UUID.randomUUID().toString();
        final Channel channel = initServerWithVoiceChannel(currentUserId, serverId, categoryId, channelId);
        final User otherUser = new User().setName("other").setId(otherUserId).withAvailableServers(channel.getServer());
        channel.withAudioMembers(editor.getOrCreateAccord().getCurrentUser(), otherUser);

        final RouteArgs args = new RouteArgs()
            .addArgument(":id", serverId)
            .addArgument(":categoryId", categoryId)
            .addArgument(":channelId", channelId);
        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER + Constants.ROUTE_CHANNEL, args));
        WaitForAsyncUtils.waitForFxEvents();

        Parent voiceChannelUsersContainer = robot.lookup(ServerVoiceChatController.VOICE_CHANNEL_USER_CONTAINER_ID).query();
        int voiceChannelUserCount = voiceChannelUsersContainer.getChildrenUnmodifiable().size();
        Assertions.assertEquals(2, voiceChannelUserCount);


        WebSocketService.addServerWebSocket(serverId);

        JsonObject jsonObject = Json.createObjectBuilder()
            .add("action", "audioLeft")
            .add("data",
                Json.createObjectBuilder()
                    .add("id", otherUserId)
                    .add("category", categoryId)
                    .add("channel", channelId)
                    .build()
            )
            .build();

        verify(webSocketMock, times(4)).inject(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());
        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();

        for (int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }

        WSCallback systemCallback = endpointCallbackHashmap.get(Constants.WS_SYSTEM_PATH + Constants.WS_SERVER_SYSTEM_PATH + serverId);
        systemCallback.handleMessage(jsonObject);

        WaitForAsyncUtils.waitForFxEvents();

        voiceChannelUserCount = voiceChannelUsersContainer.getChildrenUnmodifiable().size();
        Assertions.assertEquals(1, voiceChannelUserCount);
    }

    @AfterEach
    void tear() {
        restMock = null;
        webSocketMock = null;
        res = null;
        callbackCaptor = null;
        stringArgumentCaptor = null;
        wsCallbackArgumentCaptor = null;
        if (Objects.nonNull(endpointCallbackHashmap)) {
            endpointCallbackHashmap.clear();
        }
        endpointCallbackHashmap = null;
        Platform.runLater(app::stop);
        WaitForAsyncUtils.waitForFxEvents();
        app = null;
        editor = null;
    }
}
