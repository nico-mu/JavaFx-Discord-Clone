package de.uniks.stp.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.StageManager;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.ViewLoader;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import java.util.Objects;

public class LoginScreenController implements ControllerInterface {
    private final Parent view;
    private final Editor editor;
    private final RestClient restClient;

    private JFXTextField nameField;
    private JFXPasswordField passwordField;
    private JFXButton registerButton;
    private JFXButton loginButton;
    private JFXSpinner spinner;
    private Label errorLabel;

    private String name;
    private String password;

    public LoginScreenController(Parent view, Editor editor) {
        this.view = view;
        this.editor = editor;
        this.restClient = new RestClient();
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
        errorLabel = (Label) view.lookup("#error-message");
        spinner = (JFXSpinner) view.lookup("#spinner");

        // Register button event handler
        registerButton.setOnAction(this::onRegisterButtonClicked);
        loginButton.setOnAction(this::onLoginButtonClicked);

        loginButton.setDefaultButton(true);  // Allows to use Enter in order to press login button
    }

    public void stop() {
        registerButton.setOnAction(null);
        loginButton.setOnAction(null);
        restClient.stop();
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
            registerButton.setDisable(true);
            spinner.setVisible(true);
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
            registerButton.setDisable(false);
            spinner.setVisible(false);
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
        restClient.login(name, password, this::handleLoginResponse);
    }

    /**
     * Checks whether register was successful.
     * If so, user will be logged in.
     * Else, matching error will be shown.
     *
     * @param response contains server response
     */
    private void handleRegisterResponse(HttpResponse<JsonNode> response) {
        System.out.println(response.getBody());
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
        System.out.println(response.getBody());
        // set currentUser + userKey and switch to HomeScreen
        if (response.isSuccess()) {
            setErrorMessage(null);
            String userKey = response.getBody().getObject().getJSONObject("data").getString("userKey");
            editor.setUserKey(userKey);
            editor.setCurrentUser(editor.getOrCreateUser(name, true));

            StageManager.showHomeScreen();
            return;
        }
        // Login failed
        passwordField.clear();
        enableUserInput();
        if (response.getBody().getObject().getString(Constants.MESSAGE).equals("Invalid credentials")){
            setErrorMessage(Constants.LBL_LOGIN_WRONG_CREDENTIALS);
        }
        else{
            setErrorMessage(Constants.LBL_LOGIN_FAILED);
        }
    }

}
