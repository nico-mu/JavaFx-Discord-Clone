package de.uniks.stp.component;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.ViewLoader;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import java.io.IOException;

public class IntegrationButton extends HBox {


    @FXML
    private ImageView integrationImageView;

    @FXML
    private Pane integrationBadge;


    @AssistedInject
    public IntegrationButton(ViewLoader viewLoader,
                             @Assisted Image image) {

        FXMLLoader fxmlLoader = viewLoader.getFXMLComponentLoader(Components.INTEGRATION_BUTTON);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        integrationImageView.setImage(image);
    }

    public void setErrorMode() {
        integrationBadge.setStyle("-fx-background-color: red");
        this.setDisable(false);
    }

    public void setSuccessMode() {
        integrationBadge.setStyle("-fx-background-color: green");
        this.setDisable(true);
    }

    @AssistedFactory
    public interface IntegrationButtonFactory {
        IntegrationButton create(Image image);
    }
}
