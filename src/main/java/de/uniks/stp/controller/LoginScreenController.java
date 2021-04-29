package de.uniks.stp.controller;

import de.uniks.stp.network.auth.AuthClient;
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

    public LoginScreenController(Parent view) {
        this.view = view;
    }

    public void init() {
        nameField = (TextField) view.lookup("#name-field");
        passwordField = (PasswordField) view.lookup("#password-field");
        registerButton = (Button) view.lookup("#register-button");

        // Register button event handler
        registerButton.setOnAction(this::onRegisterButtonClicked);
    }

    public void stop() {

    }

    private Pair<String, String> readInputFields() {
        String name = nameField.getText();
        String password = passwordField.getText();

        nameField.clear();
        passwordField.clear();

        return new Pair(name, password);
    }

    public void onRegisterButtonClicked(ActionEvent event) {
        Pair<String, String> fieldValues = readInputFields();
        String name = fieldValues.getKey();
        String password = fieldValues.getValue();

        System.out.println(name + " " + password);

        // AuthClient.register(name, password, this::handleRegisterResponse);
    }

    public void login() {

    }

    public void onLoginButtonClicked() {
        // Login
    }

    private void handleRegisterResponse(HttpResponse<JsonNode> jsonNodeHttpResponse) {
        // Create user and log user in
    }

}


