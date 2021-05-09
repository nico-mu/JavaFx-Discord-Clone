package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.component.UserList;
import de.uniks.stp.router.Route;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import de.uniks.stp.view.Views;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

@Route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME)
public class HomeScreenController implements ControllerInterface {
    private static final String ACTIVE_CHATS_CONTAINER_ID = "#active-chats-container";
    private static final String ONLINE_USERS_CONTAINER_ID = "#online-users-container";

    private final AnchorPane view;
    private final Editor editor;

    private AnchorPane homeScreenView;
    private AnchorPane activeChatsContainer;
    private VBox onlineUsersContainer;

    private UserListController userListController;
    private UserList userList;

    HomeScreenController(Parent view, Editor editor) {
        this.view = (AnchorPane) view;
        this.editor = editor;
    }

    @Override
    public void init() {
        homeScreenView = (AnchorPane) ViewLoader.loadView(Views.HOME_SCREEN);
        activeChatsContainer = (AnchorPane) homeScreenView.lookup(ACTIVE_CHATS_CONTAINER_ID);
        onlineUsersContainer = (VBox) homeScreenView.lookup(ONLINE_USERS_CONTAINER_ID);

        view.getChildren().add(homeScreenView);

        userList = new UserList();
        userListController = new UserListController(userList, editor);
        userListController.init();
        final ObservableList<Node> children = onlineUsersContainer.getChildren();
        children.add(userList);
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
