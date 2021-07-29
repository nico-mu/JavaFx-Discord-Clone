package de.uniks.stp.controller;

import de.uniks.stp.AccordApp;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.component.KeyBasedComboBox;
import de.uniks.stp.dagger.components.test.AppTestComponent;
import de.uniks.stp.dagger.components.test.SessionTestComponent;
import de.uniks.stp.modal.SettingsModal;
import de.uniks.stp.model.User;
import de.uniks.stp.network.rest.SessionRestClient;
import de.uniks.stp.network.websocket.WSCallback;
import de.uniks.stp.network.websocket.WebSocketClientFactory;
import de.uniks.stp.network.websocket.WebSocketService;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
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

import java.util.HashMap;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(ApplicationExtension.class)
public class SettingsModalTest {
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
        User currentUser = editor.createCurrentUser("TestUser1", true).setId("1");
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

    @Test
    public void SettingsTest(FxRobot robot) {
        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_LIST_ONLINE_USERS, new RouteArgs()));
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#settings-gear");
        robot.clickOn("#settings-cancel-button");
        robot.clickOn("#settings-gear");

        KeyBasedComboBox languageComboBox = robot.lookup("#combo-select-language").query();
        Platform.runLater(() -> languageComboBox.setSelection("en"));
        WaitForAsyncUtils.waitForFxEvents();

        KeyBasedComboBox notificationSoundComboBox = robot.lookup("#combo-select-notification-sound").query();
        Platform.runLater(() -> notificationSoundComboBox.setSelection("light-button.wav"));
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("#settings-apply-button");

        robot.clickOn("#settings-gear");

        KeyBasedComboBox languageComboBox2 = robot.lookup("#combo-select-language").query();
        Assertions.assertEquals("en", languageComboBox2.getSelection());
        Platform.runLater(() -> languageComboBox2.setSelection("de"));
        WaitForAsyncUtils.waitForFxEvents();

        KeyBasedComboBox notificationSoundComboBox2 = robot.lookup("#combo-select-notification-sound").query();
        Assertions.assertEquals("light-button.wav", notificationSoundComboBox2.getSelection());
        Platform.runLater(() -> notificationSoundComboBox2.setSelection("gaming-lock.wav"));
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn(SettingsModal.SETTINGS_COMBO_SELECT_INPUT_DEVICE);
        robot.clickOn(SettingsModal.SETTINGS_COMBO_SELECT_OUTPUT_DEVICE);

        robot.clickOn("#settings-apply-button");
    }

    @AfterEach
    void tear(){
        restMock = null;
        webSocketClientFactoryMock = null;
        editor = null;
        webSocketService = null;
        router = null;
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
