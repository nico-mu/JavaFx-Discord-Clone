package de.uniks.stp;

import de.uniks.stp.controller.ControllerInterface;
import de.uniks.stp.controller.LoginScreenController;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.network.WebSocketService;
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

    public static void showLoginScreen() {
        cleanup();

        Parent root = ViewLoader.loadView(Views.LOGIN_SCREEN);
        if (Objects.isNull(root)) {
            System.err.println("Error while loading LoginScreen");
            return;
        }
        Scene scene = new Scene(root);

        currentController = new LoginScreenController(root, editor);
        currentController.init();

        stage.setTitle("Accord - Login");
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    public static void showHomeScreen() {
        cleanup();

        /*
        Parent root = ViewLoader.loadView(CONSTANT_HOMESCREEN);
        if (Objects.isNull(root)) {
            System.err.println("Error while loading HomeScreen");
            return;
        }
        Scene scene = new Scene(root);

        currentController = new HomeScreenController(root, editor);
        currentController.init();

        stage.setTitle("Accord");
        stage.setScene(scene);
        stage.centerOnScreen();
         */
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        editor = new Editor();
        UserKeyProvider.setEditor(editor);
        WebSocketService.setEditor(editor);

        showLoginScreen();
        stage.show();
    }

    @Override
    public void stop() {
        try {
            super.stop();

            if (currentController != null) {
                currentController.stop();
            }

            RestClient.stop();
            WebSocketService.stop();
        } catch (Exception e) {
            System.err.println("Error while trying to shutdown");
            e.printStackTrace();
        }

    }
}
