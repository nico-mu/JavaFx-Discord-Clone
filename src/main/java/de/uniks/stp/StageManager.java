package de.uniks.stp;

import de.uniks.stp.controller.ControllerInterface;
import de.uniks.stp.controller.MainScreenController;
import de.uniks.stp.network.UserKeyProvider;
import de.uniks.stp.view.Views;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
        stage.setMaxHeight(640.0d);
        stage.setMaxWidth(850.0d);
        stage.setMinHeight(640.0d);
        stage.setMinWidth(850.0d);
        editor = new Editor();
        UserKeyProvider.setEditor(editor);
        stage.show();
        showMainView();
    }

    private static void showMainView() {
        Parent root = ViewLoader.loadView(Views.MAIN_SCREEN);
        Scene scene = new Scene(root);

        currentController = new MainScreenController(root);
        currentController.init();

        stage.setScene(scene);
        stage.centerOnScreen();
    }
}
