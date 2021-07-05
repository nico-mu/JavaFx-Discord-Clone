package de.uniks.stp.controller;

import de.uniks.stp.AccordApp;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.dagger.scope.AppScope;
import de.uniks.stp.jpa.AccordSettingKey;
import de.uniks.stp.jpa.AppDatabaseService;
import de.uniks.stp.jpa.model.AccordSettingDTO;
import de.uniks.stp.router.RouteInfo;
import de.uniks.stp.view.Languages;
import de.uniks.stp.view.Views;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.inject.Inject;
import java.util.Objects;

@AppScope
public class AppController implements ControllerInterface {

    private final ViewLoader viewLoader;
    private final Stage stage;
    private final AppDatabaseService databaseService;
    private final AccordApp app;
    private final LoginScreenController.LoginScreenControllerFactory loginScreenControllerFactory;

    private ControllerInterface currentController;

    @Inject
    public AppController(AccordApp app,
                         ViewLoader viewLoader,
                         Stage primaryStage,
                         LoginScreenController.LoginScreenControllerFactory loginScreenControllerFactory,
                         AppDatabaseService databaseService) {
        this.viewLoader = viewLoader;
        this.stage = primaryStage;
        this.databaseService = databaseService;
        this.loginScreenControllerFactory = loginScreenControllerFactory;
        this.app = app;
    }

    @Override
    public void init() {
        setInitialLanguage();
        RouteInfo routeInfo = new RouteInfo()
            .setCurrentControllerRoute("")
            .setSubControllerRoute(Constants.ROUTE_LOGIN);
        route(routeInfo);
        stage.show();
    }

    private void setInitialLanguage() {
        final AccordSettingDTO accordLanguageSetting = databaseService.getAccordSetting(AccordSettingKey.LANGUAGE);
        final Languages language = Objects.nonNull(accordLanguageSetting) ?
            Languages.fromKeyOrDefault(accordLanguageSetting.getValue()) : Languages.getDefault();
        viewLoader.changeLanguage(language);
    }

    public ControllerInterface route(RouteInfo routeInfo) {
        cleanup();
        Parent root;
        Scene scene;
        String subroute = routeInfo.getSubControllerRoute();

        if (subroute.equals(Constants.ROUTE_MAIN)) {
            if(!Objects.nonNull(app.getSessionComponent())) {
                return null;
            }
            root = viewLoader.loadView(Views.MAIN_SCREEN);
            currentController = app.getSessionComponent().getMainScreenControllerFactory().create(root);
            currentController.init();
            scene = new Scene(root);
            scene.getStylesheets().add(AccordApp.class.getResource("/de/uniks/stp/style/css/component/context-menu.css").toString());
            stage.setTitle("Accord");
            stage.setScene(scene);
            stage.centerOnScreen();
            return currentController;
        } else if (subroute.equals(Constants.ROUTE_LOGIN)) {
            root = viewLoader.loadView(Views.LOGIN_SCREEN);
            currentController = loginScreenControllerFactory.create(root);
            currentController.init();
            scene = new Scene(root);
            stage.setTitle("Accord");
            stage.setScene(scene);
            stage.centerOnScreen();
            return currentController;
        }
        return null;
    }

    @Override
    public void stop() {
        cleanup();
    }

    public void cleanup() {
        if (Objects.nonNull(currentController)) {
            currentController.stop();
        }
        currentController = null;
    }
}
