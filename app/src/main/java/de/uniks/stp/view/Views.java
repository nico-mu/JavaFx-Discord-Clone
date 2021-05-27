package de.uniks.stp.view;

import java.net.URL;

public enum Views {
    MAIN_SCREEN("MainScreen.fxml"),
    HOME_SCREEN("HomeScreen.fxml"),
    LOGIN_SCREEN("LoginScreen.fxml"),
    SERVER_SCREEN("ServerScreen.fxml"),
    USER_INFO_SCREEN("UserInfoScreen.fxml"),
    SETTINGS_MODAL("SettingsModal.fxml"),
    ADD_SERVER_MODAL("AddServerModal.fxml"),
    SERVER_SETTINGS_MODAL("ServerSettingsModal.fxml"),
    CREATE_CATEGORY_MODAL("CreateCategoryModal.fxml");

    public final URL path;

    Views(String path) {
        this.path = Views.class.getResource(path);
    }
}
