package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Message;
import de.uniks.stp.util.DateUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.io.IOException;
import java.util.Locale;

public class ChatMessage extends HBox {
    @FXML
    private TextWithEmoteSupport messageText;
    @FXML
    private Text nameText;
    @FXML
    private Text timestampText;
    @FXML
    private VBox textVBox;

    private String language;

    public ChatMessage(String language) {
        this.language = language;
        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.CHAT_MESSAGE);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void loadMessage(Message message) {
        timestampText.setText(DateUtil.formatTime(message.getTimestamp(), Locale.forLanguageTag(language)));
        timestampText.setText(DateUtil.formatTime(message.getTimestamp(), Locale.forLanguageTag(language)));
        nameText.setText(message.getSender().getName());
        messageText.setText(message.getMessage());

    }

    public void addButton(Pair<String, String> inviteIds, EventHandler<ActionEvent> onButtonPressed){
        JoinServerButton button = new JoinServerButton(inviteIds, onButtonPressed);
        Platform.runLater(()-> textVBox.getChildren().add(button));
    }
}
