package de.uniks.stp.component;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.*;
import de.uniks.stp.network.RestClient;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.*;
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

import static de.uniks.stp.model.Category.PROPERTY_CHANNELS;

public class PrivateChatView extends VBox {

    @FXML
    private VBox chatViewContainer;
    @FXML
    private JFXButton chatViewSubmitButton;
    @FXML
    private JFXTextArea chatViewMessageInput;
    @FXML
    private ScrollPane chatViewMessageScrollPane;
    @FXML
    private VBox messageList;

    private final LinkedList<Consumer<String>> submitListener = new LinkedList<>();
    private final InvalidationListener heightChangedListener = this::onHeightChanged;

    public PrivateChatView() {

        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.PRIVATE_CHAT_VIEW);
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
    public void appendMessage(Message message) {
        Objects.requireNonNull(messageList);
        Objects.requireNonNull(message);

        PrivateChatMessage privateChatMessage = new PrivateChatMessage(message);
        privateChatMessage.setWidthForWrapping(chatViewMessageScrollPane.getWidth());

        Platform.runLater(() -> {
            messageList.getChildren().add(privateChatMessage);
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

}
