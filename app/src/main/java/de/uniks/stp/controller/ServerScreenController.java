package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.model.Server;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.uniks.stp.view.Views.SERVER_SCREEN;

@Route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER)
public class ServerScreenController implements ControllerInterface {

    private static final String SERVER_NAME_LABEL_ID = "#server-name-label";
    private AnchorPane view;
    private FlowPane serverScreenView;
    private final Editor editor;
    private final Server model;
    private Label serverNameLabel;

    public ServerScreenController(Parent view, Editor editor, Server model) {
        this.view = (AnchorPane)view;
        this.editor = editor;
        this.model = model;
    }

    @Override
    public void init() {
       serverScreenView = (FlowPane) ViewLoader.loadView(SERVER_SCREEN);
       view.getChildren().add(serverScreenView);
       serverNameLabel = (Label)view.lookup(SERVER_NAME_LABEL_ID);
       serverNameLabel.setText(model.getName());
    }

    @Override
    public void route(RouteInfo routeInfo, RouteArgs args) {

    }

    @Override
    public void stop() {

    }
}
