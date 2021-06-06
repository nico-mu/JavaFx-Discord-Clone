package de.uniks.stp.component;

import de.uniks.stp.Constants;
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
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;

public class ServerChatMessage extends HBox {
    @FXML
    private TextFlow messageText;
    @FXML
    private Text nameText;
    @FXML
    private Text timestampText;

    public ServerChatMessage() {
        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.SERVER_CHAT_MESSAGE);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void loadMessage(Message message) {
        timestampText.setText(formatTime(message.getTimestamp()));
        nameText.setText(message.getSender().getName());
        EmoteRenderer renderer = new EmoteRenderer().setScalingFactor(2).setSize(16);
        // renderer.setEmoteRenderStrategy(renderer::imageEmoteRenderStrategy);
        LinkedList<Node> renderResult = renderer.render(message.getMessage());

        for (Node node : renderResult) {
            Platform.runLater(() -> {
                messageText.getChildren().add(node);
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
