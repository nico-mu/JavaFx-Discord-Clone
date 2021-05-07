package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.router.Route;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

@Route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME)
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
        label.setTextFill(new Color(1, 1, 1, 1));
        label.setFont(new Font(50));
        label.setPrefHeight(200);
        label.setPrefWidth(400);
        view.getChildren().add(label);
    }

    @Override
    public void route(RouteInfo routeInfo, RouteArgs args) {
        //no subroutes
    }

    @Override
    public void stop() {

    }
}
