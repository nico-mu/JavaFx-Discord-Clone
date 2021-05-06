package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import javafx.application.Platform;
import javafx.scene.layout.VBox;

public class UserList extends AbstractComponent<VBox> {

    public UserList() {
        setRootElement((VBox) ViewLoader.loadComponent(Components.USER_LIST));
    }

    public void addUserListEntry(final UserListEntry item) {
        Platform.runLater(() -> getRootElement().getChildren().add(item.getRootElement()));
    }

    public void removeUserListEntry(final UserListEntry item) {
        Platform.runLater(() -> getRootElement().getChildren().remove(item.getRootElement()));
    }
}
