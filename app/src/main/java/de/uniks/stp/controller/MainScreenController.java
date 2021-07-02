package de.uniks.stp.controller;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.model.Server;
import de.uniks.stp.notification.NotificationService;
import de.uniks.stp.notification.SubscriberInterface;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import de.uniks.stp.router.Router;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;

@Route(Constants.ROUTE_MAIN)
public class MainScreenController implements ControllerInterface {

    private static final String NAV_BAR_ID = "#nav-bar";
    private static final String USER_SETTINGS_PANE_ID = "#user-settings-pane";
    private static final String SUBVIEW_CONTAINER_ID = "#subview-container";

    private final Parent view;
    private final Editor editor;
    private final Router router;
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
    private AnchorPane subViewContainer;
    private UserInfoController userInfoController;
    private NavBarListController navBarController;
    private ControllerInterface currentController;

    @AssistedInject
    public MainScreenController(NotificationService notificationService,
                         Editor editor,
                         Router router,
                         @Assisted Parent view) {
        this.view = view;
        this.editor = editor;
        this.router = router;
        this.notificationService = notificationService;
    }

    @Override
    public void init() {
        this.navBar = (AnchorPane) view.lookup(NAV_BAR_ID);
        this.userSettingsPane = (AnchorPane) view.lookup(USER_SETTINGS_PANE_ID);
        this.subViewContainer = (AnchorPane) view.lookup(SUBVIEW_CONTAINER_ID);

        navBarController = navBarListControllerFactory.create(navBar);
        navBarController.init();
        userInfoController = userInfoControllerFactory.create(this.userSettingsPane);
        userInfoController.init();

        notificationService.invokeUserNotifications();
    }

    @Override
    public void route(RouteInfo routeInfo, RouteArgs args) {
        cleanup();
        String subroute = routeInfo.getSubControllerRoute();
        if (subroute.equals(Constants.ROUTE_HOME)) {
            currentController = homeScreenControllerFactory.create(this.subViewContainer);
            currentController.init();
            router.addToControllerCache(routeInfo.getFullRoute(), currentController);
        } else if (subroute.equals(Constants.ROUTE_SERVER)) {
            Server server = editor.getServer(args.getArguments().get(":id"));
            if (Objects.nonNull(server)) {
                currentController = serverScreenControllerFactory.create(this.subViewContainer, server);
                currentController.init();
                router.addToControllerCache(routeInfo.getFullRoute(), currentController);
            }
        }
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
