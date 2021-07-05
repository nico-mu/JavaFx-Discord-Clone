package de.uniks.stp;

import de.uniks.stp.component.Components;
import de.uniks.stp.view.Languages;
import de.uniks.stp.view.Views;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class ViewLoader {

    @Inject
    public ViewLoader() {

    }

    private final Logger log = LoggerFactory.getLogger(ViewLoader.class);

    private ResourceBundle resourceBundle;

    public Parent loadView(final Views alias) {
        return getParent(alias.path);
    }

    public Parent loadComponent(final Components alias) {
        return getParent(alias.path);
    }

    public FXMLLoader getFXMLComponentLoader(final Components alias) {
        return new FXMLLoader(alias.path, resourceBundle);
    }

    public Image loadImage(String name) {
        final InputStream inputStream = Objects.requireNonNull(ViewLoader.class.getResourceAsStream("img/" + name));
        return new Image(inputStream);
    }

    private Parent getParent(final URL path) {
        Parent load = null;
        try {
            //load view with given resource bundle
            load = FXMLLoader.load(path, resourceBundle);
        } catch (IOException e) {
            log.error("View " + path.getPath() + " could not be loaded.", e);
        }
        return load;
    }

    public void changeLanguage(Languages language) {
        //load resource bundle for given language
        resourceBundle = ResourceBundle.getBundle("de.uniks.stp.bundle.language", new Locale(language.key));
    }

    public Locale getCurrentLocale() {
        return resourceBundle.getLocale();
    }

    public String loadLabel(String label) {
        return resourceBundle.getString(label);
    }
}
