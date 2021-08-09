package de.uniks.stp.component;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.emote.EmoteParser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.fxmisc.flowless.VirtualizedScrollPane;

import javax.inject.Inject;
import java.io.IOException;
import java.util.function.Consumer;

public class ChatMessageInput extends HBox {

    @FXML
    private HBox root;

    @FXML
    private JFXButton chatViewSubmitButton;

    @FXML
    private VBox chatViewMessageInput;

    private final EmotePickerButton emotePickerButton;
    private final EmoteTextArea emoteTextArea;
    private Consumer<String> submitListener;

    @Inject
    public ChatMessageInput(ViewLoader viewLoader,
                            EmotePickerButton emotePickerButton,
                            EmoteTextArea emoteTextArea) {
        FXMLLoader fxmlLoader = viewLoader.getFXMLComponentLoader(Components.CHAT_MESSAGE_INPUT);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        this.setId("chat-view-input");

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.emotePickerButton = emotePickerButton;
        this.emoteTextArea = emoteTextArea;
        this.emoteTextArea.setId("message-input-area");
        emoteTextArea.setOnKeyPressed(this::checkForEnter);

        VirtualizedScrollPane<EmoteTextArea> scroll = new VirtualizedScrollPane<>(emoteTextArea);
        chatViewMessageInput.setPrefHeight(emoteTextArea.getViewportHeight());
        emoteTextArea.textProperty().addListener(((observable, oldValue, newValue) -> {
            int lines = 0;
            for (int i = 0; i < emoteTextArea.getParagraphs().size(); i++) {
                lines += emoteTextArea.getParagraphLinesCount(i);
            }
            int lineHeight = 15;
            int padding = 25;
            chatViewMessageInput.setMaxHeight(lines * lineHeight + padding);
            chatViewMessageInput.setPrefHeight(lines * lineHeight + padding);
            root.setPrefHeight(lines * lineHeight + padding);
        }));
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        chatViewSubmitButton.setOnMouseClicked(this::onSubmitClicked);


        // The scrolling works but it's not a good solution
        emoteTextArea.caretPositionProperty().addListener((k) -> {
            emoteTextArea.layout();
            scroll.layout();
            emoteTextArea.layout();
            scroll.layout();
            emoteTextArea.requestFollowCaret();
        });

        this.getChildren().add(1, emotePickerButton);
        chatViewMessageInput.getChildren().add(scroll);
        emotePickerButton.setOnEmoteClicked(emoteTextArea::insertEmote);
    }


    /**
     * Enter typed -> press send Button | Shift-Enter typed -> add new line
     *
     * @param keyEvent
     */
    private void checkForEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            if (keyEvent.isShiftDown()) {
                emoteTextArea.appendText(System.getProperty("line.separator"));
                emoteTextArea.layout();
            } else {
                onSubmitClicked(null);
            }
        }
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

    public void disable() {
        Platform.runLater(() -> {
            chatViewSubmitButton.setDisable(true);
            emotePickerButton.setDisable(true);
            emoteTextArea.disable();
        });
    }

    public void enable() {
        Platform.runLater(() -> {
            chatViewSubmitButton.setDisable(false);
            emoteTextArea.enable();
            emotePickerButton.setDisable(false);
        });
    }

}
