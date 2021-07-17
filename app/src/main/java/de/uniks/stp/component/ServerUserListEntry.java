package de.uniks.stp.component;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.User;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.scene.input.MouseEvent;

public class ServerUserListEntry extends UserListEntry {
    private final Editor editor;
    private final Router router;

    @AssistedInject
    public ServerUserListEntry(ViewLoader viewLoader,
                               Router router,
                               Editor editor,
                               @Assisted final User user) {
        super(viewLoader, user);
        this.setId(user.getId() + "-ServerUserListEntry");

        this.editor = editor;
        this.router = router;
        setOnMouseClicked(this::handleClick);
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
        editor.getOrCreateChatPartnerOfCurrentUser(user.getId(), user.getName());
        RouteArgs args = new RouteArgs().addArgument(Constants.ROUTE_PRIVATE_CHAT_ARGS, user.getId());
        router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_PRIVATE_CHAT, args);
    }
}
