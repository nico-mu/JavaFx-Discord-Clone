package de.uniks.stp.controller;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.Editor;
import de.uniks.stp.component.ChatMessage;
import de.uniks.stp.component.ChatMessageInput;
import de.uniks.stp.component.ListComponent;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.User;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.network.ServerInformationHandler;
import de.uniks.stp.util.InviteInfo;
import javafx.event.ActionEvent;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract public class ChatController<T> {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    protected final Editor editor;
    protected final RestClient restClient;
    protected final User currentUser;
    protected final ServerInformationHandler serverInformationHandler;
    protected final ChatMessageInput chatMessageInput;

    protected final ListComponent<T, ChatMessage> chatMessageList;

    public ChatController(Editor editor) {
        this.editor = editor;
        restClient = NetworkClientInjector.getRestClient();
        currentUser = editor.getOrCreateAccord().getCurrentUser();
        serverInformationHandler = new ServerInformationHandler(editor);
        chatMessageInput = new ChatMessageInput();
        chatMessageList = new ListComponent<>("message-list");
        chatMessageList.setIsScrollAware(true);
        chatMessageList.getStyleClass().add("message-list");
        chatMessageList.setPrefHeight(100);
    }

    abstract protected void loadMessages();

    abstract protected ChatMessage parseMessage(Message message);

    protected void joinServer(ActionEvent actionEvent) {
        JFXButton button = (JFXButton) actionEvent.getSource();
        InviteInfo info = (InviteInfo) button.getUserData();
        String serverId = info.getServerId();
        String inviteId = info.getInviteId();

        restClient.joinServer(serverId, inviteId, currentUser.getName(), currentUser.getPassword(),
            (response) -> onJoinServerResponse(serverId, response));
    }

    protected void onJoinServerResponse(String serverId, HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toPrettyString());

        if (response.isSuccess()) {
            editor.getOrCreateServer(serverId);
            restClient.getServerInformation(serverId, serverInformationHandler::handleServerInformationRequest);
            restClient.getCategories(serverId, (msg) -> serverInformationHandler.handleCategories(msg, editor.getServer(serverId)));
        } else {
            log.error("Join Server failed because: " + response.getBody().getObject().getString("message"));
        }
    }
}
