package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.component.PrivateChatView;
import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.jpa.model.DirectMessageDTO;
import de.uniks.stp.model.DirectMessage;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.User;
import de.uniks.stp.network.WebSocketService;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_PRIVATE_CHAT)
public class PrivateChatController implements ControllerInterface {
    private static final String ONLINE_USERS_CONTAINER_ID = "#online-users-container";
    private static final String HOME_SCREEN_LABEL_ID = "#home-screen-label";

    private final Parent view;
    private final Editor editor;
    private final User model;
    private PrivateChatView chatView;
    private VBox onlineUsersContainer;
    private Label homeScreenLabel;

    private final PropertyChangeListener messagesChangeListener = this::handleNewPrivateMessage;

    public PrivateChatController(Parent view, Editor editor, User model) {
        this.view = view;
        this.editor = editor;
        this.model = model;
    }

    @Override
    public void init() {
        onlineUsersContainer = (VBox) view.lookup(ONLINE_USERS_CONTAINER_ID);
        homeScreenLabel = (Label) view.lookup(HOME_SCREEN_LABEL_ID);

        showChatView();
    }

    @Override
    public void stop() {
        if (Objects.nonNull(chatView)) {
            chatView.stop();
        }
        if (Objects.nonNull(model)) {
            model.listeners().removePropertyChangeListener(User.PROPERTY_PRIVATE_CHAT_MESSAGES, messagesChangeListener);
        }
    }

    /**
     * Creates PrivateChatView with name of .
     * Also adds all messages from model in the View and creates PropertyChangeListener that will do so in the future.
     */
    private void showChatView() {
        chatView = new PrivateChatView();

        // disable chat when user offline
        if (Objects.isNull(model)) {
            chatView.disable();
            homeScreenLabel.setText(ViewLoader.loadLabel(Constants.LBL_USER_OFFLINE));
            return;
        }

        homeScreenLabel.setText(model.getName());

        chatView.setOnMessageSubmit(this::handleMessageSubmit);
        onlineUsersContainer.getChildren().add(chatView);

        List<DirectMessageDTO> directMessages = DatabaseService.getDirectMessages(model);

        if (directMessages.size() > 0) {
            addUserToChatPartnerList();
        }

        // TODO: connect message loading to local model
        for (DirectMessageDTO directMessageDTO : directMessages) {
            chatView.appendMessage(new DirectMessage()
                .setReceiver(model)
                .setMessage(directMessageDTO.getMessage())
                .setId(directMessageDTO.getId().toString())
                .setTimestamp(directMessageDTO.getTimestamp().getTime())
                .setSender(editor.getOrCreateAccord().getCurrentUser())
            );
        };

        model.listeners().addPropertyChangeListener(User.PROPERTY_PRIVATE_CHAT_MESSAGES, messagesChangeListener);
    }

    /**
     * Creates DirectMessage, saves it in the model and sends it via websocket.
     * Adds other user to chatPartner list of currentUser if not already contained.
     * @param message
     */
    private void handleMessageSubmit(String message) {
        // create & save message
        DirectMessage msg = new DirectMessage();
        msg.setMessage(message).setSender(editor.getOrCreateAccord().getCurrentUser()).setTimestamp(new Date().getTime());
        model.withPrivateChatMessages(msg);

        // add user to chatPartner list if not already in it
        addUserToChatPartnerList();

        // send message to the server
        WebSocketService.sendPrivateMessage(model.getName(), message);
    }

    private void addUserToChatPartnerList() {
        User currentUser = editor.getOrCreateAccord().getCurrentUser();
        if (!currentUser.getChatPartner().contains(model)) {
            currentUser.withChatPartner(model);
        }
    }

    private void handleNewPrivateMessage(PropertyChangeEvent propertyChangeEvent) {
        DirectMessage directMessage = (DirectMessage) propertyChangeEvent.getNewValue();
        chatView.appendMessage(directMessage);
        DatabaseService.saveDirectMessage(directMessage);
    }
}
