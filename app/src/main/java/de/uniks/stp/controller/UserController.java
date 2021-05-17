package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import de.uniks.stp.router.Router;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserController implements ControllerInterface {
    private static final Logger log = LoggerFactory.getLogger(MainScreenController.class);

    private final String USERNAME_LABEL_ID = "#username-label";
    private final String LOGOUT_BUTTON_ID = "#logout-button";
    private final String IMAGE_VIEW_ID = "#settings-gear";
    private final String SETTINGS_GEAR_CONTAINER_ID = "#settings-gear-container";

    private final AnchorPane view;
    private final Editor editor;
    private final RestClient restClient;
    private HBox settingsWindowLayout;
    private Label usernameLabel;
    private Button logoutButton;
    private VBox settingsGearContainer;
    private ImageView imageView = new ImageView();
    private ChoiceBox<String> languageSelectChoiceBox;
    private Language german;
    private Language english;
    private final InvalidationListener languageChangedListener = this::onLanguageChanged;

    private enum Language {
        DE, EN
    }

    public UserController(Parent view, Editor editor) {
        this.view = (AnchorPane) view;
        this.editor = editor;
        this.restClient = new RestClient();
    }

    @Override
    public void init() {
        HBox userSubView = (HBox) ViewLoader.loadView(Views.USER_SCREEN);
        view.getChildren().add(userSubView);
        this.usernameLabel = (Label) view.lookup(USERNAME_LABEL_ID);
        this.logoutButton = (Button) view.lookup(LOGOUT_BUTTON_ID);
        this.imageView = (ImageView) view.lookup(IMAGE_VIEW_ID);
        this.settingsGearContainer = (VBox) view.lookup(SETTINGS_GEAR_CONTAINER_ID);
        this.german = Language.DE;
        this.english = Language.EN;

        settingsGearContainer.setOnMouseClicked(this::onSettingsGearClicked);
        logoutButton.setOnAction(this::onLogoutButtonClicked);
        usernameLabel.setText(editor.getOrCreateAccord().getCurrentUser().getName());
    }

    @Override
    public void route(RouteInfo routeInfo, RouteArgs args) {

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
        settingsWindowLayout = new HBox(15);
        languageSelectChoiceBox = new ChoiceBox<>();
        languageSelectChoiceBox.setPrefWidth(70);
        Scene scene = new Scene(settingsWindowLayout, 200, 100);
        Stage selectLanguageWindow = new Stage();
        String closeLabel = ViewLoader.loadLabel(Constants.LBL_CLOSE_WINDOW);
        String windowTitle = ViewLoader.loadLabel(Constants.LBL_SELECT_LANGUAGE);
        Button closeSettingsWindowButton = new Button(closeLabel);
        closeSettingsWindowButton.setPrefWidth(70);

        selectLanguageWindow.initModality(Modality.APPLICATION_MODAL);
        selectLanguageWindow.setTitle(windowTitle);
        selectLanguageWindow.initStyle(StageStyle.UTILITY);
        selectLanguageWindow.setScene(scene);
        settingsWindowLayout.setAlignment(Pos.CENTER);
        settingsWindowLayout.getChildren().addAll(languageSelectChoiceBox, closeSettingsWindowButton);
        languageSelectChoiceBox.getItems().addAll(Constants.LANG_DE, Constants.LANG_EN);

        //set initial value of dropdown menu, should be current language
        languageSelectChoiceBox.setValue(Constants.LANG_DE);
        languageSelectChoiceBox.getSelectionModel().selectedItemProperty().addListener(languageChangedListener);

        closeSettingsWindowButton.setOnAction(e -> {
            languageSelectChoiceBox.getSelectionModel().selectedItemProperty().removeListener(languageChangedListener);
            selectLanguageWindow.close();
        });
        selectLanguageWindow.showAndWait();
    }

    private void onLanguageChanged(Observable observable) {
        //todo
        System.out.println("language changed!");
    }

    @Override
    public void stop() {

    }

}
