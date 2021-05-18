package de.uniks.stp.component;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.ServerMessage;
import de.uniks.stp.network.RestClient;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class ServerChatView extends VBox {

    private final Editor editor;

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

    public ServerChatView(Editor editor, EventHandler<ActionEvent> loadMessagesHandler) {
        this.editor = editor;

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

        loadMessagesButton.setOnAction(loadMessagesHandler);
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
        chatViewMessageInput.setOnKeyPressed(null);
        loadMessagesButton.setOnAction(null);

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

        ServerChatMessage chatMessage = new ServerChatMessage(message, editor);
        chatMessage.setWidthForWrapping(chatViewMessageScrollPane.getWidth());

        Platform.runLater(() -> {
            messageList.getChildren().add(chatMessage);
        });

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

    /**
     * Used when there are no older messages to load
     * @param visible
     */
    public void setLoadMessagesButtonVisible(boolean visible) {
        loadMessagesButton.setVisible(visible);
    }
}
