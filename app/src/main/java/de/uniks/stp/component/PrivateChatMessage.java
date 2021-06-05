package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.emote.EmoteRenderer;
import de.uniks.stp.model.Message;
import de.uniks.stp.util.DateUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Date;

public class PrivateChatMessage extends HBox {
    @FXML
    private TextFlow text;

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
        Text infoPartText = new Text(infoPart);
        infoPartText.setFill(Paint.valueOf("#AAAAAA"));

        TextFlow renderResult = new EmoteRenderer().render(message.getMessage());

        // text.setText(message.getMessage());
        text.getChildren().add(infoPartText);
        text.getChildren().add(renderResult);

        // change color of infoPart
        /* text.setSelectionStart(0);
        text.setSelectionEnd(infoPart.length());
        text.setSelectionFill(Paint.valueOf("#AAAAAA"));  // grayish-white
         */
    }

    private String formatTime(long time) {
        Date date = new Date();
        date.setTime(time);

        if (DateUtil.isToday(date)) {
            return ViewLoader.loadLabel(Constants.LBL_TIME_FORMATTING_TODAY) + ", " + DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
        } else if (DateUtil.isYesterday(date)) {
            return ViewLoader.loadLabel(Constants.LBL_TIME_FORMATTING_YESTERDAY) + ", " + DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
        }

        return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(date);
    }

    public void setWidthForWrapping(double width) {
        // 20px padding
        // text.setWrappingWidth(width - 20);
    }
}
