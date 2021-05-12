package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.component.ChatView;
import de.uniks.stp.model.DirectMessage;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.User;
import de.uniks.stp.network.WebSocketService;
import de.uniks.stp.router.Route;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.util.Date;
import java.util.Objects;

@Route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_PRIVATE_CHAT)
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

        // Load messages from model
        try {
            for (Message message : user.getPrivateChatMessages()) {
                chatView.appendMessage(message);
            }

            user.listeners().addPropertyChangeListener(User.PROPERTY_PRIVATE_CHAT_MESSAGES, (propertyChangeEvent) -> {
                DirectMessage directMessage = (DirectMessage) propertyChangeEvent.getNewValue();

                chatView.appendMessage(directMessage);
            });
        } catch (NullPointerException e) {
            System.out.println("User already logged out");
        }
    }

    @Override
    public void stop() {
        if (!Objects.isNull(chatView)) {
            chatView.stop();
        }
    }

    @Override
    public void route(RouteInfo routeInfo, RouteArgs args) {

    }

    private void showPrivateChatView(User otherUser) {
        // Block chat for now when user offline
        if (Objects.isNull(otherUser)) {
            homeScreenLabel.setText(ViewLoader.loadLabel(Constants.LBL_USER_OFFLINE));
            return;
        }

        homeScreenLabel.setText(otherUser.getName());
        chatView = new ChatView(view);

        chatView.onMessageSubmit((message) -> {
            // create & save message
            DirectMessage msg = new DirectMessage();
            msg.setMessage(message).setSender(editor.getOrCreateAccord().getCurrentUser()).setTimestamp(new Date().getTime());
            user.withPrivateChatMessages(msg);

            // add user to chatPartner list if not already in it
            User currentUser = editor.getOrCreateAccord().getCurrentUser();
            if(!currentUser.getChatPartner().contains(user)){
                currentUser.withChatPartner(user);
            }

            // send message to the server
            WebSocketService.sendPrivateMessage(otherUser.getName(),message);
        });
        onlineUsersContainer.getChildren().add(chatView);
    }
}
