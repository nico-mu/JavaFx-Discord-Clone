package de.uniks.stp;

import de.uniks.stp.component.Components;
import de.uniks.stp.view.Languages;
import de.uniks.stp.view.Views;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class ViewLoader {
    private static ResourceBundle resourceBundle;
    private static ClassLoader loader;

    static {
        //add classloader for bundle directory to load it into resource bundle
        loader = new URLClassLoader(new URL[]{ViewLoader.class.getResource("./bundle/")});
        changeLanguage(Languages.GERMAN);
    }

    public static Parent loadView(final Views alias) {
        return getParent(alias.path);
    }

    public static Parent loadComponent(final Components alias) {
        return getParent(alias.path);
    }

    public static FXMLLoader getFXMLComponentLoader(final Components alias) {
        return new FXMLLoader(alias.path, resourceBundle);
    }

    public static Image loadImage(String name) {
        try {
            final InputStream inputStream = Objects.requireNonNull(ViewLoader.class.getResource("./pictures/" + name)).openStream();
            return new Image(inputStream);
        } catch (IOException e) {
            System.err.println("Picture " + name + "could not be loaded!");
            e.printStackTrace();
        }
       return null;
    }

    private static Parent getParent(final URL path) {
        Parent load = null;
        try {
            //load view with given resource bundle
            load = FXMLLoader.load(path, resourceBundle);
        } catch (IOException e) {
            System.err.println("View " + path.getPath() + " could not be loaded.");
            e.printStackTrace();
        }
        return load;
    }

    public static void changeLanguage(Languages language) {
        //TODO: insert current used language here (accord.getLanguage?)
        //load resource bundle for given language
        resourceBundle = ResourceBundle.getBundle("language", new Locale(language.key), loader);
    }

    public static String loadLabel(String label) {
        return resourceBundle.getString(label);
    }
}
