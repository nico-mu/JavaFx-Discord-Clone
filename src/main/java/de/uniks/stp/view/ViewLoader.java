package de.uniks.stp.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class ViewLoader {
    private static ResourceBundle resourceBundle;

    public static Parent loadView(final Views alias) {
        Parent load = null;
        try {
            //add classloader for bundle directory to load it into resource bundle
            ClassLoader loader = new URLClassLoader(new URL[]{ViewLoader.class.getResource("../bundle/")});

            //TODO: insert current used language here (accord.getLanguage?)
            //load resource bundle for given language
            resourceBundle = ResourceBundle.getBundle("language", new Locale(Languages.GERMAN.key), loader);

            System.out.println(resourceBundle.getString("LBL_LOGIN"));

            //load view with given resource bundle
            load = FXMLLoader.load(Objects.requireNonNull(ViewLoader.class.getResource(alias.path)), resourceBundle);
        } catch (IOException e) {
            System.err.println("View " + alias.path + " could not be loaded.");
            e.printStackTrace();
        }
        return load;
    }

    public static String loadLabel(String label) {
        return resourceBundle.getString(label);
    }
}
