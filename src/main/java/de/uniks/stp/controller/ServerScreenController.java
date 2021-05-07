package de.uniks.stp.controller;

import de.uniks.stp.Editor;
import de.uniks.stp.model.Server;
import de.uniks.stp.router.Route;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

@Route("/main/server/:id")
public class ServerScreenController implements ControllerInterface {

    private Parent view;
    private AnchorPane anchorPane;
    private final Editor editor;
    private final Server model;

    public ServerScreenController(Parent view, Editor editor, Server model) {
        this.view = view;
        this.editor = editor;
        this.model = model;
        anchorPane = (AnchorPane) view;
    }

    @Override
    public void init() {
        VBox vBox = new VBox();
        anchorPane.getChildren().add(vBox);

        Label label = new Label("Server Screen id: ");
        label.setTextFill(new Color(1, 1, 1, 1));
        label.setFont(new Font(50));
        label.setPrefHeight(200);
        label.setPrefWidth(400);
        vBox.getChildren().add(label);

        Label label2 = new Label( model.getId());
        label2.setTextFill(new Color(1, 1, 1, 1));
        label2.setFont(new Font(25));
        label2.setPrefHeight(200);
        label2.setPrefWidth(400);
        vBox.getChildren().add(label2);
    }

    @Override
    public void route(RouteInfo routeInfo, RouteArgs args) {

    }

    @Override
    public void stop() {

    }
}
