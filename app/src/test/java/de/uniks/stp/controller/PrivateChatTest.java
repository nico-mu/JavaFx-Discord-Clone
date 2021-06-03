package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.StageManager;
import de.uniks.stp.component.PrivateChatView;
import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.jpa.model.DirectMessageDTO;
import de.uniks.stp.model.User;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.network.WSCallback;
import de.uniks.stp.network.WebSocketClient;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(ApplicationExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class PrivateChatTest {
    @Mock
    private RestClient restMock;

    @Mock
    private WebSocketClient webSocketMock;

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

    private String generateRandomString() {
        return UUID.randomUUID().toString();
    }

    @Test
    public void testPrivateMessage(FxRobot robot) {
        Editor editor = StageManager.getEditor();

        User currentUser = new User().setName(generateRandomString()).setId(generateRandomString());
        User otherUser = new User().setName(generateRandomString()).setId(generateRandomString());

        editor.getOrCreateAccord()
            .setCurrentUser(currentUser)
            .setUserKey(generateRandomString())
            .withOtherUsers(otherUser);

        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME));
        WaitForAsyncUtils.waitForFxEvents();

        verify(webSocketMock, times(2)).inject(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());
        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();
        for (int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }
        WSCallback currentUserCallback = endpointCallbackHashmap.get(Constants.WS_USER_PATH + currentUser.getName());

        DatabaseService.clearAllConversations();

        JsonObject message = Json.createObjectBuilder()
            .add("channel", "private")
            .add("timestamp", 1)
            .add("message", "Test")
            .add("from", otherUser.getName())
            .add("to", currentUser.getName())
            .build();
        currentUserCallback.handleMessage(message);
        message = Json.createObjectBuilder()
            .add("channel", "private")
            .add("timestamp", 2)
            .add("message", "Test")
            .add("from", otherUser.getName())
            .add("to", currentUser.getName())
            .build();
        currentUserCallback.handleMessage(message);

        List<DirectMessageDTO> directMessages = DatabaseService.getConversation(currentUser.getName(), otherUser.getName());
        Assertions.assertEquals(2, directMessages.size());

        Assertions.assertEquals(1, editor.getOrCreateAccord().getCurrentUser().getChatPartner().size());

        WaitForAsyncUtils.waitForFxEvents();
    }
}
