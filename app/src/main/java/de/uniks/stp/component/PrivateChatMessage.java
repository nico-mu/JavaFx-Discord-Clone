package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Message;
import de.uniks.stp.util.DateUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import java.io.IOException;
import java.util.Locale;

public class PrivateChatMessage extends HBox {
    @FXML
    private TextWithEmoteSupport text;
    private final String language;

    public PrivateChatMessage(String language) {
        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.CHAT_MESSAGE);
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
        String infoPart = DateUtil.formatTime(message.getTimestamp(), Locale.forLanguageTag(language)) + " " + message.getSender().getName() + ": ";
        text.setText(infoPart + message.getMessage());
        // change color of infoPart
        Platform.runLater(() -> {
            Text textWithInfoPart = (Text) text.getChildren().get(0);
            textWithInfoPart.setSelectionStart(0);
            textWithInfoPart.setSelectionEnd(infoPart.length());
            textWithInfoPart.setSelectionFill(Paint.valueOf("#AAAAAA"));
        });
    }
}
