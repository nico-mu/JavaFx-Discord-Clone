package de.uniks.stp.controller;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.AccordApp;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.component.KeyBasedComboBox;
import de.uniks.stp.dagger.components.test.AppTestComponent;
import de.uniks.stp.dagger.components.test.SessionTestComponent;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.modal.SettingsModal;
import de.uniks.stp.model.User;
import de.uniks.stp.network.rest.SessionRestClient;
import de.uniks.stp.network.voice.VoiceChatClientFactory;
import de.uniks.stp.network.voice.VoiceChatService;
import de.uniks.stp.network.websocket.WSCallback;
import de.uniks.stp.network.websocket.WebSocketClientFactory;
import de.uniks.stp.network.websocket.WebSocketService;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.util.HashMap;

import static org.mockito.Mockito.doReturn;

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
    private SessionDatabaseService sessionDatabaseService;
    private VoiceChatClientFactory voiceChatClientFactory;
    private VoiceChatService voiceChatService;

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

        sessionDatabaseService = sessionTestComponent.getSessionDatabaseService();
        voiceChatClientFactory = sessionTestComponent.getVoiceChatClientFactory();

        voiceChatService = Mockito.spy(new VoiceChatService(voiceChatClientFactory, sessionDatabaseService));
    }

    @Test
    public void changeLanguageAndNotificationSoundTest(FxRobot robot) {
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

    /**
     * Tests changing audio input & output volume and saving the new value.
     * @param robot
     */
    @Test
    public void changeInputAndOutputVolumeTest(FxRobot robot) {
        // open SettingsModal
        RouteArgs args = new RouteArgs();
        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN, args));
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#settings-gear-container");

        // change input volume
        Slider inputSlider = robot.lookup("#slider-input-volume").query();
        double oldInputValue = inputSlider.getValue();
        System.out.println(oldInputValue);
        robot.clickOn(inputSlider);
        if (oldInputValue != 0.0d){
            robot.press(KeyCode.LEFT);
            robot.release(KeyCode.LEFT);
        } else{
            robot.press(KeyCode.RIGHT);
            robot.release(KeyCode.RIGHT);
        }
        // change output volume
        Slider outputSlider = robot.lookup("#slider-output-volume").query();
        double oldOutputValue = outputSlider.getValue();
        robot.clickOn(outputSlider);
        if (oldOutputValue != 0.0d){
            robot.press(KeyCode.LEFT);
            robot.release(KeyCode.LEFT);
        } else{
            robot.press(KeyCode.RIGHT);
            robot.release(KeyCode.RIGHT);
        }

        // check for change
        double newInputValue = inputSlider.getValue();
        System.out.println(newInputValue);
        Assertions.assertNotEquals(oldInputValue, newInputValue);
        double newOutputValue = outputSlider.getValue();
        Assertions.assertNotEquals(oldOutputValue, newOutputValue);
        robot.clickOn("#settings-apply-button");
        WaitForAsyncUtils.waitForFxEvents();

        // check for changes still there when opening modal again
        robot.clickOn("#settings-gear-container");
        Assertions.assertEquals(newInputValue, inputSlider.getValue());
        Assertions.assertEquals(newOutputValue, outputSlider.getValue());
    }
    /**
     * Tests changing audio input sensitivity and saving the new value.
     * @param robot
     */
    @Test
    public void changeInputSensitivityTest(FxRobot robot) {
        // open SettingsModal
        RouteArgs args = new RouteArgs();
        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN, args));
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#settings-gear-container");

        // change input volume
        Slider inputSlider = robot.lookup(SettingsModal.SETTINGS_SLIDER_INPUT_SENSITIVITY).query();
        double oldInputValue = inputSlider.getValue();

        robot.clickOn(inputSlider);
        if (oldInputValue != inputSlider.getMin()){
            robot.press(KeyCode.LEFT);
            robot.release(KeyCode.LEFT);
        } else{
            robot.press(KeyCode.RIGHT);
            robot.release(KeyCode.RIGHT);
        }

        // check for change
        double newInputValue = inputSlider.getValue();
        Assertions.assertNotEquals(oldInputValue, newInputValue);
        robot.clickOn(SettingsModal.SETTINGS_APPLY_BUTTON);
        WaitForAsyncUtils.waitForFxEvents();

        // check for changes still there when opening modal again
        robot.clickOn("#settings-gear-container");
        Assertions.assertEquals(newInputValue, inputSlider.getValue());
    }

    @Test
    public void microphoneTestTest(FxRobot  robot) {
        SourceDataLine sourceDataLineMock = Mockito.mock(SourceDataLine.class);
        TargetDataLine targetDataLineMock = Mockito.mock(TargetDataLine.class);
        try {
            doReturn(sourceDataLineMock).when(voiceChatService).createUsableSourceDataLine();
            doReturn(targetDataLineMock).when(voiceChatService).createUsableTargetDataLine();
        } catch (LineUnavailableException ignored) { }
        doReturn(true).when(voiceChatService).isMicrophoneAvailable();
        doReturn(true).when(voiceChatService).isSpeakerAvailable();

        // open SettingsModal
        RouteArgs args = new RouteArgs();
        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN, args));
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#settings-gear-container");

        JFXButton button = robot.lookup(SettingsModal.SETTINGS_MICROPHONE_TEST_BUTTON).query();
        String oldButtonText = button.getText();
        robot.clickOn(button);
        Assertions.assertNotEquals(oldButtonText, button.getText());

        final ProgressBar bar = robot.lookup(SettingsModal.SETTINGS_PROGRESS_BAR_INPUT_SENSITIVITY).query();
        Assertions.assertEquals(0d, bar.getProgress());

        oldButtonText = button.getText();
        robot.clickOn(button);
        Assertions.assertNotEquals(oldButtonText, button.getText());

        final Slider outputVolumeSlider = robot.lookup(SettingsModal.SETTINGS_SLIDER_OUTPUT_VOLUME).query();
        final Slider inputVolumeSlider = robot.lookup(SettingsModal.SETTINGS_SLIDER_INPUT_VOLUME).query();
        voiceChatService.startMicrophoneTest(inputVolumeSlider, outputVolumeSlider, bar);
        voiceChatService.stopMicrophoneTest();

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
        sessionDatabaseService = null;
        voiceChatClientFactory = null;
        voiceChatService = null;
        Platform.runLater(app::stop);
        WaitForAsyncUtils.waitForFxEvents();
        app = null;
    }
}
