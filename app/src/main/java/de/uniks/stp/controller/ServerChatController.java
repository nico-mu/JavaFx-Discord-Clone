package de.uniks.stp.controller;

import de.uniks.stp.Editor;
import de.uniks.stp.component.ServerChatView;
import de.uniks.stp.model.*;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.WebSocketService;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

public class ServerChatController implements ControllerInterface {
    private static final Logger log = LoggerFactory.getLogger(ServerChatController.class);

    private static final String CHANNEL_NAME_LABEL_ID = "#channel-name-label";
    private static final String SERVER_CHAT_VBOX = "#server-chat-vbox";

    private final Parent view;
    private final Editor editor;
    private final Channel model;
    private Label channelNameLabel;
    private VBox serverChatVBox;

    private ServerChatView chatView;
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

    /**
     * Creates ServerChatView with callback methods for sending messages and loading old messages.
     * Also adds all messages from model in the View and creates PropertyChangeListener that will do so in the future.
     */
    private void showChatView() {
        chatView = new ServerChatView(this::loadMessages);

        chatView.setOnMessageSubmit(this::handleMessageSubmit);
        serverChatVBox.getChildren().add(chatView);

        for (ServerMessage message : model.getMessages()) {
            chatView.appendMessage(message);
        }
        model.listeners().addPropertyChangeListener(Channel.PROPERTY_MESSAGES, messagesChangeListener);
    }

    /**
     * Creates ServerMessage, saves it in the model and sends it via websocket.
     * @param message
     */
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

    /**
     * Sends request to load older Messages in this channel.
     * @param actionEvent
     */
    private void loadMessages(ActionEvent actionEvent) {
        // timestamp = min timestamp of messages in model. If no messages in model, timestamp = now
        long timestamp = new Date().getTime();
        if(model.getMessages().size() > 0){
            Comparator<Message> messageComparator = Comparator.comparingLong(Message::getTimestamp);
            timestamp = Collections.min(model.getMessages(), messageComparator).getTimestamp();
        }

        NetworkClientInjector.getRestClient().getServerChannelMessages(model.getCategory().getServer().getId(),
            model.getCategory().getId(), model.getId(), timestamp, this::onLoadMessagesResponse);
    }

    /**
     * Handles response containing older ServerChatMessages.
     * Removes loadMessagesButon if there were no older messages.
     * @param response
     */
    private void onLoadMessagesResponse(HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toPrettyString());

        if (response.isSuccess()) {
            JSONArray messagesJson = response.getBody().getObject().getJSONArray("data");

            if(messagesJson.length() == 0){
                //when there are no older messages to show
                chatView.removeLoadMessagesButton();  //alternative: show note or disable button
                return;
            }

            //TODO: create ServerMessages, save in model and show correctly in chat

        } else {
            log.error("receiving old messages failed!");
        }
    }
}
