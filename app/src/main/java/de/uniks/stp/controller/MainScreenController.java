package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.model.Server;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
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
    //todo: replace settings-button
    private Button settingsButton;

    private ControllerInterface currentController;

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
        this.settingsButton = (Button) view.lookup("#settings-button");

        logoutButton.setOnAction(this::onLogoutButtonClicked);
        settingsButton.setOnAction(this::onSettingsButtonClicked);
        usernameLabel.setText(editor.getOrCreateAccord().getCurrentUser().getName());

        navBarController = new NavBarListController(navBar, editor);
        navBarController.init();
    }

    @Override
    public void route(RouteInfo routeInfo, RouteArgs args) {
        cleanup();
        String subroute = routeInfo.getSubControllerRoute();

        if (subroute.equals(Constants.ROUTE_HOME)) {
            currentController = new HomeScreenController(this.subViewContainer, this.editor);
            currentController.init();
            Router.addToControllerCache(routeInfo.getFullRoute(), currentController);
        } else if (subroute.equals(Constants.ROUTE_SERVER) && args.getKey().equals(":id") && !args.getValue().isEmpty()) {
            Server server = editor.getServer(args.getValue());
            if (Objects.nonNull(server)) {
                currentController = new ServerScreenController(this.subViewContainer, this.editor, server);
                currentController.init();
                Router.addToControllerCache(routeInfo.getFullRoute(), currentController);
            }
        }
    }

    private void onLogoutButtonClicked(ActionEvent actionEvent) {
        restClient.sendLogoutRequest(this::handleLogoutResponse);
    }

    private void handleLogoutResponse(HttpResponse<JsonNode> response) {
        System.out.println(response.getBody());
        if (!response.isSuccess()) {
            System.err.println("logout failed");
        }
        this.editor.getOrCreateAccord().setUserKey("");
        Platform.runLater(() -> Router.route(Constants.ROUTE_LOGIN));
    }

    private void onSettingsButtonClicked(ActionEvent actionEvent) {
        System.out.println("settings button clicked");
        Stage popupwindow = new Stage();
        popupwindow.initModality(Modality.APPLICATION_MODAL);
        popupwindow.setTitle("Sprache wählen");

        Button button1 = new Button("Close this pop up window");
        Button button2 = new Button("Select chosen language");

        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll("Deutsch", "English");
        //set inital value of dropdown menu, should be current language
        choiceBox.setValue("Deutsch");

        button1.setOnAction(e -> popupwindow.close());
        button2.setOnAction(event -> getChosenLanguage(choiceBox));

        VBox layout = new VBox(10);
        layout.getChildren().addAll(choiceBox, button1, button2);
        layout.setAlignment(Pos.CENTER);

        Scene scene1 = new Scene(layout, 300, 250);
        popupwindow.setScene(scene1);
        popupwindow.showAndWait();
    }

    private void getChosenLanguage(ChoiceBox<String> choiceBox) {
        String language = choiceBox.getValue();
        System.out.println(language + " language has been chosen!");
    }

    private void cleanup() {
        this.subViewContainer.getChildren().clear();
    }

    @Override
    public void stop() {
        navBarController.stop();
        logoutButton.setOnAction(null);
        settingsButton.setOnAction(null);
        if (Objects.nonNull(currentController)) {
            currentController.stop();
        }
    }
}
