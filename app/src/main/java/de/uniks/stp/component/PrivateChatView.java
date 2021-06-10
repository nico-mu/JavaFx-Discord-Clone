package de.uniks.stp.component;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.emote.EmoteParser;
import de.uniks.stp.emote.EmoteRenderer;
import de.uniks.stp.emote.EmoteTextArea;
import de.uniks.stp.model.Message;
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
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

public class PrivateChatView extends VBox {
    private static final Logger log = LoggerFactory.getLogger(PrivateChatView.class);

    @FXML
    private VBox chatViewContainer;
    @FXML
    private JFXButton chatViewSubmitButton;
    @FXML
    private VBox chatViewEmojiButton;
    @FXML
    private VBox chatViewMessageInput;
    @FXML
    private ScrollPane chatViewMessageScrollPane;
    @FXML
    private VBox messageList;
    private final EmoteTextArea emoteTextArea;

    private Consumer<String> submitListener;
    private final InvalidationListener heightChangedListener = this::onHeightChanged;
    private final EmotePickerPopup popup = new EmotePickerPopup();
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
        chatViewEmojiButton.setOnMouseClicked(this::onEmoteClicked);
        EmoteRenderer renderer = new EmoteRenderer();
        // Image strategy
        renderer.setEmoteRenderStrategy(renderer::imageEmoteRenderStrategy);
        chatViewEmojiButton.getChildren().addAll(renderer.setSize(26).render(":" + "grinning_face" + ":"));

        messageList.heightProperty().addListener(heightChangedListener);

        emoteTextArea = new EmoteTextArea();
        emoteTextArea.setOnKeyPressed(this::checkForEnter);
        VirtualizedScrollPane<EmoteTextArea> scroll = new VirtualizedScrollPane<>(emoteTextArea);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // The scrolling works but it's not a good solution
        emoteTextArea.caretPositionProperty().addListener((k) -> {
            emoteTextArea.layout();
            scroll.layout();
            emoteTextArea.layout();
            scroll.layout();
        });

        chatViewMessageInput.getChildren().add(scroll);
        popup.setOnEmoteClicked(emoteTextArea::insertEmote);

        chatViewMessageScrollPane.setFitToWidth(true);
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
     *
     * @param message
     */
    public void appendMessage(Message message) {
        Objects.requireNonNull(messageList);
        Objects.requireNonNull(message);

        PrivateChatMessage privateChatMessage = new PrivateChatMessage(language);
        privateChatMessage.loadMessage(message);

        Platform.runLater(() -> {
            messageList.getChildren().add(privateChatMessage);
        });
    }

    /**
     * Enter typed -> press send Button | Shift-Enter typed -> add new line
     *
     * @param keyEvent
     */
    private void checkForEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            if (keyEvent.isShiftDown()) {
                // TODO: The new line is not displayed in chat
                emoteTextArea.appendText(System.getProperty("line.separator"));
                emoteTextArea.layout();
            } else {
                onSubmitClicked(null);
            }
        }
    }

    private void onEmoteClicked(MouseEvent mouseEvent) {
        popup.show(chatViewEmojiButton);
    }

    /**
     * If MessageInput is not empty, text is given to method in ServerChatController and MessageInput is cleared.
     *
     * @param mouseEvent
     */
    private void onSubmitClicked(MouseEvent mouseEvent) {
        String message = EmoteParser.toUnicodeString(emoteTextArea.getStringContent());

        if (message.isEmpty()) {
            return;
        }
        emoteTextArea.clear();

        submitListener.accept(message);
    }

    /**
     * Registers a callback that is called whenever the send button is clicked.
     *
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
            emoteTextArea.setDisable(true);
            chatViewEmojiButton.setDisable(true);
            chatViewEmojiButton.setStyle("-fx-background-color: #292C2E;");
            emoteTextArea.setStyle("-fx-background-color: #292C2E;");
            chatViewMessageInput.setStyle("-fx-background-color: #292C2E;");
        });
    }

    public void enable() {
        Platform.runLater(() -> {
            chatViewSubmitButton.setDisable(false);
            emoteTextArea.setDisable(false);
            emoteTextArea.setStyle("-fx-background-color: #23272a;");
            chatViewMessageInput.setStyle("-fx-background-color:#23272a;");
            chatViewMessageInput.setDisable(false);
            chatViewEmojiButton.setDisable(false);
            chatViewEmojiButton.setStyle("-fx-background-color: #23272a;");
        });
    }
}
