package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.model.Server;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import de.uniks.stp.router.Router;
import de.uniks.stp.network.NetworkClientInjector;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@Route(Constants.ROUTE_MAIN)
public class MainScreenController implements ControllerInterface {
    private static final Logger log = LoggerFactory.getLogger(MainScreenController.class);

    private final String NAV_BAR_ID = "#nav-bar";
    private final String USER_SETTINGS_PANE_ID = "#user-settings-pane";
    private final String SUBVIEW_CONTAINER_ID = "#subview-container";
    private final Parent view;
    private final Editor editor;
    private AnchorPane navBar;
    private AnchorPane userSettingsPane;
    private AnchorPane subViewContainer;
    private UserController userSubViewController;
    private NavBarListController navBarController;
    private ControllerInterface currentController;

    public MainScreenController(Parent view, Editor editor) {
        this.view = view;
        this.editor = editor;
    }

    @Override
    public void init() {
        this.navBar = (AnchorPane) view.lookup(NAV_BAR_ID);
        this.userSettingsPane = (AnchorPane) view.lookup(USER_SETTINGS_PANE_ID);
        this.subViewContainer = (AnchorPane) view.lookup(SUBVIEW_CONTAINER_ID);

        navBarController = new NavBarListController(navBar, editor);
        navBarController.init();
        userSubViewController = new UserController(this.userSettingsPane, this.editor);
        userSubViewController.init();
    }

    @Override
    public void route(RouteInfo routeInfo, RouteArgs args) {
        cleanup();
        String subroute = routeInfo.getSubControllerRoute();

        if (subroute.equals(Constants.ROUTE_HOME)) {
            currentController = new HomeScreenController(this.subViewContainer, this.editor);
            currentController.init();
            Router.addToControllerCache(routeInfo.getFullRoute(), currentController);
        } else if (subroute.equals(Constants.ROUTE_SERVER)) {
            Server server = editor.getServer(args.getArguments().get(":id"));
            if (Objects.nonNull(server)) {
                currentController = new ServerScreenController(this.subViewContainer, this.editor, server);
                currentController.init();
                Router.addToControllerCache(routeInfo.getFullRoute(), currentController);
            }
        }
    }

    private void cleanup() {
        this.subViewContainer.getChildren().clear();
    }

    @Override
    public void stop() {
        navBarController.stop();
        if (Objects.nonNull(currentController)) {
            currentController.stop();
        }
    }
}
