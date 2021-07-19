package de.uniks.stp.controller;

import de.uniks.stp.AccordApp;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.dagger.scope.AppScope;
import de.uniks.stp.jpa.AccordSettingKey;
import de.uniks.stp.jpa.AppDatabaseService;
import de.uniks.stp.jpa.model.AccordSettingDTO;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import de.uniks.stp.router.Router;
import de.uniks.stp.view.Languages;
import de.uniks.stp.view.Views;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Objects;

@AppScope
public class AppController implements ControllerInterface {

    private final ViewLoader viewLoader;
    private final Stage stage;
    private final AppDatabaseService databaseService;
    private final AccordApp app;
    private final LoginScreenController.LoginScreenControllerFactory loginScreenControllerFactory;

    private ControllerInterface currentController;

    public AppController(AccordApp app,
                         ViewLoader viewLoader,
                         @Named("primaryStage") Stage primaryStage,
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
        route(routeInfo, new RouteArgs());
        stage.show();
    }

    private void setInitialLanguage() {
        final AccordSettingDTO accordLanguageSetting = databaseService.getAccordSetting(AccordSettingKey.LANGUAGE);
        final Languages language = Objects.nonNull(accordLanguageSetting) ?
            Languages.fromKeyOrDefault(accordLanguageSetting.getValue()) : Languages.getDefault();
        viewLoader.changeLanguage(language);
    }

    public ControllerInterface route(RouteInfo routeInfo, RouteArgs args) {
        cleanup();
        Parent root;
        Scene scene;
        String subroute = routeInfo.getSubControllerRoute();

        if (subroute.equals(Constants.ROUTE_MAIN)) {
            if(Objects.isNull(app.getSessionComponent())) {
                return null;
            }
            root = viewLoader.loadView(Views.MAIN_SCREEN);
            currentController = app.getSessionComponent().getMainScreenControllerFactory().create(root);
            currentController.init();
            scene = new Scene(root);
            scene.getStylesheets().add(AccordApp.class.getResource("/de/uniks/stp/style/css/component/context-menu.css").toString());

            stage.setTitle("Accord");
            stage.setScene(scene);

            if(args.getArguments().containsKey(Router.FORCE_RELOAD)) {
                args.removeArgument(Router.FORCE_RELOAD);
            }
            else {
                stage.setMinWidth(Constants.RES_MIN_MAIN_SCREEN_WIDTH);
                stage.setMinHeight(Constants.RES_MIN_MAIN_SCREEN_HEIGHT);
                stage.setWidth(Constants.RES_MAIN_SCREEN_WIDTH);
                stage.setHeight(Constants.RES_MAIN_SCREEN_HEIGHT);
                stage.centerOnScreen();
            }

            return currentController;
        } else if (subroute.equals(Constants.ROUTE_LOGIN)) {
            root = viewLoader.loadView(Views.LOGIN_SCREEN);
            currentController = loginScreenControllerFactory.create(root);
            currentController.init();
            scene = new Scene(root);

            stage.setTitle("Accord");
            stage.setScene(scene);

            stage.setMinWidth(Constants.RES_MIN_LOGIN_SCREEN_WIDTH);
            stage.setMinHeight(Constants.RES_MIN_LOGIN_SCREEN_HEIGHT);
            stage.setWidth(Constants.RES_LOGIN_SCREEN_WIDTH);
            stage.setHeight(Constants.RES_LOGIN_SCREEN_HEIGHT);
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
