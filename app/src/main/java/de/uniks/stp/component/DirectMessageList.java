package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class DirectMessageList extends ScrollPane {
    private final ObservableList<Node> directMessageListEntries;
    @FXML
    private VBox container;

    public DirectMessageList() {
        final FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.USER_LIST);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            directMessageListEntries = container.getChildren();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        container.setPadding(new Insets(10.0d, 5.0d, 10.0d, 5.0d));
        container.setSpacing(10.0d);
    }

    public void addElement(final DirectMessageEntry item) {
        directMessageListEntries.add(item);
    }

    public void removeElement(final DirectMessageEntry item) {
        directMessageListEntries.remove(item);
    }

    @Override
    public void requestFocus() {}
}
