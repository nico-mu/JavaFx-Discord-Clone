package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.model.Server;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.router.Route;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;

import java.util.Objects;

@Route(Constants.ROUTE_MAIN)
public class MainScreenController implements ControllerInterface {

    private final String NAV_BAR_ID = "#nav-bar";
    private final String USER_SETTINGS_PANE_ID = "#user-settings-pane";
    private final String SUBVIEW_CONTAINER_ID = "#subview-container";
    private final String USERNAME_LABEL_ID = "#username-label";
    private final String LOGOUT_BUTTON_ID = "#logout-button";
    private final Parent view;
    private final Editor editor;
    private final RestClient restClient;
    private AnchorPane navBar;
    private AnchorPane userSettingsPane;
    private AnchorPane subViewContainer;
    private NavBarListController navBarController;
    private Label usernameLabel;
    private Button logoutButton;

    public MainScreenController(Parent view, Editor editor) {
        this.view = view;
        this.editor = editor;
        this.restClient = new RestClient();
    }

    @Override
    public void init() {
        this.navBar = (AnchorPane) view.lookup(NAV_BAR_ID);
        this.userSettingsPane = (AnchorPane) view.lookup(USER_SETTINGS_PANE_ID);
        this.subViewContainer = (AnchorPane) view.lookup(SUBVIEW_CONTAINER_ID);
        this.usernameLabel = (Label) view.lookup(USERNAME_LABEL_ID);
        this.logoutButton = (Button) view.lookup(LOGOUT_BUTTON_ID);

        logoutButton.setOnAction(this::onLogoutButtonClicked);
        usernameLabel.setText(editor.getOrCreateAccord().getCurrentUser().getName());

        navBarController = new NavBarListController(navBar, editor);
        navBarController.init();
    }

    @Override
    public void route(RouteInfo routeInfo, RouteArgs args) {
        cleanup();
        String subroute = routeInfo.getSubControllerRoute();

        if (subroute.equals(Constants.ROUTE_HOME)) {
            HomeScreenController homeScreenController = new HomeScreenController(this.subViewContainer, this.editor);
            homeScreenController.init();
            Router.addToControllerCache(routeInfo.getFullRoute(), homeScreenController);
        } else if (subroute.equals(Constants.ROUTE_SERVER) && args.getKey().equals(":id") && !args.getValue().isEmpty()) {
            Server server = editor.getServer(args.getValue());
            if (Objects.nonNull(server)) {
                ServerScreenController serverScreenController = new ServerScreenController(this.subViewContainer, this.editor, server);
                serverScreenController.init();
                Router.addToControllerCache(routeInfo.getFullRoute(), serverScreenController);
            }
        }
    }

    private void onLogoutButtonClicked(ActionEvent actionEvent) {
        restClient.sendLogoutRequest(this::handleLogoutResponse, this.editor.getOrCreateAccord().getUserKey());
    }

    private void handleLogoutResponse(HttpResponse<JsonNode> response) {
        if (!response.isSuccess()) {
            System.err.println("logout failed");
        }
        this.editor.getOrCreateAccord().setUserKey("");
        Platform.runLater(() -> Router.route(Constants.ROUTE_LOGIN));
    }

    private void cleanup() {
        this.subViewContainer.getChildren().clear();
    }

    @Override
    public void stop() {
        navBarController.stop();
        logoutButton.setOnAction(null);
    }
}
