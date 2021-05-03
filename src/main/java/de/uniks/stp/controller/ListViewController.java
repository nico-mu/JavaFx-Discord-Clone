package de.uniks.stp.controller;

import de.uniks.stp.Editor;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;

public abstract class ListViewController implements ControllerInterface {

    protected final Editor editor;
    private final AnchorPane view;
    protected ScrollPane scrollPane;
    protected VBox container;

    public ListViewController(Parent view, Editor editor) {
        this.view = (AnchorPane) view;
        this.editor = editor;
    }

    @Override
    public void init() {
        container = new VBox();
        container.setPadding(new Insets(10.0d, 5.0d, 10.0d, 5.0d));
        container.setSpacing(10.0d);
        scrollPane = new ScrollPane(container);
        this.view.getChildren().add(scrollPane);
        scrollPane.setPrefHeight(view.getPrefHeight());
    }

    abstract protected void callback(HttpResponse<JsonNode> response);

    @Override
    public void stop() {

    }
}
