package de.uniks.stp;

import de.uniks.stp.controller.ControllerInterface;
import de.uniks.stp.controller.LoginScreenController;
import de.uniks.stp.controller.MainScreenController;
import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.language.LanguageService;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.network.UserKeyProvider;
import de.uniks.stp.network.WebSocketService;
import de.uniks.stp.router.RouteInfo;
import de.uniks.stp.router.Router;
import de.uniks.stp.view.Views;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class StageManager extends Application {
    private static final Logger log = LoggerFactory.getLogger(StageManager.class);

    private static Editor editor;
    private static Stage stage;
    private static ControllerInterface currentController;
    private static boolean backup = true;

    private static LanguageService languageService;
    private static AudioService audioService;

    public static void cleanup() {
        if (Objects.nonNull(currentController)) {
            currentController.stop();
        }
        currentController = null;
    }

    public static void route(RouteInfo routeInfo) {
        cleanup();
        Parent root;
        Scene scene;
        String subroute = routeInfo.getSubControllerRoute();

        if (subroute.equals(Constants.ROUTE_MAIN)) {
            root = ViewLoader.loadView(Views.MAIN_SCREEN);
            currentController = new MainScreenController(root, editor);
            currentController.init();
            scene = new Scene(root);
            scene.getStylesheets().add(StageManager.class.getResource("/de/uniks/stp/style/css/component/context-menu.css").toExternalForm());
            stage.setTitle("Accord");
            stage.setScene(scene);
            if (routeInfo.getCurrentControllerRoute().equals("") || routeInfo.getCurrentControllerRoute().equals(Constants.ROUTE_LOGIN)) {
                stage.centerOnScreen();
            }
        } else if (subroute.equals(Constants.ROUTE_LOGIN)) {
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

    public static Editor getEditor() {
        return editor;
    }

    public static Stage getStage() {
        return stage;
    }

    public static void setBackupMode(boolean mode) {
        backup = mode;
    }

    public static LanguageService getLanguageService() {
        return languageService;
    }

    public static void setLanguageService(LanguageService languageService) {
        StageManager.languageService = languageService;
    }

    public static AudioService getAudioService() {
        return audioService;
    }

    public static void setAudioService(AudioService audioService) {
        StageManager.audioService = audioService;
    }


    @Override
    public void start(Stage primaryStage) {
        DatabaseService.init(backup);

        stage = primaryStage;
        editor = new Editor();
        languageService = new LanguageService(editor);
        languageService.startLanguageAwareness();
        audioService = new AudioService(editor);

        UserKeyProvider.setEditor(editor);
        WebSocketService.setEditor(editor);

        //init Router and go to login
        Router.init();
        Router.route(Constants.ROUTE_LOGIN);

        stage.show();
    }

    @Override
    public void stop() {
        try {
            super.stop();

            languageService.stopLanguageAwareness();

            if (currentController != null) {
                currentController.stop();
            }
            NetworkClientInjector.getRestClient().sendLogoutRequest(response -> {
                RestClient.stop();
            });

            NetworkClientInjector.getMediaRequestClient().stop();
            WebSocketService.stop();
            DatabaseService.stop();
        } catch (Exception e) {
            log.error("Error while trying to shutdown", e);
        }
    }
}
