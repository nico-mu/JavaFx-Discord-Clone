package de.uniks.stp;

import de.uniks.stp.component.Components;
import de.uniks.stp.view.Languages;
import de.uniks.stp.view.Views;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;

public class ViewLoader {
    public static Parent loadView(final Views alias) {
        return getParent(alias.path);
    }

    public static Parent loadComponent(final Components alias) {
        return getParent(alias.path);
    }

    private static Parent getParent(final URL path) {
        Parent load = null;
        try {
            //add classloader for bundle directory to load it into resource bundle
            ClassLoader loader = new URLClassLoader(new URL[]{ViewLoader.class.getResource("./bundle/")});

            //TODO: insert current used language here (accord.getLanguage?)
            //load resource bundle for given language
            ResourceBundle resourceBundle = ResourceBundle.getBundle("language", new Locale(Languages.GERMAN.key), loader);

            //load view with given resource bundle
            load = FXMLLoader.load(path, resourceBundle);
        } catch (IOException e) {
            System.err.println("View " + path.getPath() + " could not be loaded.");
            e.printStackTrace();
        }
        return load;
    }
}
