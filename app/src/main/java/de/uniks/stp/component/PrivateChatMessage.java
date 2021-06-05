package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.emote.EmoteRenderer;
import de.uniks.stp.model.Message;
import de.uniks.stp.util.DateUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;

public class PrivateChatMessage extends HBox {
    @FXML
    private TextFlow text;

    public PrivateChatMessage() {
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
        EmoteRenderer renderer = new EmoteRenderer().setScalingFactor(2);
        // renderer.setEmoteRenderStrategy(renderer::imageEmoteRenderStrategy);;

        String infoPart = formatTime(message.getTimestamp()) + " " + message.getSender().getName() + ": ";
        LinkedList<Node> renderResult = renderer.render(infoPart + message.getMessage());
        // change color of infoPart
        Text textWithInfoPart = (Text) renderResult.get(0);
        textWithInfoPart.setSelectionStart(0);
        textWithInfoPart.setSelectionEnd(infoPart.length());
        textWithInfoPart.setSelectionFill(Paint.valueOf("#AAAAAA"));

        for (Node node : renderResult) {
            Platform.runLater(() -> {
                text.getChildren().add(node);
            });
        }
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
}
