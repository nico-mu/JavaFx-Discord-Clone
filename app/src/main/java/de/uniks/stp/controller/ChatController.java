package de.uniks.stp.controller;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.component.ChatMessage;
import de.uniks.stp.component.ChatMessageInput;
import de.uniks.stp.component.ListComponent;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.User;
import de.uniks.stp.network.rest.MediaRequestClient;
import de.uniks.stp.network.rest.ServerInformationHandler;
import de.uniks.stp.network.rest.SessionRestClient;
import de.uniks.stp.util.InviteInfo;
import javafx.event.ActionEvent;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract public class ChatController<T> extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    protected final Editor editor;
    protected final User currentUser;
    protected final ChatMessageInput chatMessageInput;
    protected final ListComponent<T, ChatMessage> chatMessageList;


    protected ServerInformationHandler serverInformationHandler;
    protected SessionRestClient restClient;
    protected ViewLoader viewLoader;
    protected MediaRequestClient mediaRequestClient;

    public ChatController(Editor editor,
                          ServerInformationHandler informationHandler,
                          SessionRestClient restClient,
                          MediaRequestClient mediaRequestClient,
                          ChatMessageInput chatMessageInput,
                          ViewLoader viewLoader) {
        this.editor = editor;
        this.viewLoader = viewLoader;
        this.serverInformationHandler = informationHandler;
        this.restClient = restClient;
        this.mediaRequestClient = mediaRequestClient;
        currentUser = editor.getOrCreateAccord().getCurrentUser();
        this.chatMessageInput = chatMessageInput;
        chatMessageList = new ListComponent<>(viewLoader, "message-list");
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
