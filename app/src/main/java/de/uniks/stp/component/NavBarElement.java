package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;

import java.io.IOException;

abstract public class NavBarElement extends HBox {

    @FXML
    protected Pane navBarElementMarker;

    @FXML
    protected Pane navBarElement;

    @FXML
    protected ImageView imageView;

    @FXML
    protected Label notificationLabel;

    @FXML
    protected Circle circle;

    protected final ViewLoader viewLoader;

    public NavBarElement(ViewLoader viewLoader) {
        this.viewLoader = viewLoader;
        FXMLLoader fxmlLoader = viewLoader.getFXMLComponentLoader(Components.NAV_BAR_ELEMENT);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        navBarElement.setOnMouseClicked(this::onMouseClicked);
    }

    public void installTooltip(String text) {
        Tooltip.install(navBarElement, new Tooltip(text));
    }

    protected void onMouseClicked(MouseEvent mouseEvent) {}

    public void setActive(boolean active) {
        navBarElementMarker.setVisible(active);
    }
}
