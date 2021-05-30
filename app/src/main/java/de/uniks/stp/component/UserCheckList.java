package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class UserCheckList extends ScrollPane {

    private final ObservableList<Node> userListEntries;
    @FXML
    private VBox container;

    public UserCheckList() {
        final FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.USER_LIST);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            userListEntries = container.getChildren();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void addUserCheckListEntry(final UserCheckListEntry item) {
        userListEntries.add(item);
    }

    public void removeUserCheckListEntry(final UserCheckListEntry item) {
        userListEntries.remove(item);
    }

    public void clearUserCheckList(){
        userListEntries.clear();
    }
}
