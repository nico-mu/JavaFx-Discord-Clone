package de.uniks.stp.controller;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.model.User;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import de.uniks.stp.router.Router;
import de.uniks.stp.view.Views;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@Route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME)
public class HomeScreenController implements ControllerInterface {
    private static final Logger log = LoggerFactory.getLogger(HomeScreenController.class);
    private static final String ONLINE_USERS_CONTAINER_ID = "#online-users-container";
    private static final String DIRECT_MESSAGE_CONTAINER_ID = "#direct-messages-container";
    private static final String TOGGLE_ONLINE_BUTTON_ID = "#toggle-online-button";
    private static final String HOME_SCREEN_LABEL_ID = "#home-screen-label";

    private final AnchorPane view;
    private final Editor editor;
    private VBox onlineUsersContainer;
    private VBox directMessagesContainer;
    private JFXButton showOnlineUsersButton;
    private Label homeScreenLabel;
    private UserListController userListController;
    private DirectMessageListController directMessageListController;
    private PrivateChatController privateChatController;

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
        directMessagesContainer = (VBox) homeScreenView.lookup(DIRECT_MESSAGE_CONTAINER_ID);
        homeScreenLabel = (Label) homeScreenView.lookup(HOME_SCREEN_LABEL_ID);

        showOnlineUsersButton.setOnMouseClicked(this::handleShowOnlineUsersClicked);
        directMessageListController = new DirectMessageListController(directMessagesContainer, editor);
        directMessageListController.init();
    }

    @Override
    public void route(RouteInfo routeInfo, RouteArgs args) {
        String subRoute = routeInfo.getSubControllerRoute();
        subviewCleanup();
        if (subRoute.equals(Constants.ROUTE_PRIVATE_CHAT)) {
            String userId = args.getArguments().get(Constants.ROUTE_PRIVATE_CHAT_ARGS);
            User user = editor.getChatPartnerOfCurrentUserById(userId);

            if(Objects.isNull(user)) {
                user = editor.getOtherUserById(userId);
                if(Objects.nonNull(user)) {
                    editor.getOrCreateChatPartnerOfCurrentUser(user.getId(), user.getName());
                }
                else {
                    log.error("No user can be selected.");
                    return;
                }
            }
            privateChatController = new PrivateChatController(view, editor, userId, user.getName());
            privateChatController.init();
            Router.addToControllerCache(routeInfo.getFullRoute(), privateChatController);

        } else if (subRoute.equals(Constants.ROUTE_LIST_ONLINE_USERS)) {
            userListController = new UserListController(onlineUsersContainer, editor);
            userListController.init();
            Router.addToControllerCache(routeInfo.getFullRoute(), userListController);
            homeScreenLabel.setText(ViewLoader.loadLabel(Constants.LBL_ONLINE_USERS));
        }
    }

    @Override
    public void stop() {
        subviewCleanup();
        if (Objects.nonNull(userListController)) {
            userListController.stop();
        }
        if (Objects.nonNull(directMessageListController)) {
            directMessageListController.stop();
        }
        if (Objects.nonNull(privateChatController)) {
            privateChatController.stop();
        }
        directMessagesContainer.getChildren().clear();
        showOnlineUsersButton.setOnMouseClicked(null);

    }

    private void subviewCleanup() {
        onlineUsersContainer.getChildren().clear();
    }

    private void handleShowOnlineUsersClicked(MouseEvent mouseEvent) {
        Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_LIST_ONLINE_USERS);
    }
}
