package de.uniks.stp.component;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.emote.EmoteParser;
import de.uniks.stp.emote.EmoteRenderer;
import de.uniks.stp.emote.EmoteTextArea;
import de.uniks.stp.model.ServerMessage;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

public class ServerChatView extends VBox {
    @FXML
    private VBox chatViewContainer;
    @FXML
    private JFXButton chatViewSubmitButton;
    @FXML
    private VBox chatViewMessageInput;
    @FXML
    private ScrollPane chatViewMessageScrollPane;
    @FXML
    private JFXButton loadMessagesButton;
    @FXML
    private VBox messageList;
    @FXML
    private VBox chatVBox;
    @FXML
    private HBox chatViewControlsContainer;

    private Consumer<String> submitListener;
    private final InvalidationListener heightChangedListener = this::onHeightChanged;
    private final EmoteTextArea emoteTextArea;
    private final String language;

    public ServerChatView(EventHandler<ActionEvent> loadMessagesHandler, String language) {
        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.SERVER_CHAT_VIEW);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        this.language = language;
        chatViewSubmitButton.setOnMouseClicked(this::onSubmitClicked);

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

        chatViewMessageInput.setOnKeyPressed(this::checkForEnter);

        loadMessagesButton.setOnAction(loadMessagesHandler);
        chatViewMessageInput.getChildren().add(scroll);
        EmotePickerButton emotePickerButton = new EmotePickerButton();
        chatViewControlsContainer.getChildren().add(1, emotePickerButton);
        emotePickerButton.setOnEmoteClicked(emoteTextArea::insertEmote);

        chatViewMessageScrollPane.setFitToWidth(true);

    }

    /**
     * Clears the view
     */
    public void stop() {
        chatViewSubmitButton.setOnMouseClicked(null);
        chatViewMessageInput.setOnKeyPressed(null);
        loadMessagesButton.setOnAction(null);

        messageList.heightProperty().removeListener(heightChangedListener);
        submitListener = null;
    }

    /**
     * Appends a message at the end of the messages list.
     * @param message
     * @param iniviteIds
     */
    public void appendMessage(ServerMessage message, Pair<String, String> iniviteIds, EventHandler<ActionEvent> onButtonPressed) {
        Objects.requireNonNull(messageList);
        Objects.requireNonNull(message);


        ServerChatMessage chatMessage = new ServerChatMessage(language);
        chatMessage.loadMessage(message);
        if(iniviteIds != null){
            chatMessage.addButton(iniviteIds, onButtonPressed);
        }

        Platform.runLater(() -> {
            messageList.getChildren().add(chatMessage);
        });
    }

    public void insertMessage(int pos, ServerMessage message, Pair<String, String> inviteIds, EventHandler<ActionEvent> onButtonPressed) {
        Objects.requireNonNull(messageList);
        Objects.requireNonNull(message);

        ServerChatMessage chatMessage = new ServerChatMessage(language);
        chatMessage.loadMessage(message);
        if(inviteIds != null){
            chatMessage.addButton(inviteIds, onButtonPressed);
        }


        Platform.runLater(() -> {
            messageList.getChildren().add(pos, chatMessage);
        });
    }

    /**
     * Enter typed -> press send Button | Shift-Enter typed -> add new line
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
     * @param callback should send message via websocket
     */
    public void setOnMessageSubmit(Consumer<String> callback) {
        submitListener = callback;
    }

    private void onHeightChanged(Observable observable) {
        chatViewMessageScrollPane.setVvalue(1.0);
    }

    /**
     * Used when there are no older messages to load
     */
    public void removeLoadMessagesButton() {
        Platform.runLater(()-> chatVBox.getChildren().remove(loadMessagesButton));
    }

    public void clearMessages() {
        Platform.runLater(() -> {
            messageList.getChildren().clear();
        });
    }
}
