package de.uniks.stp.view;

import java.net.URL;

public enum Views {
    MAIN_SCREEN("MainScreen.fxml"),
    HOME_SCREEN("HomeScreen.fxml"),
    LOGIN_SCREEN("LoginScreen.fxml"),
    SERVER_SCREEN("ServerScreen.fxml"),
    USER_SCREEN("UserScreen.fxml");

    public final URL path;

    Views(String path) {
        this.path = Views.class.getResource(path);
    }
}
