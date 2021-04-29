package de.uniks.stp.view;

public enum Views {
    LOGIN_SCREEN("LoginScreen.fxml"),
    LOGIN_SCREEN2("LoginScreen2.fxml");

    public final String path;

    Views(String path) {
        this.path = path;
    }
}
