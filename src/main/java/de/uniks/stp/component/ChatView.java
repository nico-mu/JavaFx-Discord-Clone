package de.uniks.stp.component;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Message;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Consumer;

public class ChatView {
    final static String SUBVIEW_CONTAINER_ID = "#subview-container";
    final static String SUBMIT_BUTTON_ID = "#chatview__submit-button";
    final static String MESSAGE_INPUT_ID = "#chatview__message-input";
    final static String MESSAGE_LIST_ID = "#chatview__message-list";
    final static String CONTAINER = "#chatview__container";

    private final JFXButton submitButton;
    private final Parent component;
    private final JFXTextArea messageInput;
    private final VBox messageList;
    private final ScrollPane chatViewScrollPane;
    private final LinkedList<Consumer<String>> submitListener = new LinkedList<>();

    public ChatView(Parent parent) {
        component = ViewLoader.loadComponent(Components.CHAT_VIEW);

        VBox chatViewContainer = (VBox) component.lookup(CONTAINER);
        AnchorPane wrapper = (AnchorPane) parent.lookup(SUBVIEW_CONTAINER_ID);
        chatViewContainer.setPrefWidth(wrapper.getWidth());
        chatViewContainer.setPrefHeight(wrapper.getHeight());

        submitButton = (JFXButton) component.lookup(SUBMIT_BUTTON_ID);
        messageInput = (JFXTextArea) component.lookup(MESSAGE_INPUT_ID);
        chatViewScrollPane = (ScrollPane) component.lookup(MESSAGE_LIST_ID);
        messageList = (VBox) chatViewScrollPane.getContent();

        submitButton.setOnMouseClicked(this::onSubmitClicked);
    }

    /**
     * Clears the view
     */
    public void stop() {
        submitButton.setOnMouseClicked(null);
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
    public void appendMessage(@NotNull Message message) {
        Objects.requireNonNull(messageList);
        Objects.requireNonNull(message);

        Text text = new Text();
        text.setText(formatTime(message.getTimestamp()) + " " + message.getSender().getName() + ": " + message.getMessage());
        text.setFont(Font.font(16));
        text.setFill(Color.WHITE);
        // 20px padding
        text.setWrappingWidth(chatViewScrollPane.getWidth() - 20);

        Platform.runLater(() -> {
            messageList.getChildren().add(text);
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

    private @NotNull String formatTime(long time) {
        Date date = new Date();
        date.setTime(time);

        return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(date);
    }

}
