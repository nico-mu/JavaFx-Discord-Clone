package de.uniks.stp.controller;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.component.ChatView;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.User;
import de.uniks.stp.router.Route;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.component.UserList;
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
    
    private final EventHandler<MouseEvent> handleShowOnlineUsersClicked = this::handleShowOnlineUsersClicked;

    private final AnchorPane view;
    private final Editor editor;
    private ChatView chatView;
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

        showOnlineUsersButton.setOnMouseClicked(handleShowOnlineUsersClicked);

        showOnlineUserList();
    }

    private void handleShowOnlineUsersClicked(MouseEvent mouseEvent) {
        if (!Objects.isNull(selectedOnlineUser)) {
            showOnlineUserList();
        }
    }

    private void subviewCleanup() {
        if (onlineUsersContainer.getChildren().size() > 0) {
            onlineUsersContainer.getChildren().clear();
        }
    }

    private void showOnlineUserList() {
        subviewCleanup();


        /* if (availableDmUsers.getChildren().size() > 0) {
            availableDmUsers.getChildren().forEach(node -> {
                Text textNode = (Text) node;
                if (textNode.getText().equals(selectedOnlineUser.getName())) {
                    textNode.setUnderline(false);
                    textNode.setFill(Color.WHITE);
                };
            });
        } */

        selectedOnlineUser = null;


        UserList userList = new UserList();
        userListController = new UserListController(userList, editor);
        // TODO: The order matters here, init has to be called after onUserSelected. Change so that order doesn't matter anymore.
        userListController.onUserSelected(id -> {
            User user = editor.getUserById(id);
            showPrivateChatView(user);

            // Check if user is already in the list
            if (knownUsers.containsKey(id)) {
                return;
            }
            knownUsers.put(id, true);

            // Add to known users sidebar
            Text text = new Text(user.getName());
            text.setFill(Color.WHITE);
            text.setFont(Font.font(16));
            text.setOnMouseClicked((mouseEvent) -> {
                showPrivateChatView(user);
            });
            directMessageUsersList.getChildren().add(text);
        });

        userListController.init();

        onlineUsersContainer.getChildren().add(userList);
        homeScreenLabel.setText(ViewLoader.loadLabel(Constants.LBL_ONLINE_USERS));
    }

    private void showPrivateChatView(User otherUser) {
        if (!Objects.isNull(selectedOnlineUser) && selectedOnlineUser.equals(otherUser)) {
            return;
        }

        subviewCleanup();

        selectedOnlineUser = otherUser;

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

        /* availableDmUsers.getChildren().forEach(node -> {
            Text textNode = (Text) node;
            if (textNode.getText().equals(otherUser.getName())) {
                textNode.setUnderline(true);
                textNode.setFill(Color.BLUE);
            };
        }); */
    }

    @Override
    public void route(RouteInfo routeInfo, RouteArgs args) {
        //no subroutes
    }

    @Override
    public void stop() {
        userListController.stop();
        showOnlineUsersButton.setOnMouseClicked(handleShowOnlineUsersClicked);
        chatView = null;
    }
}
