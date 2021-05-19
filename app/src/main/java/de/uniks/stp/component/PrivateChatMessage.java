package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Message;
import de.uniks.stp.util.DateUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import org.apache.http.client.utils.DateUtils;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

public class PrivateChatMessage extends HBox {
    @FXML
    private Text text;

    public PrivateChatMessage(Message message) {
        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.CHAT_MESSAGE);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        String infoPart = formatTime(message.getTimestamp()) + " " + message.getSender().getName() + ": ";
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
