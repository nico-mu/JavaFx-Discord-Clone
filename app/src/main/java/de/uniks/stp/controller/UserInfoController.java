package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.modal.SettingsModal;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.router.Router;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserInfoController implements ControllerInterface {
    private static final Logger log = LoggerFactory.getLogger(MainScreenController.class);

    private final String USERNAME_LABEL_ID = "#username-label";
    private final String LOGOUT_BUTTON_ID = "#logout-button";
    private final String IMAGE_VIEW_ID = "#settings-gear";
    private final String SETTINGS_GEAR_CONTAINER_ID = "#settings-gear-container";

    private final AnchorPane view;
    private final Editor editor;
    private final RestClient restClient;
    private Label usernameLabel;
    private Button logoutButton;
    private VBox settingsGearContainer;
    private ImageView imageView;

    public UserInfoController(Parent view, Editor editor) {
        this.view = (AnchorPane) view;
        this.editor = editor;
        this.restClient = NetworkClientInjector.getRestClient();
    }

    @Override
    public void init() {
        HBox userSubView = (HBox) ViewLoader.loadView(Views.USER_INFO_SCREEN);
        view.getChildren().add(userSubView);
        this.usernameLabel = (Label) view.lookup(USERNAME_LABEL_ID);
        this.logoutButton = (Button) view.lookup(LOGOUT_BUTTON_ID);
        this.imageView = (ImageView) view.lookup(IMAGE_VIEW_ID);
        this.settingsGearContainer = (VBox) view.lookup(SETTINGS_GEAR_CONTAINER_ID);

        AnchorPane.setBottomAnchor(userSubView, 7.5d);
        AnchorPane.setTopAnchor(userSubView, 5.0d);
        AnchorPane.setLeftAnchor(userSubView, 10.0d);
        AnchorPane.setRightAnchor(userSubView, 10.0d);

        settingsGearContainer.setOnMouseClicked(this::onSettingsGearClicked);
        logoutButton.setOnAction(this::onLogoutButtonClicked);
        usernameLabel.setText(editor.getOrCreateAccord().getCurrentUser().getName());
    }

    private void onLogoutButtonClicked(ActionEvent actionEvent) {
        restClient.sendLogoutRequest(this::handleLogoutResponse);
    }

    private void handleLogoutResponse(HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toPrettyString());
        if (!response.isSuccess()) {
            log.error("logout failed");
        }
        this.editor.getOrCreateAccord().setUserKey("");
        Platform.runLater(() -> Router.route(Constants.ROUTE_LOGIN));
    }

    private void onSettingsGearClicked(MouseEvent mouseEvent) {
        Parent settingsModalView = ViewLoader.loadView(Views.SETTINGS_MODAL);
        SettingsModal settingsModal = new SettingsModal(settingsModalView, editor);
        settingsModal.showAndWait();
    }

    @Override
    public void stop() {
        logoutButton.setOnAction(null);
    }

}
