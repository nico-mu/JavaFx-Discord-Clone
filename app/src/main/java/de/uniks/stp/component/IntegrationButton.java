package de.uniks.stp.component;

import com.jfoenix.controls.JFXToggleButton;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.ViewLoader;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class IntegrationButton extends VBox {


    @FXML
    private ImageView integrationImageView;

    @FXML
    private Pane integrationBadge;

    @FXML
    private HBox integrationActiveContainer;

    @FXML
    private ImageView integrationCloseImage;

    @FXML
    private HBox integrationButtonContainer;

    @FXML
    private JFXToggleButton integrationToggleButton;


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

    public void setOnButtonClicked(EventHandler<? super MouseEvent> value) {
        integrationButtonContainer.setOnMouseClicked(value);
    }

    public void setOnRemoveClicked(EventHandler<? super MouseEvent> value) {
        integrationCloseImage.setOnMouseClicked((MouseEvent event) -> {
            value.handle(event);
            hideActiveContainer();
        });
    }

    public void setOnToggleClicked(EventHandler<? super MouseEvent> value) {
        integrationToggleButton.setOnMouseClicked(value);
    }

    private void hideActiveContainer() {
        integrationActiveContainer.setManaged(false);
        integrationActiveContainer.setVisible(false);
        integrationCloseImage.setManaged(false);
        integrationCloseImage.setVisible(false);
        integrationBadge.setStyle("-fx-background-color: #4059a9");
    }

    public void setErrorMode() {
        integrationBadge.setStyle("-fx-background-color: red");
        this.setDisable(false);
    }

    public void setSuccessMode() {
        integrationBadge.setStyle("-fx-background-color: green");
        integrationActiveContainer.setManaged(true);
        integrationActiveContainer.setVisible(true);
        integrationCloseImage.setManaged(true);
        integrationCloseImage.setVisible(true);
        integrationToggleButton.setSelected(true);
        integrationButtonContainer.setDisable(true);
    }

    @AssistedFactory
    public interface IntegrationButtonFactory {
        IntegrationButton create(Image image);
    }
}
