package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.jpa.AccordSettingKey;
import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.model.Message;
import de.uniks.stp.util.DateUtil;
import de.uniks.stp.view.Languages;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class PrivateChatMessage extends HBox {
    @FXML
    private Text text;

    public PrivateChatMessage(Message message, String language) {
        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.CHAT_MESSAGE);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        String infoPart = DateUtil.formatTime(message.getTimestamp(), Locale.forLanguageTag(language)) + " " + message.getSender().getName() + ": ";
        text.setText(infoPart + message.getMessage());

        // change color of infoPart
        text.setSelectionStart(0);
        text.setSelectionEnd(infoPart.length());
        text.setSelectionFill(Paint.valueOf("#AAAAAA"));  // grayish-white
    }

    public void setWidthForWrapping(double width) {
        // 20px padding
        text.setWrappingWidth(width - 20);
    }
}
