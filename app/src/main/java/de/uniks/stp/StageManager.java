package de.uniks.stp;

import de.uniks.stp.controller.ControllerInterface;
import de.uniks.stp.controller.LoginScreenController;
import de.uniks.stp.controller.MainScreenController;
import de.uniks.stp.jpa.AccordSettingKey;
import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.jpa.model.AccordSettingDTO;
import de.uniks.stp.model.Accord;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.network.UserKeyProvider;
import de.uniks.stp.network.WebSocketService;
import de.uniks.stp.router.RouteInfo;
import de.uniks.stp.router.Router;
import de.uniks.stp.view.Languages;
import de.uniks.stp.view.Views;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

public class StageManager extends Application {
    private static final Logger log = LoggerFactory.getLogger(StageManager.class);

    private static Editor editor;
    private static Stage stage;
    private static ControllerInterface currentController;

    private final PropertyChangeListener languagePropertyChangeListener = this::onLanguagePropertyChange;

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
            stage.centerOnScreen();
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

    private void onLanguagePropertyChange(final PropertyChangeEvent languageChangeEvent) {
        final Languages newLanguage = Languages.fromKeyOrDefault((String) languageChangeEvent.getNewValue());
        ViewLoader.changeLanguage(newLanguage);
        Router.forceReloadAndRouteHome();

        DatabaseService.saveAccordSetting(AccordSettingKey.LANGUAGE, newLanguage.key);

    }

    @Override
    public void start(Stage primaryStage) {
        DatabaseService.init();

        stage = primaryStage;
        editor = new Editor();

        startLanguageAwareness();

        UserKeyProvider.setEditor(editor);
        WebSocketService.setEditor(editor);

        //init Router and go to login
        Router.init();
        Router.route(Constants.ROUTE_LOGIN);

        stage.show();
    }

    private void startLanguageAwareness() {
        final AccordSettingDTO accordLanguageSetting = DatabaseService.getAccordSetting(AccordSettingKey.LANGUAGE);
        final Languages language = Objects.nonNull(accordLanguageSetting) ?
            Languages.fromKeyOrDefault(accordLanguageSetting.getValue()) : Languages.getDefault();

        final Accord accord = editor.getOrCreateAccord();
        accord.setLanguage(language.key);
        ViewLoader.changeLanguage(language);
        accord.listeners().addPropertyChangeListener(Accord.PROPERTY_LANGUAGE, languagePropertyChangeListener);
    }

    @Override
    public void stop() {
        try {
            super.stop();

            stopLanguageAwareness();

            if (currentController != null) {
                currentController.stop();
            }
            NetworkClientInjector.getRestClient().sendLogoutRequest(response -> {
                RestClient.stop();
            });

            WebSocketService.stop();
            DatabaseService.stop();
        } catch (Exception e) {
            log.error("Error while trying to shutdown", e);
        }

    }

    private void stopLanguageAwareness() {
        final Accord accord = editor.getOrCreateAccord();
        accord.listeners().removePropertyChangeListener(languagePropertyChangeListener);
    }
}
