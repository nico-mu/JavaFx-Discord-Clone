package de.uniks.stp.controller;

import de.uniks.stp.network.HttpResponseHelper;
import de.uniks.stp.network.auth.AuthClient;
import de.uniks.stp.util.JsonUtil;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Pair;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;

public class LoginScreenController implements ControllerInterface {

    private Parent view;
    private TextField nameField;
    private PasswordField passwordField;
    private Button registerButton;
    private Button loginButton;

    private String name;
    private String password;

    public LoginScreenController(Parent view) {
        this.view = view;
    }

    public void init() {
        nameField = (TextField) view.lookup("#name-field");
        passwordField = (PasswordField) view.lookup("#password-field");
        registerButton = (Button) view.lookup("#register-button");
        loginButton = (Button) view.lookup("#login-button");

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

        nameField.clear();
        passwordField.clear();
    }

    private boolean isEmptyInput(String name, String password) {
        return name.isEmpty() || password.isEmpty();
    }

    public void onRegisterButtonClicked(ActionEvent event) {
        readInputFields();

        if (isEmptyInput(name, password)) {
            return;
        }

        AuthClient.tempRegister(this::handleRegisterResponse);
    }

    public void onLoginButtonClicked(ActionEvent _event) {
        readInputFields();

        if (isEmptyInput(name, password)) {
            return;
        }

        AuthClient.login(name, password, this::handleLoginResponse);
    }

    private void handleRegisterResponse(HttpResponse<JsonNode> response) {
        // log user in
        if(HttpResponseHelper.isSuccess(response.getStatus())) {
            AuthClient.login(name, password, this::handleLoginResponse);
            return;
        }
        // Registration failed
    }

    private void handleLoginResponse(HttpResponse<JsonNode> response) {
        System.out.println(response.getBody());
    }

}


