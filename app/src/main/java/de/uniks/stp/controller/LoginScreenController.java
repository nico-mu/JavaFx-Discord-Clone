package de.uniks.stp.controller;

import com.jfoenix.controls.*;
import de.uniks.stp.*;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.jpa.AccordSettingKey;
import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.jpa.model.AccordSettingDTO;
import de.uniks.stp.language.LanguageService;
import de.uniks.stp.model.User;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.network.UserKeyProvider;
import de.uniks.stp.notification.NotificationService;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@Route(Constants.ROUTE_LOGIN)
public class LoginScreenController implements ControllerInterface {
    private static final Logger log = LoggerFactory.getLogger(LoginScreenController.class);

    private final Parent view;
    private final Editor editor;
    private final RestClient restClient;

    private JFXTextField nameField;
    private JFXPasswordField passwordField;
    private JFXButton registerButton;
    private JFXButton loginButton;
    private JFXButton tempLoginButton;
    private JFXSpinner spinner;
    private JFXCheckBox rememberMeCheckBox;
    private Label errorLabel;

    private String name;
    private String password;
    private boolean tempUserLogin;

    public LoginScreenController(Parent view, Editor editor) {
        this.view = view;
        this.editor = editor;
        this.restClient = NetworkClientInjector.getRestClient();
    }

    /**
     * Looks up View Objects, activates Buttons and sets default button.
     * Should be called in StageManager::showLoginScreen.
     */
    public void init() {
        nameField = (JFXTextField) view.lookup("#name-field");
        passwordField = (JFXPasswordField) view.lookup("#password-field");
        registerButton = (JFXButton) view.lookup("#register-button");
        loginButton = (JFXButton) view.lookup("#login-button");
        tempLoginButton = (JFXButton) view.lookup("#temp-login-button");
        errorLabel = (Label) view.lookup("#error-message");
        spinner = (JFXSpinner) view.lookup("#spinner");
        rememberMeCheckBox = (JFXCheckBox) view.lookup("#remember-me-checkbox");

        // Register button event handler
        registerButton.setOnAction(this::onRegisterButtonClicked);
        loginButton.setOnAction(this::onLoginButtonClicked);
        tempLoginButton.setOnAction(this::onTempLoginButtonClicked);

        loginButton.setDefaultButton(true);  // Allows to use Enter in order to press login button

        AccordSettingDTO lastUserLogin = DatabaseService.getAccordSetting(AccordSettingKey.LAST_USER_LOGIN);
        if (Objects.nonNull(lastUserLogin) && Objects.nonNull(lastUserLogin.getValue())) {
            Platform.runLater(() -> {
                nameField.setText(lastUserLogin.getValue());
                passwordField.requestFocus();
            });
        }
    }

    public void stop() {
        registerButton.setOnAction(null);
        loginButton.setOnAction(null);
        tempLoginButton.setOnAction(null);
    }

    private void readInputFields() {
        name = nameField.getText();
        password = passwordField.getText();
    }

    private boolean isEmptyInput() {
        return name.isEmpty() || password.isEmpty();
    }

    /**
     * Changes ErrorLabel to display given error.
     *
     * @param label Constant of resource label
     */
    private void setErrorMessage(String label) {
        if (Objects.isNull(label)) {
            Platform.runLater(() -> {
                errorLabel.setText("");
            });
            return;
        }
        String message = ViewLoader.loadLabel(label);
        Platform.runLater(() -> {
            errorLabel.setText(message);
        });
    }

    /**
     * Disables TextFields and Buttons and shows Spinner.
     * Should be called when waiting for server response.
     */
    private void disableUserInput() {
        Platform.runLater(() -> {
            nameField.setDisable(true);
            passwordField.setDisable(true);
            loginButton.setDisable(true);
            tempLoginButton.setDisable(true);
            registerButton.setDisable(true);
            spinner.setVisible(true);
            rememberMeCheckBox.setDisable(true);
        });
    }

    /**
     * Enables TextFields and Buttons and hides Spinner.
     * Should be called when server response is received.
     */
    private void enableUserInput() {
        Platform.runLater(() -> {
            nameField.setDisable(false);
            passwordField.setDisable(false);
            loginButton.setDisable(false);
            tempLoginButton.setDisable(false);
            registerButton.setDisable(false);
            spinner.setVisible(false);
            rememberMeCheckBox.setDisable(false);
        });
    }

    /**
     * Reads TextFields and sends register request.
     *
     * @param event (passed automatically)
     */
    private void onRegisterButtonClicked(ActionEvent event) {
        // read/check TextFields
        readInputFields();
        if (isEmptyInput()) {
            setErrorMessage(Constants.LBL_MISSING_FIELDS);
            return;
        }

        // prepare for and send register request
        setErrorMessage(null);
        disableUserInput();
        restClient.register(name, password, this::handleRegisterResponse);
    }

    /**
     * Reads TextFields and sends login request.
     *
     * @param event (passed automatically)
     */
    private void onLoginButtonClicked(ActionEvent event) {
        // read/check TextFields
        readInputFields();
        if (isEmptyInput()) {
            setErrorMessage(Constants.LBL_MISSING_FIELDS);
            return;
        }

        // prepare for and send login request
        setErrorMessage(null);
        disableUserInput();
        tempUserLogin = false;
        restClient.login(name, password, this::handleLoginResponse);
    }

    /**
     * Requests a temp user and sends login request.
     *
     * @param event (passed automatically)
     */
    private void onTempLoginButtonClicked(ActionEvent event) {
        // prepare for and send login request
        setErrorMessage(null);
        disableUserInput();
        tempUserLogin = true;

        restClient.tempRegister(this::handleTempRegisterResponse);
    }

    private void handleTempRegisterResponse(HttpResponse<JsonNode> response) {
        if (response.isSuccess()) {
            final JSONObject data = response.getBody().getObject().getJSONObject("data");
            name = data.getString("name");
            password = data.getString("password");

            restClient.login(name, password, this::handleLoginResponse);
        } else {
            setErrorMessage(Constants.LBL_REGISTRATION_FAILED);
        }
    }

    /**
     * Checks whether register was successful.
     * If so, user will be logged in.
     * Else, matching error will be shown.
     *
     * @param response contains server response
     */
    private void handleRegisterResponse(HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toPrettyString());
        // log user in
        if (response.isSuccess()) {
            setErrorMessage(null);
            restClient.login(name, password, this::handleLoginResponse);
            return;
        }
        // Registration failed
        passwordField.clear();
        enableUserInput();
        if (response.getBody().getObject().getString(Constants.MESSAGE).equals("Name already taken")){
            setErrorMessage(Constants.LBL_REGISTRATION_NAME_TAKEN);
        }
        else{
            setErrorMessage(Constants.LBL_REGISTRATION_FAILED);
        }
    }

    /**
     * Checks whether login was successful.
     * If so, HomeScreen will be shown.
     * Else, matching error will be shown.
     *
     * @param response contains server response
     */
    private void handleLoginResponse(HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toPrettyString());
        // set currentUser + userKey and switch to HomeScreen
        if (response.isSuccess()) {
            setErrorMessage(null);
            String userKey = response.getBody().getObject().getJSONObject("data").getString("userKey");
            UserKeyProvider.setEditor(editor);
            StageManager.setLanguageService(new LanguageService(editor));
            StageManager.getLanguageService().startLanguageAwareness();
            StageManager.setAudioService(new AudioService(editor));
            User currentUser = editor.createCurrentUser(name, true).setPassword(password);
            editor.setCurrentUser(currentUser);
            editor.setUserKey(userKey);
            NotificationService.reset();

            if (!tempUserLogin && rememberMeCheckBox.isSelected()) {
                // Save in db
                DatabaseService.saveAccordSetting(AccordSettingKey.LAST_USER_LOGIN, name);
            } else {
                DatabaseService.saveAccordSetting(AccordSettingKey.LAST_USER_LOGIN, null);
            }

            Stage stage = StageManager.getStage();
            Platform.runLater(()-> {
                Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_LIST_ONLINE_USERS);

                stage.setMinWidth(1000);
                stage.setMinHeight(700);
                stage.setWidth(1300);
                stage.setHeight(750);
            });
        }
        // Login failed
        Platform.runLater(passwordField::clear);
        enableUserInput();
        if (response.getBody().getObject().getString(Constants.MESSAGE).equals("Invalid credentials")){
            setErrorMessage(Constants.LBL_LOGIN_WRONG_CREDENTIALS);
        }
        else{
            setErrorMessage(Constants.LBL_LOGIN_FAILED);
        }
    }

}
