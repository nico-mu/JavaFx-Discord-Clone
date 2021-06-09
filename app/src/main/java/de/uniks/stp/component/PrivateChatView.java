package de.uniks.stp.component;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Message;
import de.uniks.stp.view.Languages;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

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

    private Consumer<String> submitListener;
    private final InvalidationListener heightChangedListener = this::onHeightChanged;
    private final String language;

    public PrivateChatView(String language) {
        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.PRIVATE_CHAT_VIEW);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        this.setId("chat-view");

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        this.language = language;
        chatViewSubmitButton.setOnMouseClicked(this::onSubmitClicked);

        messageList.heightProperty().addListener(heightChangedListener);

        chatViewMessageInput.setOnKeyPressed(this::checkForEnter);
    }

    /**
     * Clears the view
     */
    public void stop() {
        chatViewSubmitButton.setOnMouseClicked(null);
        chatViewMessageInput.setOnKeyPressed(null);
        messageList.heightProperty().removeListener(heightChangedListener);
        submitListener = null;
    }

    /**
     * Appends a message at the end of the messages list.
     * @param message
     */
    public void appendMessage(Message message) {
        Objects.requireNonNull(messageList);
        Objects.requireNonNull(message);

        PrivateChatMessage privateChatMessage = new PrivateChatMessage(message, language);
        privateChatMessage.setWidthForWrapping(chatViewMessageScrollPane.getWidth());

        Platform.runLater(() -> {
            messageList.getChildren().add(privateChatMessage);
        });
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
     * If MessageInput is not empty, text is given to method in ServerChatController and MessageInput is cleared.
     * @param mouseEvent
     */
    private void onSubmitClicked(MouseEvent mouseEvent) {
        String message = chatViewMessageInput.getText();

        if (message.isEmpty()) {
            return;
        }
        chatViewMessageInput.clear();

        submitListener.accept(message);
    }

    /**
     * Registers a callback that is called whenever the send button is clicked.
     * @param callback should send message via websocket
     */
    public void setOnMessageSubmit(Consumer<String> callback) {
        submitListener = callback;
    }

    private void onHeightChanged(Observable observable) {
        chatViewMessageScrollPane.setVvalue(1.0);
    }

    public void disable() {
        Platform.runLater(() -> {
            chatViewSubmitButton.setDisable(true);
            chatViewMessageInput.setDisable(true);
        });
    }

    public void enable() {
        Platform.runLater(() -> {
            chatViewSubmitButton.setDisable(false);
            chatViewMessageInput.setDisable(false);
        });
    }
}
