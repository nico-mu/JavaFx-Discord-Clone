package de.uniks.stp.accordsettings;

import de.uniks.stp.AccordApp;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.dagger.components.test.AppTestComponent;
import de.uniks.stp.dagger.components.test.SessionTestComponent;
import de.uniks.stp.model.User;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(ApplicationExtension.class)
public class AudioSettingsTest {

    private AccordApp app;
    private Router router;
    private Editor editor;

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
        User currentUser = editor.createCurrentUser("Test", true).setId("123-45");
        editor.setCurrentUser(currentUser);

        SessionTestComponent sessionTestComponent = appTestComponent
            .sessionTestComponentBuilder()
            .currentUser(currentUser)
            .userKey("123-45")
            .build();
        app.setSessionComponent(sessionTestComponent);
    }

    /**
     * Tests changing audio output volume and saving the new value.
     * @param robot
     */
    @Test
    public void changeOutputVolumeTest(FxRobot robot) {
        // open SettingsModal
        RouteArgs args = new RouteArgs();
        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN, args));
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#settings-gear-container");

        // change output volume
        Slider audioSlider = robot.lookup("#slider-output-volume").query();
        double oldValue = audioSlider.getValue();
        robot.clickOn(audioSlider);
        if (oldValue != 0.0d){
            robot.press(KeyCode.LEFT);
        } else{
            robot.press(KeyCode.RIGHT);
        }

        // check for change
        double newValue = audioSlider.getValue();
        Assertions.assertNotEquals(oldValue, audioSlider.getValue());
        robot.clickOn("#settings-apply-button");
        robot.clickOn("#logout-button");

        // check for change still there after logout
        Platform.runLater(() -> router.route(Constants.ROUTE_MAIN, args));
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn("#settings-gear-container");
        Assertions.assertEquals(newValue, audioSlider.getValue());
    }

    @AfterEach
    void tear() {
        router = null;
        Platform.runLater(app::stop);
        WaitForAsyncUtils.waitForFxEvents();
        app = null;
        editor = null;
    }
}
