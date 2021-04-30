package de.uniks.stp.component;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class NavBarElement {

    private final String NAV_ELEMENT_MARKER_ID = "#nav-element-marker";
    private final String NAV_BAR_ELEMENT_ID = "#nav-bar-element";
    private final Pane navBarElementMarker;
    private final Pane navBarElement;
    private Parent view;

    public NavBarElement(Parent root) {
        view = root;
        navBarElement = (Pane)view.lookup(NAV_BAR_ELEMENT_ID);
        navBarElementMarker = (Pane)view.lookup(NAV_ELEMENT_MARKER_ID);
        Tooltip.install(navBarElement, new Tooltip("Home"));

        navBarElement.setOnMouseClicked(this::onMouseClicked);
    }

    private void onMouseClicked(MouseEvent mouseEvent) {
        navBarElementMarker.setVisible(true);
    }
}
