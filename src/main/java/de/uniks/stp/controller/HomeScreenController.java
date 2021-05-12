package de.uniks.stp.controller;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.model.Accord;
import de.uniks.stp.model.User;
import de.uniks.stp.network.UserKeyProvider;
import de.uniks.stp.router.Route;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.component.UserList;
import de.uniks.stp.router.Router;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

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
    private boolean isShowingOnlineUserList;
    private final PropertyChangeListener chatPartnerChangeListener = this::onChatPartnerChanged;
    private ScrollPane directMessageUsersListScroll;

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
        directMessageUsersListScroll = (ScrollPane) homeScreenView.lookup(AVAILABLE_DM_USERS_ID);
        directMessageUsersList = (VBox) directMessageUsersListScroll.getContent();

        showOnlineUsersButton.setOnMouseClicked(this::handleShowOnlineUsersClicked);

        // init direct messages list
        // TODO: offlineUser Chat
        for (User user: editor.getOrCreateAccord().getCurrentUser().getChatPartner()){
            addUserToSidebar(user);
        }

        editor.getOrCreateAccord()
            .getCurrentUser()
            .listeners()
            .addPropertyChangeListener(User.PROPERTY_CHAT_PARTNER, chatPartnerChangeListener);
    }

    @Override
    public void route(RouteInfo routeInfo, RouteArgs args) {
        String subRoute = routeInfo.getSubControllerRoute();

        if (subRoute.equals(Constants.ROUTE_PRIVATE_CHAT)) {

            isShowingOnlineUserList = false;

            User otherUser = editor.getUserById(args.getValue());
            // Don't open private chat view which is already open
            if (!Objects.isNull(selectedOnlineUser) && selectedOnlineUser.equals(otherUser)) {
                return;
            }

            subviewCleanup();

            selectedOnlineUser = otherUser;

            PrivateChatController privateChatController = new PrivateChatController(view, editor, otherUser);
            privateChatController.init();
            Router.addToControllerCache(routeInfo.getFullRoute(), privateChatController);

            addUserToSidebar(otherUser);

        } else if (subRoute.equals(Constants.ROUTE_LIST_ONLINE_USERS)) {
            if (isShowingOnlineUserList) {
                return;
            }

            subviewCleanup();

            selectedOnlineUser = null;
            isShowingOnlineUserList = true;

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
        if (!Objects.isNull(userListController)) {
            userListController.stop();
        }
        showOnlineUsersButton.setOnMouseClicked(null);
    }

    private void subviewCleanup() {
        if (onlineUsersContainer.getChildren().size() > 0) {
            onlineUsersContainer.getChildren().clear();
        }
    }

    private void handleShowOnlineUsersClicked(MouseEvent mouseEvent) {
        if (!isShowingOnlineUserList) {
            Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_LIST_ONLINE_USERS);
        }
    }

    private void addUserToSidebar(User otherUser) {
        if (Objects.isNull(otherUser)) {
            return;
        }

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
        // TODO: Long user names break the view
        // text.setWrappingWidth(directMessageUsersList.getWidth() - 5);

        text.setOnMouseClicked((mouseEvent) -> {
            Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_PRIVATE_CHAT,
                new RouteArgs().setKey(Constants.ROUTE_PRIVATE_CHAT_ARGS).setValue(otherUser.getId()));
        });
        Platform.runLater(() -> {
            directMessageUsersList.getChildren().add(text);
        });
    }

    private void onChatPartnerChanged(PropertyChangeEvent propertyChangeEvent) {
        User user = (User) propertyChangeEvent.getNewValue();
        addUserToSidebar(user);
    }
}
