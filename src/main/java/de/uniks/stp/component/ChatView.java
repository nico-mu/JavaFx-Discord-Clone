package de.uniks.stp.component;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Message;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Consumer;

public class ChatView {

    private final Parent component;
    private JFXTextArea messageInput;
    private JFXTextArea messageList;
    private VBox chatViewContainer;
    private LinkedList<Consumer<String>> submitListener = new LinkedList<>();

    public ChatView(Parent parent) {
        final String SUBVIEW_CONTAINER_ID = "#subview-container";
        final String SUBMIT_BUTTON_ID = "#chatview__submit-button";
        final String MESSAGE_INPUT_ID = "#chatview__message-input";
        final String MESSAGE_LIST_ID = "#chatview__message-list";
        final String CONTAINER = "#chatview__container";

        component = ViewLoader.loadComponent(Components.CHAT_VIEW);

        chatViewContainer = (VBox) component.lookup(CONTAINER);
        AnchorPane wrapper = (AnchorPane) parent.lookup(SUBVIEW_CONTAINER_ID);
        chatViewContainer.setPrefWidth(wrapper.getPrefWidth());
        chatViewContainer.setPrefHeight(wrapper.getPrefHeight());

        JFXButton submitButton = (JFXButton) component.lookup(SUBMIT_BUTTON_ID);
        messageInput = (JFXTextArea) component.lookup(MESSAGE_INPUT_ID);
        messageList = (JFXTextArea) component.lookup(MESSAGE_LIST_ID);
        messageList.setFont(Font.font(16));

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
    public void appendMessage(@NotNull Message message) {
        Objects.requireNonNull(messageList);
        Objects.requireNonNull(message);

        Platform.runLater(() -> {
            messageList.setText(messageList.getText()
                + (messageList.getText().isEmpty() ? "" : "\n")
                + formatTime(message.getTimestamp()) + " " + message.getSender().getName() + ": " + message.getMessage());
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
