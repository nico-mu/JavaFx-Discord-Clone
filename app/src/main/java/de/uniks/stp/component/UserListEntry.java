package de.uniks.stp.component;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class UserListEntry extends HBox {

    @FXML
    private Label userNameLabel;

    protected final User user;

    @AssistedInject
    public UserListEntry(ViewLoader viewLoader,
                         @Assisted final User user) {
        final FXMLLoader fxmlLoader = viewLoader.getFXMLComponentLoader(Components.USER_LIST_ENTRY);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        this.setId(user.getId() + "-UserListEntry");

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.user = user;

        setUserName(user.getName());
    }

    public void setUserName(final String userName) {
        userNameLabel.setText(userName);
    }

    @AssistedFactory
    public interface UserListEntryFactory {
        UserListEntry create(User user);
    }
}
