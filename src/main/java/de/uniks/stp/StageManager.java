package de.uniks.stp;

import de.uniks.stp.controller.ControllerInterface;
import de.uniks.stp.controller.MainScreenController;
import de.uniks.stp.controller.LoginScreenController;
import de.uniks.stp.view.Views;
import de.uniks.stp.network.UserKeyProvider;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import kong.unirest.Unirest;

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
        showLoginScreen();
        stage.show();
    }

    public static void showMainScreen() {
        Parent root = ViewLoader.loadView(Views.MAIN_SCREEN);
        Scene scene = new Scene(root);

        currentController = new MainScreenController(root);
        currentController.init();

        stage.setScene(scene);
        stage.centerOnScreen();
    }

    @Override
    public void stop() {
        try {
            super.stop();
            // Logout

            Unirest.shutDown();

        } catch (Exception e) {
            System.err.println("Error while trying to shutdown");
            e.printStackTrace();
        }

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
}
