package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.User;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.LinkedList;
import java.util.function.Consumer;

public class UserListEntry extends HBox {

    @FXML
    private Text userNameText;
    private final User user;

    public UserListEntry(final User user) {
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

        userNameText.setOnMouseClicked(this::handleClick);
    }

    private void handleClick(MouseEvent mouseEvent) {
        Router.route("/main/home/chat/:userId", new RouteArgs().setKey(":userId").setValue(user.getId()));
    }

    public void setUserName(final String userName) {
        userNameText.setText(userName);
    }
}
