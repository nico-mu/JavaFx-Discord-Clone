package de.uniks.stp.component;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.User;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class UserListEntry extends HBox {

    @FXML
    private Label userNameLabel;

    private final User user;
    private final Router router;

    @AssistedInject
    public UserListEntry(ViewLoader viewLoader,
                         Router router,
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
        this.router = router;

        setUserName(user.getName());

        this.setOnMouseClicked(this::handleClick);
    }

    private void handleClick(MouseEvent mouseEvent) {
        RouteArgs args = new RouteArgs().addArgument(Constants.ROUTE_PRIVATE_CHAT_ARGS, user.getId());
        router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_PRIVATE_CHAT, args);
    }

    public void setUserName(final String userName) {
        userNameLabel.setText(userName);
    }

    @AssistedFactory
    public interface UserListEntryFactory {
        UserListEntry create(User user);
    }
}
