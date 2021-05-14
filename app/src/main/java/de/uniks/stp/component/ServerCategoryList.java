package de.uniks.stp.component;
import de.uniks.stp.ViewLoader;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ServerCategoryList extends ScrollPane {
    @FXML
    protected VBox container;

    public ServerCategoryList() {
        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.SERVER_CATEGORY_LIST);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        container.setPadding(new Insets(10.0d, 5.0d, 10.0d, 5.0d));
        container.setSpacing(10.0d);
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    public void addElement(ServerCategoryElement element) {
        container.getChildren().add(element);
    }

    public void removeElement(ServerCategoryElement element) {
        this.container.getChildren().remove(element);
    }
}
