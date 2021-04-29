package de.uniks.stp;

import de.uniks.stp.controller.ControllerInterface;
import de.uniks.stp.controller.LoginScreenController;
import de.uniks.stp.view.ViewLoader;
import de.uniks.stp.view.Views;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.Objects;

public class StageManager extends Application {
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
        showLoginScreen();
        stage.show();
    }

    public static void showLoginScreen() {
        cleanup();

        Parent root = ViewLoader.loadView(Views.LOGIN_SCREEN2);
        if (Objects.isNull(root)) {
            System.err.println("Error while loading LoginScreen");
            return;
        }
        Scene scene = new Scene(root);

        currentController = new LoginScreenController(root);
        currentController.init();

        stage.setTitle("Accord - Login");
        stage.setScene(scene);
        stage.centerOnScreen();
    }
}
