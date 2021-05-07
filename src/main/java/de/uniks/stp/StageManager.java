package de.uniks.stp;

import de.uniks.stp.controller.*;
import de.uniks.stp.router.RouteInfo;
import de.uniks.stp.router.Router;
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
        //showLoginScreen();
        Router.route("/login");
        stage.show();
    }

    public static void route(RouteInfo routeInfo) {
        cleanup();
        Parent root;
        Scene scene;
        String subroute = routeInfo.getSubControllerRoute();

        if(subroute.equals("/main")) {
            root = ViewLoader.loadView(Views.MAIN_SCREEN);
            currentController = new MainScreenController(root, editor);
            currentController.init();
            scene = new Scene(root);
            stage.setTitle("Accord");
            stage.setScene(scene);
            stage.centerOnScreen();
        }
        else if(subroute.equals("/login")) {
            root = ViewLoader.loadView(Views.LOGIN_SCREEN);
            currentController = new LoginScreenController(root, editor);
            currentController.init();
            scene = new Scene(root);
            stage.setTitle("Accord");
            stage.setScene(scene);
            stage.centerOnScreen();
        }

        Router.addToControllerCache(routeInfo.getFullRoute(), currentController);
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
}
