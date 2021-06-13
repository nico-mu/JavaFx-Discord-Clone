package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import de.uniks.stp.emote.EmoteRenderer;
import de.uniks.stp.model.Message;
import de.uniks.stp.util.DateUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Locale;

public class ServerChatMessage extends HBox {
    @FXML
    private TextWithEmoteSupport messageText;
    @FXML
    private Text nameText;
    @FXML
    private Text timestampText;
    private final String language;

    public ServerChatMessage(String language) {
        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.SERVER_CHAT_MESSAGE);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        this.language = language;
    }

    public void loadMessage(Message message) {
        timestampText.setText(DateUtil.formatTime(message.getTimestamp(), Locale.forLanguageTag(language)));
        timestampText.setText(DateUtil.formatTime(message.getTimestamp(), Locale.forLanguageTag(language)));
        nameText.setText(message.getSender().getName());
        messageText.setText(message.getMessage());
    }
}
