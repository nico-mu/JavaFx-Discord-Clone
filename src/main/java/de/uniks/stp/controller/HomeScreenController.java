package de.uniks.stp.controller;

import de.uniks.stp.Editor;
import de.uniks.stp.annotation.Route;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

@Route("/main/home")
public class HomeScreenController implements ControllerInterface {

    private final AnchorPane view;
    private final Editor editor;

    HomeScreenController(Parent view, Editor editor) {
        this.view = (AnchorPane) view;
        this.editor = editor;
    }

    @Override
    public void init() {
        Label label = new Label("Home Screen");
        label.setPrefHeight(200);
        label.setPrefWidth(400);
        view.getChildren().add(label);
    }

    @Override
    public void route(RouteInfo routeInfo) {
        //no subroutes
    }

    @Override
    public void stop() {

    }
}
