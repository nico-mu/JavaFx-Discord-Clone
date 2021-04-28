package de.uniks.stp.view;

import java.net.URL;

public enum Views {
    START_SCREEN("StartScreen.fxml"); // TODO: To be replaced

    public final URL path;

    Views(String path) {
        this.path = Views.class.getResource(path);
    }
}
