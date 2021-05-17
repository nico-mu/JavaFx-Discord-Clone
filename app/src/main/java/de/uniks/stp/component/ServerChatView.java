package de.uniks.stp.component;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.controller.ServerChatController;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.ServerMessage;
import de.uniks.stp.model.User;
import de.uniks.stp.network.RestClient;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class ServerChatView extends VBox {
    private static final Logger log = LoggerFactory.getLogger(ServerChatView.class);

    private final Editor editor;
    private final Channel model;

    private final RestClient restClient;
    private ArrayList<ServerMessage> shownMessages;
    private boolean loading = false;

    @FXML
    private VBox chatViewContainer;
    @FXML
    private JFXButton chatViewSubmitButton;
    @FXML
    private JFXTextArea chatViewMessageInput;
    @FXML
    private ScrollPane chatViewMessageScrollPane;
    @FXML
    private JFXButton loadMessagesButton;
    @FXML
    private VBox messageList;

    private final LinkedList<Consumer<String>> submitListener = new LinkedList<>();
    private final InvalidationListener heightChangedListener = this::onHeightChanged;

    public ServerChatView(Editor editor, Channel model) {
        this.editor = editor;
        this.model = model;

        restClient = new RestClient();
        shownMessages = new ArrayList<ServerMessage>();

        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.SERVER_CHAT_VIEW);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        chatViewSubmitButton.setOnMouseClicked(this::onSubmitClicked);

        messageList.heightProperty().addListener(heightChangedListener);

        chatViewMessageInput.setOnKeyPressed(this::checkForEnter);

        loadMessagesButton.setOnAction(this::loadMessages);
    }

    private void loadMessages(ActionEvent actionEvent) {
        long timestamp = new Date().getTime();
        if(model.getMessages().size() > 0){
            timestamp = model.getMessages().get(0).getTimestamp();
        }
        restClient.getServerMessages(model.getCategory().getServer().getId(),model.getCategory().getId(),
            model.getId(), timestamp, this::onLoadMessagesResponse);
    }

    private void onLoadMessagesResponse(HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toPrettyString());

        if (response.isSuccess()) {
            // TODO
        } else {
            log.error("receiving old messages failed!");
        }
    }

    private void onHeightChanged(Observable observable) {
        chatViewMessageScrollPane.setVvalue(1.0);
    }

    /**
     * Enter typed -> press send Button | Shift-Enter typed -> add new line
     * @param keyEvent
     */
    private void checkForEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER)  {
            if (keyEvent.isShiftDown()) {
                chatViewMessageInput.appendText(System.getProperty("line.separator"));
            } else {
                String text = chatViewMessageInput.getText();
                chatViewMessageInput.setText(text.substring(0, text.length() - 1));
                onSubmitClicked(null);
            }
        }
    }

    /**
     * Clears the view
     */
    public void stop() {
        chatViewSubmitButton.setOnMouseClicked(null);
        messageList.heightProperty().removeListener(heightChangedListener);
        submitListener.clear();
    }

    public void setSize(double width, double height) {
        chatViewContainer.setPrefWidth(width);
        chatViewContainer.setPrefHeight(height);
    }

    /**
     * Registers a callback that is called whenever the send button is clicked.
     * @param callback
     */
    public void onMessageSubmit(Consumer<String> callback) {
        submitListener.add(callback);
    }

    /**
     * Appends a message at the end of the messages list.
     * @param message
     */
    public void appendMessage(ServerMessage message) {
        Objects.requireNonNull(messageList);
        Objects.requireNonNull(message);

        shownMessages.add(message);
        if(!loading){
            ServerChatMessage chatMessage = new ServerChatMessage(message, editor);
            chatMessage.setWidthForWrapping(chatViewMessageScrollPane.getWidth());

            Platform.runLater(() -> {
                messageList.getChildren().add(chatMessage);
            });
        }
    }

    private void onSubmitClicked(MouseEvent mouseEvent) {
        String message = chatViewMessageInput.getText();

        if (message.isEmpty()) {
            return;
        }

        chatViewMessageInput.clear();

        submitListener.forEach(callback -> {
            callback.accept(message);
        });
    }

}
