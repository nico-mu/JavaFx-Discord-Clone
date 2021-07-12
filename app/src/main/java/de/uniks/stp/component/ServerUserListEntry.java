package de.uniks.stp.component;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.event.NavBarHomeElementActiveEvent;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.model.User;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class ServerUserListEntry extends HBox {
    @FXML
    private Label userNameLabel;

    private User user;
    private final Router router;
    private final Editor editor;
    private final SessionDatabaseService databaseService;

    @AssistedInject
    public ServerUserListEntry(ViewLoader viewLoader,
                               Router router,
                               Editor editor,
                               SessionDatabaseService databaseService,
                               @Assisted final User user) {
        final FXMLLoader fxmlLoader = viewLoader.getFXMLComponentLoader(Components.USER_LIST_ENTRY);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.user = user;
        this.router = router;
        this.editor = editor;
        this.databaseService = databaseService;

        setUserName(user.getName());
        this.setOnMouseClicked(this::handleClick);
    }

    public void setUserName(final String userName) {
        userNameLabel.setText(userName);
    }

    @AssistedFactory
    public interface ServerUserListEntryFactory {
        ServerUserListEntry create(User user);
    }

    private void handleClick(MouseEvent mouseEvent) {
        User currentUser = editor.getOrCreateAccord().getCurrentUser();
        if (user.getId().equals(currentUser.getId())) {
            return;
        }
        if (databaseService.getConversation(currentUser.getName(), user.getName()).isEmpty()) {
            editor.getOrCreateChatPartnerOfCurrentUser(user.getId(), user.getName());
        }
        //TODO set Home Element active
        RouteArgs args = new RouteArgs().addArgument(Constants.ROUTE_PRIVATE_CHAT_ARGS, user.getId());
        router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_PRIVATE_CHAT, args);
    }
}
