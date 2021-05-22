package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.io.IOException;

public class ServerUserListEntry extends HBox {
    @FXML
    private Text userNameText;

    private User user;

    public ServerUserListEntry(final User user) {
        final FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.USER_LIST_ENTRY);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.user = user;

        setUserName(user.getName());
    }

    public void setUserName(final String userName) {
        userNameText.setText(userName);
    }

}
