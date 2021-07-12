package de.uniks.stp.controller;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.model.Server;
import de.uniks.stp.notification.NotificationService;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Objects;

@Route(Constants.ROUTE_MAIN)
public class MainScreenController implements ControllerInterface {

    private static final String NAV_BAR_ID = "#nav-bar";
    private static final String USER_SETTINGS_PANE_ID = "#user-settings-pane";
    private static final String SUBVIEW_CONTAINER_ID = "#subview-container";

    private final Parent view;
    private final Editor editor;
    private final NotificationService notificationService;

    @Inject
    HomeScreenController.HomeScreenControllerFactory homeScreenControllerFactory;

    @Inject
    ServerScreenController.ServerScreenControllerFactory serverScreenControllerFactory;

    @Inject
    NavBarListController.NavBarListControllerFactory navBarListControllerFactory;

    @Inject
    UserInfoController.UserInfoControllerFactory userInfoControllerFactory;

    private AnchorPane navBar;
    private AnchorPane userSettingsPane;
    private VBox subViewContainer;
    private UserInfoController userInfoController;
    private NavBarListController navBarController;
    private ControllerInterface currentController;

    @AssistedInject
    public MainScreenController(NotificationService notificationService,
                         Editor editor,
                         @Assisted Parent view) {
        this.view = view;
        this.editor = editor;
        this.notificationService = notificationService;
    }

    @Override
    public void init() {
        this.navBar = (AnchorPane) view.lookup(NAV_BAR_ID);
        this.userSettingsPane = (AnchorPane) view.lookup(USER_SETTINGS_PANE_ID);
        this.subViewContainer = (VBox) view.lookup(SUBVIEW_CONTAINER_ID);

        navBarController = navBarListControllerFactory.create(navBar);
        navBarController.init();
        userInfoController = userInfoControllerFactory.create(this.userSettingsPane);
        userInfoController.init();

        notificationService.invokeUserNotifications();
    }

    @Override
    public ControllerInterface route(RouteInfo routeInfo, RouteArgs args) {
        cleanup();
        String subroute = routeInfo.getSubControllerRoute();
        if (subroute.equals(Constants.ROUTE_HOME)) {
            navBarController.setHomeElementActive();
            currentController = homeScreenControllerFactory.create(this.subViewContainer);
            currentController.init();
            return currentController;
        } else if (subroute.equals(Constants.ROUTE_SERVER)) {
            Server server = editor.getServer(args.getArguments().get(":id"));
            if (Objects.nonNull(server)) {
                currentController = serverScreenControllerFactory.create(this.subViewContainer, server);
                currentController.init();
                return currentController;
            }
        }
        return null;
    }

    private void cleanup() {
        this.subViewContainer.getChildren().clear();
    }

    @Override
    public void stop() {
        cleanup();
        navBarController.stop();
        userInfoController.stop();

        if (Objects.nonNull(currentController)) {
            currentController.stop();
        }
    }

    @AssistedFactory
    public interface MainScreenControllerFactory {
        MainScreenController create(Parent view);
    }
}
