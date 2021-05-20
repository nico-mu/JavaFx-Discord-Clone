package de.uniks.stp;

import de.uniks.stp.component.Components;
import de.uniks.stp.view.Languages;
import de.uniks.stp.view.Views;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class ViewLoader {
    private static final Logger log = LoggerFactory.getLogger(ViewLoader.class);

    private static ResourceBundle resourceBundle;

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
        final InputStream inputStream = Objects.requireNonNull(ViewLoader.class.getResourceAsStream("img/" + name));
        return new Image(inputStream);
    }

    private static Parent getParent(final URL path) {
        Parent load = null;
        try {
            //load view with given resource bundle
            load = FXMLLoader.load(path, resourceBundle);
        } catch (IOException e) {
            log.error("View " + path.getPath() + " could not be loaded.", e);
        }
        return load;
    }

    public static void changeLanguage(Languages language) {
        //load resource bundle for given language
        resourceBundle = ResourceBundle.getBundle("de.uniks.stp.bundle.language", new Locale(language.key));
    }

    public static String loadLabel(String label) {
        return resourceBundle.getString(label);
    }
}
