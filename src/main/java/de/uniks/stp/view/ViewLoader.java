package de.uniks.stp.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.util.Objects;

public class ViewLoader {
    public static Parent loadView(final Views alias) {
        Parent load = null;
        try {
            load = FXMLLoader.load(Objects.requireNonNull(Views.class.getResource(alias.path)));
        } catch (IOException e) {
            System.err.println("View " + alias.path + " could not be loaded.");
            e.printStackTrace();
        }
        return load;
    }
}
