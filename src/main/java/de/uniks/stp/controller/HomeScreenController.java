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
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME)
public class HomeScreenController implements ControllerInterface {
    private static final String ONLINE_USERS_CONTAINER_ID = "#online-users-container";
    private static final String TOGGLE_ONLINE_BUTTON_ID = "#toggle-online-button";
    private static final String HOME_SCREEN_LABEL_ID = "#home-screen-label";
    private static final String AVAILABLE_DM_USERS_ID = "#dm-user-list";

    private final AnchorPane view;
    private final Editor editor;
    private VBox onlineUsersContainer;
    private JFXButton showOnlineUsersButton;
    private Label homeScreenLabel;
    private VBox directMessageUsersList;

    private final Map<String, Boolean> knownUsers = new ConcurrentHashMap<>();
    private UserListController userListController;
    private User selectedOnlineUser;

    HomeScreenController(Parent view, Editor editor) {
        this.view = (AnchorPane) view;
        this.editor = editor;
    }

    @Override
    public void init() {
        AnchorPane homeScreenView = (AnchorPane) ViewLoader.loadView(Views.HOME_SCREEN);
        view.getChildren().add(homeScreenView);

        showOnlineUsersButton = (JFXButton) homeScreenView.lookup(TOGGLE_ONLINE_BUTTON_ID);
        onlineUsersContainer = (VBox) homeScreenView.lookup(ONLINE_USERS_CONTAINER_ID);
        homeScreenLabel = (Label) homeScreenView.lookup(HOME_SCREEN_LABEL_ID);
        directMessageUsersList = (VBox) ((ScrollPane) homeScreenView.lookup(AVAILABLE_DM_USERS_ID)).getContent();

        showOnlineUsersButton.setOnMouseClicked(this::handleShowOnlineUsersClicked);

    }

    @Override
    public void route(RouteInfo routeInfo, RouteArgs args) {
        String subRoute = routeInfo.getSubControllerRoute();

        if (subRoute.equals("/chat/:userId")) {
            User otherUser = editor.getUserById(args.getValue());
            if (!Objects.isNull(selectedOnlineUser) && selectedOnlineUser.equals(otherUser)) {
                return;
            }

            subviewCleanup();
            selectedOnlineUser = otherUser;

            PrivateChatController privateChatController = new PrivateChatController(view, editor, otherUser);
            privateChatController.init();
            Router.addToControllerCache(routeInfo.getFullRoute(), privateChatController);

            addUserToSidebar(otherUser);
        } else if (subRoute.equals("/online")) {
            subviewCleanup();

            selectedOnlineUser = null;

            UserList userList = new UserList();
            userListController = new UserListController(userList, editor);
            userListController.init();

            onlineUsersContainer.getChildren().add(userList);
            homeScreenLabel.setText(ViewLoader.loadLabel(Constants.LBL_ONLINE_USERS));

            Router.addToControllerCache(routeInfo.getFullRoute(), userListController);
        }
    }

    @Override
    public void stop() {
        subviewCleanup();
        userListController.stop();
        showOnlineUsersButton.setOnMouseClicked(null);
    }

    private void subviewCleanup() {
        if (onlineUsersContainer.getChildren().size() > 0) {
            onlineUsersContainer.getChildren().clear();
        }
    }

    private void handleShowOnlineUsersClicked(MouseEvent mouseEvent) {
        if (!Objects.isNull(selectedOnlineUser)) {
            // showOnlineUserList();
            Router.route("/main/home/online");
        }
    }

    private void addUserToSidebar(User otherUser) {
        String id = otherUser.getId();

        // Check if user is already in the list
        if (knownUsers.containsKey(id)) {
            return;
        }
        knownUsers.put(id, true);

        // Add to known users sidebar
        Text text = new Text(otherUser.getName());
        text.setFill(Color.WHITE);
        text.setFont(Font.font(16));
        text.setOnMouseClicked((mouseEvent) -> {
            Router.route("/main/home/chat/:userId", new RouteArgs().setKey(":userId").setValue(otherUser.getId()));
        });
        directMessageUsersList.getChildren().add(text);
    }
}
