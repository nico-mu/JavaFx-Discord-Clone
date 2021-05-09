package de.uniks.stp.controller;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.component.ChatView;
import de.uniks.stp.model.Message;
import de.uniks.stp.router.Route;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.component.UserList;
import de.uniks.stp.view.Views;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.util.Date;

@Route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME)
public class HomeScreenController implements ControllerInterface {
    private static final String ACTIVE_CHATS_CONTAINER_ID = "#active-chats-container";
    private static final String ONLINE_USERS_CONTAINER_ID = "#online-users-container";
    private static final String TOGGLE_ONLINE_BUTTON_ID = "#toggle-online-button";

    private final AnchorPane view;
    private final Editor editor;
    private ChatView chatView;
    private AnchorPane container;
    private JFXButton toggleOnlineUsersButton;

    private AnchorPane homeScreenView;
    private AnchorPane activeChatsContainer;
    private VBox onlineUsersContainer;

    private UserListController userListController;
    private UserList userList;

    private boolean showOnlineUsers = true;

    HomeScreenController(Parent view, Editor editor) {
        this.view = (AnchorPane) view;
        this.editor = editor;
        this.chatView = new ChatView(view);
    }

    @Override
    public void init() {
        homeScreenView = (AnchorPane) ViewLoader.loadView(Views.HOME_SCREEN);
        activeChatsContainer = (AnchorPane) homeScreenView.lookup(ACTIVE_CHATS_CONTAINER_ID);
        onlineUsersContainer = (VBox) homeScreenView.lookup(ONLINE_USERS_CONTAINER_ID);
        toggleOnlineUsersButton = (JFXButton) homeScreenView.lookup(TOGGLE_ONLINE_BUTTON_ID);

        view.getChildren().add(homeScreenView);

        userList = new UserList();
        userListController = new UserListController(userList, editor);
        userListController.init();
        onlineUsersContainer.getChildren().add(userList);

        toggleOnlineUsersButton.setOnMouseClicked(this::toggleOnlineUsers);
    }

    private void toggleOnlineUsers(MouseEvent mouseEvent) {
        onlineUsersContainer.getChildren().remove(0);

        if (!showOnlineUsers) {
            showOnlineUsers = true;
            userList = new UserList();
            userListController = new UserListController(userList, editor);
            userListController.init();
            onlineUsersContainer.getChildren().add(userList);
            return;
        }
        showOnlineUsers = false;
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
    }

    @Override
    public void stop() {
        userListController.stop();
    }
}
