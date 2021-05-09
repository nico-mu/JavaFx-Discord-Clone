package de.uniks.stp.view;

import java.net.URL;

public enum Views {
    MAIN_SCREEN("MainScreen.fxml"),
    HOME_SCREEN("HomeScreen.fxml"),
    LOGIN_SCREEN("LoginScreen.fxml");

    public final URL path;

    Views(String path) {
        this.path = Views.class.getResource(path);
    }
}
