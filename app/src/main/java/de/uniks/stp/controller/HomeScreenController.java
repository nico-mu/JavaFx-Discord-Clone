package de.uniks.stp.controller;

import com.jfoenix.controls.JFXButton;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.model.User;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import de.uniks.stp.router.Router;
import de.uniks.stp.view.Views;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Objects;

@Route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME)
public class HomeScreenController implements ControllerInterface {
    private static final Logger log = LoggerFactory.getLogger(HomeScreenController.class);
    private static final String ONLINE_USERS_CONTAINER_ID = "#online-users-container";
    private static final String DIRECT_MESSAGE_CONTAINER_ID = "#direct-messages-container";
    private static final String TOGGLE_ONLINE_BUTTON_ID = "#toggle-online-button";
    private static final String HOME_SCREEN_LABEL_ID = "#home-screen-label";

    private final VBox view;
    private final Editor editor;
    private final Router router;
    private final ViewLoader viewLoader;

    private VBox onlineUsersContainer;
    private VBox directMessagesContainer;
    private JFXButton showOnlineUsersButton;
    private Label homeScreenLabel;
    private HBox homeScreenView;

    private UserListController userListController;
    private DirectMessageListController directMessageListController;
    private PrivateChatController privateChatController;

    @Inject
    DirectMessageListController.DirectMessageListControllerFactory directMessageListControllerFactory;

    @Inject
    PrivateChatController.PrivateChatControllerFactory privateChatControllerFactory;

    @Inject
    UserListController.UserListControllerFactory userListControllerFactory;
    private final ChangeListener<Number> viewHeightChangedListener = this::onViewHeightChangeListener;


    @AssistedInject
    HomeScreenController(Editor editor, Router router, ViewLoader viewLoader, @Assisted Parent view) {
        this.view = (VBox) view;
        this.editor = editor;
        this.router = router;
        this.viewLoader = viewLoader;
    }

    @Override
    public void init() {
        homeScreenView = (HBox) viewLoader.loadView(Views.HOME_SCREEN);
        view.getChildren().add(homeScreenView);

        showOnlineUsersButton = (JFXButton) homeScreenView.lookup(TOGGLE_ONLINE_BUTTON_ID);
        onlineUsersContainer = (VBox) homeScreenView.lookup(ONLINE_USERS_CONTAINER_ID);
        directMessagesContainer = (VBox) homeScreenView.lookup(DIRECT_MESSAGE_CONTAINER_ID);
        homeScreenLabel = (Label) homeScreenView.lookup(HOME_SCREEN_LABEL_ID);

        showOnlineUsersButton.setOnMouseClicked(this::handleShowOnlineUsersClicked);
        directMessageListController =  directMessageListControllerFactory.create(directMessagesContainer);
        directMessageListController.init();
        homeScreenView.setPrefHeight(view.getHeight());
        view.heightProperty().addListener(viewHeightChangedListener);
    }

    @Override
    public ControllerInterface route(RouteInfo routeInfo, RouteArgs args) {
        String subRoute = routeInfo.getSubControllerRoute();
        subviewCleanup();
        if (subRoute.equals(Constants.ROUTE_PRIVATE_CHAT)) {
            String userId = args.getArguments().get(Constants.ROUTE_PRIVATE_CHAT_ARGS);
            User user = editor.getChatPartnerOfCurrentUserById(userId);

            if (Objects.isNull(user)) {
                User otherUser = editor.getOtherUserById(userId);
                if (Objects.nonNull(otherUser)) {
                    user = editor.getOrCreateChatPartnerOfCurrentUser(otherUser.getId(), otherUser.getName());
                } else {
                    log.error("No user can be selected.");
                    return null;
                }
            }
            directMessageListController.addUserToSidebar(user);
            privateChatController = privateChatControllerFactory.create(view, user);
            privateChatController.init();
            return privateChatController;

        } else if (subRoute.equals(Constants.ROUTE_LIST_ONLINE_USERS)) {
            userListController = userListControllerFactory.create(onlineUsersContainer);
            userListController.init();
            homeScreenLabel.setText(viewLoader.loadLabel(Constants.LBL_ONLINE_USERS));
            return userListController;
        }
        return null;
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
        homeScreenView.heightProperty().removeListener(viewHeightChangedListener);
    }

    private void subviewCleanup() {
        onlineUsersContainer.getChildren().clear();
    }

    private void handleShowOnlineUsersClicked(MouseEvent mouseEvent) {
        router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_LIST_ONLINE_USERS);
    }

    private void onViewHeightChangeListener(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
        homeScreenView.setPrefHeight(newValue.doubleValue());
    }

    @AssistedFactory
    public interface HomeScreenControllerFactory {
        HomeScreenController create(Parent view);
    }
}
