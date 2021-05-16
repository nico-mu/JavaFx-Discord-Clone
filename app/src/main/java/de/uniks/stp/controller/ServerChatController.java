package de.uniks.stp.controller;

import de.uniks.stp.Editor;
import de.uniks.stp.component.ChatView;
import de.uniks.stp.component.ServerChatMessage;
import de.uniks.stp.model.*;
import de.uniks.stp.network.WebSocketService;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.Objects;

public class ServerChatController implements ControllerInterface {

    private static final String CHANNEL_NAME_LABEL_ID = "#channel-name-label";
    private static final String SERVER_CHAT_VBOX = "#server-chat-vbox";

    private final Parent view;
    private final Editor editor;
    private final Channel model;
    private Label channelNameLabel;
    private VBox serverChatVBox;

    private ChatView chatView;
    private PropertyChangeListener messagesChangeListener = this::handleNewMessage;

    public ServerChatController(Parent view, Editor editor, Channel model) {
        this.view = view;
        this.editor = editor;
        this.model = model;
    }

    @Override
    public void init() {
        channelNameLabel = (Label)view.lookup(CHANNEL_NAME_LABEL_ID);
        serverChatVBox = (VBox)view.lookup(SERVER_CHAT_VBOX);

        channelNameLabel.setText(model.getName());

        showChatView();

        for (Message message : model.getMessages()) {
            chatView.appendMessage(message);
        }

        model.listeners().addPropertyChangeListener(Channel.PROPERTY_MESSAGES, messagesChangeListener);
    }

    @Override
    public void stop() {
        if (Objects.nonNull(chatView)) {
            chatView.stop();
        }
        if (Objects.nonNull(model)) {
            model.listeners().removePropertyChangeListener(Channel.PROPERTY_MESSAGES, messagesChangeListener);
        }
    }

    @Override
    public void route(RouteInfo routeInfo, RouteArgs args) {

    }

    private void showChatView() {
        chatView = new ChatView(editor, false);

        chatView.onMessageSubmit(this::handleMessageSubmit);
        serverChatVBox.getChildren().add(chatView);
    }

    private void handleMessageSubmit(String message) {
        // create & save message
        ServerMessage msg = new ServerMessage();
        msg.setMessage(message).setSender(editor.getOrCreateAccord().getCurrentUser()).setTimestamp(new Date().getTime());
        msg.setChannel(model);  // triggers PropertyChangeListener that shows Message in Chat

        // send message
        WebSocketService.sendServerMessage(model.getCategory().getServer().getId(), model.getId(), message);
    }

    private void handleNewMessage(PropertyChangeEvent propertyChangeEvent) {
        ServerMessage msg = (ServerMessage) propertyChangeEvent.getNewValue();
        chatView.appendMessage(msg);
    }
}
