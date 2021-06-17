package de.uniks.stp.controller;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.component.ServerChatView;
import de.uniks.stp.component.TextWithEmoteSupport;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.ServerMessage;
import de.uniks.stp.model.User;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.WebSocketService;
import de.uniks.stp.router.Router;
import de.uniks.stp.util.MessageUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.Objects;

public class ServerChatController implements ControllerInterface {
    private static final Logger log = LoggerFactory.getLogger(ServerChatController.class);

    private static final String CHANNEL_NAME_LABEL_ID = "#channel-name-label";
    private static final String SERVER_CHAT_VBOX = "#server-chat-vbox";

    private final Parent view;
    private final Editor editor;
    private final Channel model;
    private TextWithEmoteSupport channelNameLabel;
    private VBox serverChatVBox;

    private ServerChatView chatView;
    private final PropertyChangeListener messagesChangeListener = this::handleNewMessage;
    private final PropertyChangeListener channelNameListener = this::onChannelNamePropertyChange;

    public ServerChatController(Parent view, Editor editor, Channel model) {
        this.view = view;
        this.editor = editor;
        this.model = model;
    }

    @Override
    public void init() {
        channelNameLabel = (TextWithEmoteSupport) view.lookup(CHANNEL_NAME_LABEL_ID);
        serverChatVBox = (VBox)view.lookup(SERVER_CHAT_VBOX);
        channelNameLabel.getRenderer().setSize(16).setScalingFactor(2);
        channelNameLabel.setText(model.getName());

        model.listeners().addPropertyChangeListener(Channel.PROPERTY_NAME, channelNameListener);

        showChatView();
    }

    @Override
    public void stop() {
        if (Objects.nonNull(chatView)) {
            chatView.stop();
            serverChatVBox.getChildren().clear();
        }
        if (Objects.nonNull(model)) {
            model.listeners().removePropertyChangeListener(Channel.PROPERTY_MESSAGES, messagesChangeListener);
            model.listeners().removePropertyChangeListener(Channel.PROPERTY_NAME, channelNameListener);
        }
    }

    /**
     * Creates ServerChatView with callback methods for sending messages and loading old messages.
     * Also adds all messages from model in the View and creates PropertyChangeListener that will do so in the future.
     */
    private void showChatView() {
        chatView = new ServerChatView(this::loadMessages, editor.getOrCreateAccord().getLanguage());

        chatView.setOnMessageSubmit(this::handleMessageSubmit);
        serverChatVBox.getChildren().add(chatView);

        if(model.getMessages().size() != 0) {
            for (ServerMessage message : model.getMessages()) {
                // check if message contains a server invite link
                Pair<String, String> ids = MessageUtil.getInviteIds(message.getMessage());
                if((ids != null) && (!editor.serverAdded(ids.getKey()))){
                    chatView.appendMessageWithButton(message, ids, this::joinServer);
                } else{
                    chatView.appendMessage(message);
                }
            }
        }
        if(model.getMessages().size() < 20){
            loadMessages(new ActionEvent());  //load old messages to fill initial view
        }
        model.listeners().addPropertyChangeListener(Channel.PROPERTY_MESSAGES, messagesChangeListener);
    }

    /**
     * Creates & fills new ChatView
     */
    private void reloadChatView() {
        // remove old ChatView
        serverChatVBox.getChildren().remove(chatView);
        chatView.stop();

        // init new ChatView
        chatView = new ServerChatView(this::loadMessages, editor.getOrCreateAccord().getLanguage());
        chatView.setOnMessageSubmit(this::handleMessageSubmit);
        serverChatVBox.getChildren().add(chatView);

        if(model.getMessages().size() != 0) {
            for (ServerMessage message : model.getMessages()) {
                // check if message contains a server invite link
                Pair<String, String> ids = MessageUtil.getInviteIds(message.getMessage());
                if((ids != null) && (!editor.serverAdded(ids.getKey()))){
                    chatView.appendMessageWithButton(message, ids, this::joinServer);
                } else{
                    chatView.appendMessage(message);
                }
            }
        }
        if(model.getMessages().size() < 20){
            chatView.removeLoadMessagesButton();
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
        Channel source = (Channel) propertyChangeEvent.getSource();

        //check if message contains a server invite link
        Pair<String, String> ids = MessageUtil.getInviteIds(msg.getMessage());

        if(source.getMessages().last().equals(msg)) {
            // append message
            if((ids != null) && (!editor.serverAdded(ids.getKey()))){
                chatView.appendMessageWithButton(msg, ids, this::joinServer);
            } else{
                chatView.appendMessage(msg);
            }
        }
        else {
            // prepend message
            int insertPos = source.getMessages().headSet(msg).size();
            if((ids != null) && (!editor.serverAdded(ids.getKey()))){
                chatView.insertMessageWithButton(insertPos, msg, ids, this::joinServer);
            } else{
                chatView.insertMessage(insertPos, msg);
            }
        }
    }

    private void joinServer(ActionEvent actionEvent) {
        JFXButton button = (JFXButton) actionEvent.getSource();
        String ids = button.getId();
        String serverId = ids.split("-")[0];
        String inviteId = ids.split("-")[1];

        User currentUser = editor.getOrCreateAccord().getCurrentUser();
        NetworkClientInjector.getRestClient().joinServer(serverId, inviteId, currentUser.getName(), currentUser.getPassword(),
            (response) -> onJoinServerResponse(serverId, response));
    }

    private void onJoinServerResponse(String serverId, HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toPrettyString());

        if(response.isSuccess()){
            NetworkClientInjector.getRestClient().getServerInformation(serverId, this::onServerInformationResponse);
        } else{
            log.error("Join Server failed because: " + response.getBody().getObject().getString("message"));
        }
    }

    private void onServerInformationResponse(HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toString());
        if(response.isSuccess()){
            JSONObject resJson = response.getBody().getObject().getJSONObject("data");
            final String serverId = resJson.getString("id");
            final String serverName = resJson.getString("name");
            final String serverOwner = resJson.getString("owner");

            // add server to model -> to NavBar List
            if (serverOwner.equals(editor.getOrCreateAccord().getCurrentUser().getId())) {
                editor.getOrCreateServer(serverId, serverName).setOwner(editor.getOrCreateAccord().getCurrentUser());
            } else {
                editor.getOrCreateServer(serverId, serverName);
            }

            // reload chatView -> some button might not be needed anymore
            Platform.runLater(this::reloadChatView);
        } else{
            log.error("Error trying to load information of new server");
        }
    }

    private void onChannelNamePropertyChange(PropertyChangeEvent propertyChangeEvent) {
        Platform.runLater(()-> {
            channelNameLabel.setText(model.getName());
        });
    }

    /**
     * Sends request to load older Messages in this channel.
     * @param actionEvent
     */
    private void loadMessages(ActionEvent actionEvent) {
        // timestamp = min timestamp of messages in model. If no messages in model, timestamp = now
        long timestamp = new Date().getTime();
        if(model.getMessages().size() > 0){
            timestamp = model.getMessages().first().getTimestamp();
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

            if (messagesJson.length() < 50) {
                // when there are no older messages to show
                chatView.removeLoadMessagesButton();  //alternative: show note or disable button
                if(messagesJson.length() == 0) {
                    return;
                }
            }

            // create messages
            for (Object msgObject : messagesJson) {
                JSONObject msgJson = (JSONObject) msgObject;
                final String msgId = msgJson.getString("id");
                final String channelId = msgJson.getString("channel");
                final long timestamp = msgJson.getLong("timestamp");
                final String senderName = msgJson.getString("from");
                final String msgText = msgJson.getString("text");

                if(!channelId.equals(model.getId())){
                    log.error("Received old server messages of wrong channel!");
                    return;
                }
                User sender = editor.getOrCreateServerMember(senderName, model.getCategory().getServer());
                if (Objects.isNull(sender.getId())){
                    log.debug("Loaded old server message from former serveruser, created dummy object");
                }

                ServerMessage msg = new ServerMessage();
                msg.setMessage(msgText).setTimestamp(timestamp).setId(msgId).setSender(sender);
                msg.setChannel(model);  //message will be added to view by PropertyChangeListener
            }
        } else {
            log.error("receiving old messages failed!");
        }
    }
}
