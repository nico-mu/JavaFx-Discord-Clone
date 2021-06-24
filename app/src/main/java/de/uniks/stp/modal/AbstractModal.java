package de.uniks.stp.modal;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public abstract class AbstractModal extends Stage {

    protected Scene scene;
    protected Parent view;
    protected Pane rootElement;

    AbstractModal(Parent root) {
        view = root;

        if(view instanceof Pane) {
            rootElement = (Pane) view;
        }
        else {
            throw new RuntimeException("Modal fxml root element must be of type Pane");
        }

        scene = new Scene(root, rootElement.getPrefWidth(), rootElement.getPrefHeight());
        this.initModality(Modality.APPLICATION_MODAL);
        this.initStyle(StageStyle.UTILITY);
        this.setScene(scene);
    }
}
