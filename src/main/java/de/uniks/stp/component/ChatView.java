package de.uniks.stp.component;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.User;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Consumer;

public class ChatView {

    private final Parent component;
    private JFXTextArea messageInput;
    private VBox messageList;
    private LinkedList<Consumer<String>> submitListener = new LinkedList<>();
    private User owner;

    public ChatView() {
        final String SUBMIT_BUTTON_ID = "#chatview__submit-button";
        final String MESSAGE_INPUT_ID = "#chatview__message-input";
        final String MESSAGE_LIST_ID = "#chatview__message-list";

        this.owner = owner;

        component = ViewLoader.loadComponent(Components.CHAT_VIEW);

        JFXButton submitButton = (JFXButton) component.lookup(SUBMIT_BUTTON_ID);
        messageInput = (JFXTextArea) component.lookup(MESSAGE_INPUT_ID);
        messageList = (VBox) ((ScrollPane) component.lookup(MESSAGE_LIST_ID)).getContent();

        submitButton.setOnMouseClicked(this::onSubmitClicked);
    }

    /**
     * Returns the view reference for putting the component onto the screen.
     */
    public final Parent getComponent() {
        return component;
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
        if (Objects.isNull(messageList)) {
            System.err.println("message list not set");
            return;
        }

        Platform.runLater(() -> {
            messageList.getChildren().add(new Text(formatTime(message.getTimestamp())  + message.getSender().getName() + message.getMessage()));
        });

    }

    private void onSubmitClicked(MouseEvent mouseEvent) {
        String message = messageInput.getText();

        if (message.isEmpty()) {
            return;
        }

        messageInput.clear();

        submitListener.forEach(callback -> {
            callback.accept(message);
        });
    }

    private String formatTime(long time) {
        Date date = new Date();
        date.setTime(time);

        return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(date);
    }

}
