package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.component.ChatMessage;
import de.uniks.stp.component.TextWithEmoteSupport;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.ServerMessage;
import de.uniks.stp.model.User;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.WebSocketService;
import de.uniks.stp.util.InviteInfo;
import de.uniks.stp.util.MessageUtil;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

@Route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER + Constants.ROUTE_CHANNEL)
public class ServerChatController extends ChatController<ServerMessage> implements ControllerInterface {
    private static final Logger log = LoggerFactory.getLogger(ServerChatController.class);

    private static final String SERVER_CHAT_VBOX = "#server-chat-vbox";
    private static final String LOAD_OLD_MESSAGES_BOX = "#load-old-messages-hbox";

    private final Channel model;
    private VBox serverChatVBox;
    private HBox loadOldMessagesBox;
    private final ChangeListener<Number> scrollValueChangedListener = this::onScrollValueChanged;
    private boolean canLoadOldMessages;

    private final PropertyChangeListener messagesChangeListener = this::handleNewMessage;

    public ServerChatController(Parent view, Editor editor, Channel model) {
        super(view, editor);
        this.model = model;
        canLoadOldMessages = true;
        chatMessageList.vvalueProperty().addListener(scrollValueChangedListener);
    }

    @Override
    public void init() {
        serverChatVBox = (VBox)view.lookup(SERVER_CHAT_VBOX);

        loadOldMessagesBox = (HBox) view.lookup(LOAD_OLD_MESSAGES_BOX);

        //add chatMessageList
        serverChatVBox.getChildren().add(chatMessageList);
        //add chatMessageInput
        serverChatVBox.getChildren().add(chatMessageInput);

        loadMessages();
        model.listeners().addPropertyChangeListener(Channel.PROPERTY_MESSAGES, messagesChangeListener);

        chatMessageInput.setOnMessageSubmit(this::handleMessageSubmit);
    }

    @Override
    public void stop() {
        serverChatVBox.getChildren().clear();
        if (Objects.nonNull(model)) {
            model.listeners().removePropertyChangeListener(Channel.PROPERTY_MESSAGES, messagesChangeListener);
        }
    }

    @Override
    protected void loadMessages() {
        if(model.getMessages().size() != 0) {
            for (ServerMessage message : model.getMessages()) {
                chatMessageList.addElement(message, parseMessage(message));
            }
        }
        if(model.getMessages().size() < 20){
            loadOldMessages();  //load old messages to fill initial view
        }
    }

    @Override
    protected ChatMessage parseMessage(Message message) {
        ChatMessage messageNode = new ChatMessage(message, editor.getOrCreateAccord().getLanguage());

        if(!message.getSender().getName().equals(currentUser.getName())) {
            InviteInfo info = MessageUtil.getInviteInfo(message.getMessage());
            // check if message contains a server invite link
            if(Objects.nonNull(info) && Objects.isNull(editor.getServer(info.getServerId()))){
                messageNode.addJoinButtonButton(info, this::joinServer);
            }
        }
        return messageNode;
    }

    private void onScrollValueChanged(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
        if(newValue.doubleValue() == 0.0d) {
            loadOldMessages();
        }
    }

    /**
     * Creates ServerMessage, saves it in the model and sends it via websocket.
     * @param message
     */
    private void handleMessageSubmit(String message) {
        // send message
        WebSocketService.sendServerMessage(model.getCategory().getServer().getId(), model.getId(), message);
    }

    private void handleNewMessage(PropertyChangeEvent propertyChangeEvent) {
        ServerMessage msg = (ServerMessage) propertyChangeEvent.getNewValue();
        Channel source = (Channel) propertyChangeEvent.getSource();
        ChatMessage newChatMessageNode = parseMessage(msg);

        if(source.getMessages().last().equals(msg)) {
            // append message
            Platform.runLater(() -> {
                chatMessageList.addElement(msg, newChatMessageNode);
            });

        }
        else {
            // prepend message
            int insertPos = source.getMessages().headSet(msg).size();
            Platform.runLater(() -> {
                chatMessageList.insertElement(insertPos, msg, newChatMessageNode);
            });
        }
    }

    /**
     * Sends request to load older Messages in this channel.
     */
    private void loadOldMessages() {
        if(canLoadOldMessages) {
            // timestamp = min timestamp of messages in model. If no messages in model, timestamp = now
            long timestamp = new Date().getTime();
            if(model.getMessages().size() > 0){
                timestamp = model.getMessages().first().getTimestamp();
            }

            loadOldMessagesBox.setVisible(true);
            NetworkClientInjector.getRestClient().getServerChannelMessages(model.getCategory().getServer().getId(),
                model.getCategory().getId(), model.getId(), timestamp, this::onLoadMessagesResponse);
        }
    }

    /**
     * Handles response containing older ServerChatMessages.
     * Removes loadMessagesButon if there were no older messages.
     * @param response
     */
    private void onLoadMessagesResponse(HttpResponse<JsonNode> response) {
        loadOldMessagesBox.setVisible(false);
        log.debug(response.getBody().toPrettyString());

        if (response.isSuccess()) {
            JSONArray messagesJson = response.getBody().getObject().getJSONArray("data");

            if (messagesJson.length() < 50) {
                // when there are no older messages to show
                canLoadOldMessages = false;
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

                ServerMessage msg = editor.getOrCreateServerMessage(msgId, model);
                msg.setMessage(msgText).setTimestamp(timestamp).setId(msgId).setSender(sender);
                msg.setChannel(model);  //message will be added to view by PropertyChangeListener
            }
        } else {
            log.error("receiving old messages failed!");
        }
    }
}
