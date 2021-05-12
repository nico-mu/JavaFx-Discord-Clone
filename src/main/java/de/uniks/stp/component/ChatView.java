package de.uniks.stp.component;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Message;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Consumer;

public class ChatView extends VBox {
    final static String SUBVIEW_CONTAINER_ID = "#subview-container";

    @FXML
    private VBox chatViewContainer;
    @FXML
    private JFXButton chatViewSubmitButton;
    @FXML
    private JFXTextArea chatViewMessageInput;
    @FXML
    private ScrollPane chatViewMessageScrollPane;
    private final VBox messageList;

    private final LinkedList<Consumer<String>> submitListener = new LinkedList<>();
    private final InvalidationListener heightChangedListener = this::onHeightChanged;

    public ChatView(Parent parent) {
        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.CHAT_VIEW);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        AnchorPane wrapper = (AnchorPane) parent.lookup(SUBVIEW_CONTAINER_ID);
        chatViewContainer.setPrefWidth(wrapper.getWidth());
        chatViewContainer.setPrefHeight(wrapper.getHeight());
        messageList = (VBox) chatViewMessageScrollPane.getContent();

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
        messageList.heightProperty().removeListener(heightChangedListener);
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

        String infoPart = formatTime(message.getTimestamp()) + " " + message.getSender().getName() + ": ";
        Text text = new Text();
        text.setText( infoPart + message.getMessage());
        text.setFont(Font.font(16));
        text.setFill(Color.WHITE);

        // change color of infoPart
        text.setSelectionStart(0);
        text.setSelectionEnd(infoPart.length());
        text.setSelectionFill(Paint.valueOf("#AAAAAA"));  // greyish-white
        // 20px padding
        text.setWrappingWidth(chatViewMessageScrollPane.getWidth() - 20);

        Platform.runLater(() -> {
            messageList.getChildren().add(text);
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

    private @NotNull String formatTime(long time) {
        Date date = new Date();
        date.setTime(time);

        return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(date);
    }

}
