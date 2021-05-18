package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.StageManager;
import de.uniks.stp.model.User;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.network.WSCallback;
import de.uniks.stp.network.WebSocketClient;
import de.uniks.stp.router.Router;
import de.uniks.stp.network.NetworkClientInjector;
import javafx.application.Platform;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
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

import javax.json.*;
import java.util.*;
import java.util.List;

import static org.mockito.Mockito.*;


@ExtendWith(ApplicationExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class SystemWebsocketTest {
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

    @Test
    public void testUserJoined(FxRobot robot) {

        Editor editor = StageManager.getEditor();

        editor.getOrCreateAccord()
            .setCurrentUser(new User().setName("Test").setId("123-45"))
            .setUserKey("123-45");

        Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_LIST_ONLINE_USERS));
        WaitForAsyncUtils.waitForFxEvents();

        verify(webSocketMock, times(2)).inject(stringArgumentCaptor.capture(), wsCallbackArgumentCaptor.capture());

        List<WSCallback> wsCallbacks = wsCallbackArgumentCaptor.getAllValues();
        List<String> endpoints = stringArgumentCaptor.getAllValues();

        for(int i = 0; i < endpoints.size(); i++) {
            endpointCallbackHashmap.putIfAbsent(endpoints.get(i), wsCallbacks.get(i));
        }
        WSCallback systemCallback = endpointCallbackHashmap.get(Constants.WS_SYSTEM_PATH);

        String testUserName = "testUser";
        JsonObject jsonObject = Json.createObjectBuilder()
            .add("action", "userJoined")
            .add("data",
                Json.createObjectBuilder()
                    .add("name", testUserName)
                    .add("id", "12345678")
                    .build()
            )
            .build();
        systemCallback.handleMessage(jsonObject);

        Assertions.assertEquals(1, editor.getOrCreateAccord().getOtherUsers().size());
        Assertions.assertNotNull(editor.getOtherUser(testUserName));

        Platform.runLater(() -> {
            VBox onlineUsersContainer = robot.lookup("#online-users-container").query();
            Object[] userListEntryLabels = onlineUsersContainer.lookupAll("#user-list-entry-text").toArray();
            Assertions.assertEquals(1, userListEntryLabels.length);
            Assertions.assertEquals(testUserName, ((Text)userListEntryLabels[0]).getText());
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testUserLeft(FxRobot robot) {
        //TODO: write test method
    }

}
