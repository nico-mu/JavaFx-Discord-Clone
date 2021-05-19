package de.uniks.stp.component;

import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Message;
import de.uniks.stp.util.DateUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

public class ServerChatMessage extends HBox {
    @FXML
    private Text messageText;
    @FXML
    private Text nameText;
    @FXML
    private Text timestampText;

    public ServerChatMessage(Message message, Editor editor) {
        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.SERVER_CHAT_MESSAGE);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        timestampText.setText(formatTime(message.getTimestamp()));
        nameText.setText(message.getSender().getName());
        messageText.setText(message.getMessage());

    }

    public void setWidthForWrapping(double width) {
        // 20px padding (without this, a horizontal scroll bar might appear)
        messageText.setWrappingWidth(width - 20);
    }

    private String formatTime(long time) {
        Date date = new Date();
        date.setTime(time);

        if (DateUtil.isToday(date)) {
            return "Today, " + DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
        } else if (DateUtil.isYesterday(date)) {
            return "Yesterday, " + DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
        }

        return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(date);
    }
}
