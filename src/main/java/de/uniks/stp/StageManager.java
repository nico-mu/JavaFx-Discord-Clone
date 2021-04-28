package de.uniks.stp;

import de.uniks.stp.controller.ControllerInterface;
import de.uniks.stp.network.UserKeyProvider;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Objects;

public class StageManager extends Application {
    private static Editor editor;
    private static Stage stage;
    private static ControllerInterface currentController;

    public static void cleanup() {
        if (Objects.nonNull(currentController)) {
            currentController.stop();
        }
        currentController = null;
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        editor = new Editor();
        UserKeyProvider.setEditor(editor);
        stage.show();
    }
}
