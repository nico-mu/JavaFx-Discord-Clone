package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class InviteList extends ScrollPane {

    private final ObservableList<Node> inviteListEntries;
    @FXML
    private VBox container;


    public InviteList() {
        final FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.USER_LIST);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            inviteListEntries = container.getChildren();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void addInviteListEntry(final InviteListEntry item) {
        inviteListEntries.add(item);
    }

    public void removeInviteListEntry(final InviteListEntry item) {
        inviteListEntries.remove(item);
    }
}

