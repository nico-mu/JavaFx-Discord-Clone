package de.uniks.stp.controller;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.component.ChatView;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.router.Route;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.component.UserList;
import de.uniks.stp.router.Router;
import de.uniks.stp.view.Views;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.util.Date;
import java.util.Objects;

@Route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME)
public class HomeScreenController implements ControllerInterface {
    private static final String ACTIVE_CHATS_CONTAINER_ID = "#active-chats-container";
    private static final String ONLINE_USERS_CONTAINER_ID = "#online-users-container";
    private static final String TOGGLE_ONLINE_BUTTON_ID = "#toggle-online-button";
    private static final String HOME_SCREEN_LABEL_ID = "#home-screen-label";

    private final AnchorPane view;
    private final Editor editor;
    private ChatView chatView;
    private AnchorPane activeChatsContainer;
    private VBox onlineUsersContainer;
    private Label homeScreenLabel;

    private UserListController userListController;
    private boolean showOnlineUsers = true;

    HomeScreenController(Parent view, Editor editor) {
        this.view = (AnchorPane) view;
        this.editor = editor;
        this.chatView = new ChatView(view);
    }

    @Override
    public void init() {
        AnchorPane homeScreenView = (AnchorPane) ViewLoader.loadView(Views.HOME_SCREEN);
        view.getChildren().add(homeScreenView);

        JFXButton toggleOnlineUsersButton = (JFXButton) homeScreenView.lookup(TOGGLE_ONLINE_BUTTON_ID);
        activeChatsContainer = (AnchorPane) homeScreenView.lookup(ACTIVE_CHATS_CONTAINER_ID);
        onlineUsersContainer = (VBox) homeScreenView.lookup(ONLINE_USERS_CONTAINER_ID);
        homeScreenLabel = (Label) homeScreenView.lookup(HOME_SCREEN_LABEL_ID);

        showOnlineUserList();

        toggleOnlineUsersButton.setOnMouseClicked(this::toggleShowOnlineUsers);
    }

    public void toggleShowOnlineUsers(MouseEvent mouseEvent) {
        showOnlineUsers = !showOnlineUsers;

        if (showOnlineUsers) {
            showOnlineUserList();
            // Router.route("/main/home");
            chatView = null;
            return;
        }

        // Router.route("/main/home/chat/:userId", new RouteArgs().setKey(":userId").setValue("Bob"));

        showPrivateChatView(new User().setName("Bob"));
    }

    private void showOnlineUserList() {
        if (onlineUsersContainer.getChildren().size() > 0) {
            onlineUsersContainer.getChildren().clear();
        }

        UserList userList = new UserList();
        userListController = new UserListController(userList, editor);
        userListController.init();
        userListController.onUserSelected(id -> {
            showOnlineUsers = false;
            showPrivateChatView(editor.getUserById(id));
        });
        onlineUsersContainer.getChildren().add(userList);
        homeScreenLabel.setText(ViewLoader.loadLabel(Constants.LBL_ONLINE_USERS));
    }

    private void showPrivateChatView(User otherUser) {
        if (onlineUsersContainer.getChildren().size() > 0) {
            onlineUsersContainer.getChildren().clear();
        }

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

    @Override
    public void route(RouteInfo routeInfo, RouteArgs args) {
        //no subroutes
        String subroute = routeInfo.getSubControllerRoute();

        /* if (subroute.equals("/chat/:userId")) {
            PrivateChatController privateScreenController = new PrivateChatController(this.onlineUsersContainer, this.editor);
            privateScreenController.init();
            showPrivateChatView(new User().setName("Bob"));
            Router.addToControllerCache(routeInfo.getFullRoute(), privateScreenController);
        } */
    }

    @Override
    public void stop() {
        userListController.stop();
        chatView = null;
    }
}
