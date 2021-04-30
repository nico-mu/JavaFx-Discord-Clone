package de.uniks.stp.controller;

import de.uniks.stp.ViewLoader;
import de.uniks.stp.component.Components;
import de.uniks.stp.component.NavBarElement;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class MainScreenController implements ControllerInterface {

    private final String NAV_BAR_ID = "#nav-bar";
    private final String USER_SETTINGS_PANE_ID = "#user-settings-pane";
    private final String SUBVIEW_CONTAINER_ID = "#subview-container";
    private final Parent view;
    private AnchorPane navBar;
    private AnchorPane userSettingsPane;
    private AnchorPane subViewContainer;

    public MainScreenController(Parent view) {
        this.view = view;
    }

    @Override
    public void init() {
        this.navBar = (AnchorPane)view.lookup(NAV_BAR_ID);
        this.userSettingsPane = (AnchorPane)view.lookup(USER_SETTINGS_PANE_ID);
        this.subViewContainer = (AnchorPane)view.lookup(SUBVIEW_CONTAINER_ID);

        VBox container = new VBox();
        container.setPadding(new Insets(10.0d, 5.0d, 10.0d, 5.0d));
        container.setSpacing(10.0d);
        container.getStyleClass().add("nav-bar-vbox");
        ScrollPane scroll = new ScrollPane(container);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("nav-bar-vbox");
        this.navBar.getChildren().add(scroll);
        scroll.setPrefHeight(navBar.getPrefHeight());

        for(int i = 0; i < 20; i++) {
            Parent elem = ViewLoader.loadComponent(Components.NAV_BAR_ELEMENT);
            container.getChildren().add(elem);
            new NavBarElement(elem);
        }




    }

    @Override
    public void stop() {

    }
}
