package de.uniks.stp.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import de.uniks.stp.Editor;
import de.uniks.stp.StageManager;
import de.uniks.stp.network.HttpResponseHelper;
import de.uniks.stp.network.auth.AuthClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;

public class LoginScreenController implements ControllerInterface {

    private final Parent view;
    private final Editor editor;

    private JFXTextField nameField;
    private JFXPasswordField passwordField;
    private JFXButton registerButton;
    private JFXButton loginButton;
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

        passwordField.clear();
    }

    private boolean isEmptyInput() {
        return name.isEmpty() || password.isEmpty();
    }

    public void onRegisterButtonClicked(ActionEvent event) {
        readInputFields();

        if (isEmptyInput()) {
            return;
        }

        AuthClient.register(name, password, this::handleRegisterResponse);
    }

    public void onLoginButtonClicked(ActionEvent _event) {
        readInputFields();

        if (isEmptyInput()) {
            return;
        }

        AuthClient.login(name, password, this::handleLoginResponse);
    }

    private void handleRegisterResponse(HttpResponse<JsonNode> response) {
        // log user in
        if (HttpResponseHelper.isSuccess(response.getStatus())) {
            Platform.runLater(() -> {
                errorLabel.setText("");
            });
            AuthClient.login(name, password, this::handleLoginResponse);
            return;
        }
        // Registration failed
        Platform.runLater(() -> {
            errorLabel.setText("Registration failed");
        });
    }

    private void handleLoginResponse(HttpResponse<JsonNode> response) {
        // set currentUser + userKey and switch to HomeScreen
        if (HttpResponseHelper.isSuccess(response.getStatus())) {
            String userKey = response.getBody().getObject().getJSONObject("data").getString("userKey");
            editor.setUserKey(userKey);
            editor.setCurrentUser(editor.getOrCreateUser(name, true));

            StageManager.showHomeScreen();
            return;
        }
        // Login failed
        Platform.runLater(() -> {
            errorLabel.setText("Login failed");
        });
        
    }

}
