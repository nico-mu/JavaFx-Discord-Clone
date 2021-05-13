package de.uniks.stp.component;

import de.uniks.stp.model.Message;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.text.DateFormat;
import java.util.Date;

public class ChatMessage extends Text {
    public ChatMessage(Message message) {
        String infoPart = formatTime(message.getTimestamp()) + " " + message.getSender().getName() + ": ";
        setText(infoPart + message.getMessage());
        setFont(Font.font(16));
        setFill(Color.WHITE);

        // change color of infoPart
        setSelectionStart(0);
        setSelectionEnd(infoPart.length());
        setSelectionFill(Paint.valueOf("#AAAAAA"));  // grayish-white
    }

    public void setWidthForWrapping(double width) {
        // 20px padding
        this.setWrappingWidth(width - 20);
    }

    private String formatTime(long time) {
        Date date = new Date();
        date.setTime(time);

        // TODO: Format time for today and yesterday

        return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(date);
    }
}
