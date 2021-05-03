package de.uniks.stp.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.StageManager;
import de.uniks.stp.network.auth.AuthClient;
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
    }

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
    }

    public void stop() {
        registerButton.setOnAction(null);
        loginButton.setOnAction(null);
    }

    private void readInputFields() {
        name = nameField.getText();
        password = passwordField.getText();
    }

    private boolean isEmptyInput() {
        return name.isEmpty() || password.isEmpty();
    }

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

    public void showSpinner() {
        Platform.runLater(() -> {
            nameField.setDisable(true);
            passwordField.setDisable(true);
            loginButton.setDisable(true);
            registerButton.setDisable(true);
            spinner.setVisible(true);
        });
    }

    public void hideSpinner() {
        Platform.runLater(() -> {
            nameField.setDisable(false);
            passwordField.setDisable(false);
            loginButton.setDisable(false);
            registerButton.setDisable(false);
            spinner.setVisible(false);
        });
    }

    public void onRegisterButtonClicked(ActionEvent event) {
        readInputFields();

        if (isEmptyInput()) {
            setErrorMessage(Constants.LBL_MISSING_FIELDS);
            return;
        }

        setErrorMessage(null);
        showSpinner();
        AuthClient.register(name, password, this::handleRegisterResponse);
    }

    public void onLoginButtonClicked(ActionEvent _event) {
        readInputFields();

        if (isEmptyInput()) {
            setErrorMessage(Constants.LBL_MISSING_FIELDS);
            return;
        }

        setErrorMessage(null);
        showSpinner();
        AuthClient.login(name, password, this::handleLoginResponse);
    }

    private void handleRegisterResponse(HttpResponse<JsonNode> response) {
        hideSpinner();
        // log user in
        if (response.isSuccess()) {
            setErrorMessage(null);
            AuthClient.login(name, password, this::handleLoginResponse);
            return;
        }
        // Registration failed
        passwordField.clear();
        setErrorMessage(Constants.LBL_REGISTRATION_FAILED);
    }

    private void handleLoginResponse(HttpResponse<JsonNode> response) {
        hideSpinner();
        // set currentUser + userKey and switch to HomeScreen
        if (response.isSuccess()) {
            setErrorMessage(null);
            String userKey = response.getBody().getObject().getJSONObject("data").getString("userKey");
            editor.setUserKey(userKey);
            editor.setCurrentUser(editor.getOrCreateUser(name, true));

            StageManager.showMainScreen();
            return;
        }
        // Login failed
        passwordField.clear();
        setErrorMessage(Constants.LBL_LOGIN_FAILED);
    }

}
