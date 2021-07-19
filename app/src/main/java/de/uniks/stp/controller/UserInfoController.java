package de.uniks.stp.controller;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.AccordApp;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.modal.SettingsModal;
import de.uniks.stp.network.rest.SessionRestClient;
import de.uniks.stp.router.Router;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class UserInfoController implements ControllerInterface {
    private static final Logger log = LoggerFactory.getLogger(MainScreenController.class);

    private final static String USERNAME_LABEL_ID = "#username-label";
    private final static String LOGOUT_BUTTON_ID = "#logout-button";
    private final static String SETTINGS_GEAR_CONTAINER_ID = "#settings-gear-container";

    private final AnchorPane view;
    private final Editor editor;
    private final SessionRestClient restClient;
    private final ViewLoader viewLoader;
    private final Router router;
    private final AccordApp app;
    private Label usernameLabel;
    private Button logoutButton;
    private VBox settingsGearContainer;

    private final SettingsModal.SettingsModalFactory settingsModalFactory;

    @AssistedInject
    public UserInfoController(Editor editor,
                              AccordApp app,
                              ViewLoader viewLoader,
                              SessionRestClient restClient,
                              Router router,
                              SettingsModal.SettingsModalFactory settingsModalFactory,
                              @Assisted Parent view) {
        this.view = (AnchorPane) view;
        this.editor = editor;
        this.restClient = restClient;
        this.viewLoader = viewLoader;
        this.router = router;
        this.app = app;
        this.settingsModalFactory = settingsModalFactory;
    }

    @Override
    public void init() {
        HBox userSubView = (HBox) viewLoader.loadView(Views.USER_INFO_SCREEN);
        view.getChildren().add(userSubView);
        this.usernameLabel = (Label) view.lookup(USERNAME_LABEL_ID);
        this.logoutButton = (Button) view.lookup(LOGOUT_BUTTON_ID);
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

        Platform.runLater(() ->  {
            router.route(Constants.ROUTE_LOGIN);
            this.editor.prepareLogout();  //delete user related information
            app.getSessionComponent().getWebsocketService().stop();
            app.getSessionComponent().getSessionDatabaseService().stop();
            app.getSessionComponent().getSessionRestClient().stop();
            app.getSessionComponent().getMediaRequestClient().stop();
            app.setSessionComponent(null);
        });
    }

    private void onSettingsGearClicked(MouseEvent mouseEvent) {
        Parent settingsModalView = viewLoader.loadView(Views.SETTINGS_MODAL);
        SettingsModal settingsModal = settingsModalFactory.create(settingsModalView);
        settingsModal.show();
    }

    @Override
    public void stop() {
        logoutButton.setOnAction(null);
        settingsGearContainer.setOnMouseClicked(null);
    }

    @AssistedFactory
    public interface UserInfoControllerFactory {
        UserInfoController create(Parent view);
    }

}
