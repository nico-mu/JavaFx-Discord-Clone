package de.uniks.stp.controller;

import de.uniks.stp.network.auth.AuthClient;
import javafx.scene.Parent;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;

public class LoginScreenController implements ControllerInterface {

    private Parent view;

    public LoginScreenController(Parent view) {
        this.view = view;
    }

    public void init() {
        // Register button event handler

    }

    public void stop() {

    }

    public void onRegisterButtonClicked() {
        AuthClient.register("", "", this::handleRegisterResponse);
        // Login
    }

    public void login() {

    }

    public void onLoginButtonClicked() {
        // Login
    }

    private void handleRegisterResponse(HttpResponse<JsonNode> jsonNodeHttpResponse) {
        // Create user and login
    }

}


