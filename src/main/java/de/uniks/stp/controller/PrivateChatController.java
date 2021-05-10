package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.component.ChatView;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.User;
import de.uniks.stp.router.Route;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.util.Date;

@Route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + "/chat/:userId")
public class PrivateChatController implements ControllerInterface {
    private static final String ONLINE_USERS_CONTAINER_ID = "#online-users-container";
    private static final String HOME_SCREEN_LABEL_ID = "#home-screen-label";

    private final Parent view;
    private final Editor editor;
    private final User user;
    private ChatView chatView;
    private VBox onlineUsersContainer;
    private Label homeScreenLabel;

    public PrivateChatController(Parent view, Editor editor, User user) {
        this.view = view;
        this.editor = editor;
        this.user = user;
    }

    @Override
    public void init() {
        onlineUsersContainer = (VBox) view.lookup(ONLINE_USERS_CONTAINER_ID);
        homeScreenLabel = (Label) view.lookup(HOME_SCREEN_LABEL_ID);

        showPrivateChatView(user);
    }

    @Override
    public void stop() {
        chatView.stop();
    }

    @Override
    public void route(RouteInfo routeInfo, RouteArgs args) {

    }

    public void showPrivateChatView(User otherUser) {
        homeScreenLabel.setText(otherUser.getName());
        chatView = new ChatView(view);

        chatView.onMessageSubmit((message) -> {
            chatView.appendMessage(new Message()
                .setMessage(message)
                .setSender(editor.getOrCreateAccord().getCurrentUser())
                .setTimestamp(new Date().getTime()));

            // send message to the server

        });
        onlineUsersContainer.getChildren().add(chatView.getComponent());
    }
}
