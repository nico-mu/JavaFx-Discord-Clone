package de.uniks.stp;

import de.uniks.stp.model.Accord;
import de.uniks.stp.model.User;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.view.Languages;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

import java.util.Objects;

public class Editor {
    // Connection to model root object
    private Accord accord;

    public Accord getOrCreateAccord() {
        if (Objects.isNull(accord)) {
            accord = new Accord().setLanguage(Languages.GERMAN.key);
        }
        return accord;
    }

    public void setUserKey(String userKey) {
        accord.setUserKey(userKey);
    }

    public User getOrCreateUser(String name, boolean status) {
        User user = new User().setAccord(accord).setName(name).setStatus(status);

        return user;
    }

    public void setCurrentUser(User currentUser) {
        accord.setCurrentUser(currentUser);
    }

    //TODO: put following methods into WelcomeScreenController
    public void onLogoutButtonClicked() {
        RestClient.sendLogoutRequest(this::handleLogoutResponse, accord.getUserKey());
    }

    private void handleLogoutResponse(HttpResponse<JsonNode> response) {
        if (response.isSuccess()) {
            accord.setUserKey("");
            StageManager.showLoginScreen();
            return;
        }
        accord.setUserKey("");
        StageManager.showLoginScreen();
        System.err.println("logout failed");
    }

    /*
    public void init() {
        logoutButton = (JFXButton) view.lookup("#logout-button");

        // Register button event handler
        logoutButton.setOnAction(this::onLogoutButtonClicked);
    }

    public void stop() {
        logoutButton.setOnAction(null);
    }
     */
}
