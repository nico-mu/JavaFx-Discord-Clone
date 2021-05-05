package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import de.uniks.stp.controller.CustomEventCallback;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.io.IOException;

abstract public class NavBarElement extends HBox {

    @FXML
    protected  Pane navBarElementMarker;

    @FXML
    protected Pane navBarElement;

    @FXML
    protected ImageView imageView;

    private CustomEventCallback eventCallback;

    public NavBarElement() {
        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.NAV_BAR_ELEMENT);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        navBarElement.setOnMouseClicked(this::onMouseClicked);
    }

    private void onMouseClicked(MouseEvent mouseEvent) {
        eventCallback.handleEvent(this);
    }

    public void setActive(boolean active) {
        navBarElementMarker.setVisible(active);
    }

    public void onClick(CustomEventCallback eventCallback) {
        this.eventCallback = eventCallback;
    }
}
