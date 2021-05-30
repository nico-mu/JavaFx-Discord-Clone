package de.uniks.stp.controller;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.component.UserList;
import de.uniks.stp.component.UserListSidebarEntry;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.notification.NotificationService;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import de.uniks.stp.router.Router;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
    private final Map<String, Boolean> knownUsers = new ConcurrentHashMap<>();
    private VBox onlineUsersContainer;
    private JFXButton showOnlineUsersButton;
    private Label homeScreenLabel;
    private VBox directMessageUsersList;
    private final PropertyChangeListener chatPartnerChangeListener = this::onChatPartnerChanged;
    private UserListController userListController;

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
        ScrollPane directMessageUsersListScroll = (ScrollPane) homeScreenView.lookup(AVAILABLE_DM_USERS_ID);
        directMessageUsersList = (VBox) directMessageUsersListScroll.getContent();

        showOnlineUsersButton.setOnMouseClicked(this::handleShowOnlineUsersClicked);

        // init direct messages list
        // TODO: offlineUser Chat
        for (User user : editor.getOrCreateAccord().getCurrentUser().getChatPartner()) {
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
        subviewCleanup();
        NotificationService.setActiveObject(null);
        if (subRoute.equals(Constants.ROUTE_PRIVATE_CHAT)) {
            User otherUser = editor.getUserById(args.getArguments().get(Constants.ROUTE_PRIVATE_CHAT_ARGS));
            NotificationService.setActiveObject(otherUser);
            PrivateChatController privateChatController = new PrivateChatController(view, editor, otherUser);
            privateChatController.init();
            Router.addToControllerCache(routeInfo.getFullRoute(), privateChatController);

            addUserToSidebar(otherUser);

        } else if (subRoute.equals(Constants.ROUTE_LIST_ONLINE_USERS)) {
            UserList userList = new UserList();
            userListController = new UserListController(userList, editor);
            userListController.init();
            Router.addToControllerCache(routeInfo.getFullRoute(), userListController);
            onlineUsersContainer.getChildren().add(userList);

            homeScreenLabel.setText(ViewLoader.loadLabel(Constants.LBL_ONLINE_USERS));
        }
    }

    @Override
    public void stop() {
        subviewCleanup();
        if (Objects.nonNull(userListController)) {
            userListController.stop();
        }
        showOnlineUsersButton.setOnMouseClicked(null);
        editor.getOrCreateAccord()
            .getCurrentUser()
            .listeners()
            .removePropertyChangeListener(User.PROPERTY_CHAT_PARTNER, chatPartnerChangeListener);
    }

    private void subviewCleanup() {
        onlineUsersContainer.getChildren().clear();
    }

    private void handleShowOnlineUsersClicked(MouseEvent mouseEvent) {
        Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_LIST_ONLINE_USERS);
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
        UserListSidebarEntry userListEntry = new UserListSidebarEntry(otherUser);

        Platform.runLater(() -> {
            directMessageUsersList.getChildren().add(userListEntry);
        });
    }

    private void onChatPartnerChanged(PropertyChangeEvent propertyChangeEvent) {
        User user = (User) propertyChangeEvent.getNewValue();
        addUserToSidebar(user);
    }
}
