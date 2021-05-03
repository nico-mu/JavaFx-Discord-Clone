package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import de.uniks.stp.controller.CustomEventCallback;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

abstract public class NavBarElement {

    protected final Pane navBarElementMarker;
    protected final Pane navBarElement;
    protected final ImageView navBarImageView;
    private final String NAV_ELEMENT_MARKER_ID = "#nav-element-marker";
    private final String NAV_BAR_ELEMENT_ID = "#nav-bar-element";
    private final String NAV_BAR_IMAGE_VIEW = "#nav-bar-image-view";
    private final CustomEventCallback eventCallback;
    protected Parent view;


    public NavBarElement(CustomEventCallback eventCallback) {
        view = ViewLoader.loadComponent(Components.NAV_BAR_ELEMENT);
        navBarElement = (Pane) view.lookup(NAV_BAR_ELEMENT_ID);
        navBarElementMarker = (Pane) view.lookup(NAV_ELEMENT_MARKER_ID);
        navBarImageView = (ImageView) view.lookup(NAV_BAR_IMAGE_VIEW);
        navBarElement.setOnMouseClicked(this::onMouseClicked);
        this.eventCallback = eventCallback;
    }

    private void onMouseClicked(MouseEvent mouseEvent) {
        //parent controller set active element
        eventCallback.handleEvent(this);
    }

    public void setActive(boolean active) {
        Platform.runLater(() -> navBarElementMarker.setVisible(active));
    }


    public Parent getRootElement() {
        return view;
    }
}
