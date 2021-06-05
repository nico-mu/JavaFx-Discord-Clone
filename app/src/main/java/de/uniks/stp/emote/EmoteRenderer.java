package de.uniks.stp.emote;

import de.uniks.stp.util.Triple;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.*;
import java.util.function.Function;

public class EmoteRenderer {
    private Function<String, Node> emoteRenderStrategy;
    private Paint textFill = Color.WHITE;
    private int size = 16;

    public EmoteRenderer() {
        this.emoteRenderStrategy = this::defaultEmoteRenderStrategy;
    }

    public Map<String, String> getEmoteMapping() {
        return EmoteParser.getEmoteMapping();
    }

    public boolean isEmoteName(String emoteName) {
        return EmoteParser.isEmoteName(emoteName);
    }

    public List<String> getAllEmoteNames() {
        return EmoteParser.getAllEmoteNames();
    }

    public String getEmoteByName(String emoteName) {
        return EmoteParser.getEmoteByName(emoteName);
    }

    public Paint getTextFill() {
        return textFill;
    }

    public int getSize() {
        return size;
    }

    public EmoteRenderer setEmoteRenderStrategy(Function<String, Node> emoteRenderStrategy) {
        this.emoteRenderStrategy = emoteRenderStrategy;
        return this;
    }

    public EmoteRenderer setTextFill(Paint textFill) {
        this.textFill = textFill;
        return this;
    }

    public EmoteRenderer setSize(Integer size) {
        this.size = size;
        return this;
    }

    public TextFlow render(String input) {
        TextFlow renderResult = new TextFlow();
        LinkedList<Triple<Integer, Integer, String>> parsingResult = EmoteParser.parse(input);
        int from = 0;

        for (Triple<Integer, Integer, String> emoteInfo : parsingResult) {
            if (input.substring(from, emoteInfo.getFirst()).length() > 0) {
                Text text = createTextNode(input.substring(from, emoteInfo.getFirst()));
                renderResult.getChildren().add(text);
            }
            renderResult.getChildren().add(emoteRenderStrategy.apply(emoteInfo.getThird()));
            from = emoteInfo.getSecond() + 1;
        }
        if (from < input.length()) {
            Text text = createTextNode(input.substring(from));
            renderResult.getChildren().add(text);
        }

        return renderResult;
    }

    private Text createTextNode(String text) {
        Text textNode = new Text();
        textNode.setText(text);
        textNode.setFill(getTextFill());
        textNode.setFont(Font.font(getSize()));

        return textNode;
    }

    private Text defaultEmoteRenderStrategy(String emoteName) {
        return createTextNode(getEmoteByName(emoteName));
    }

    private Text fontEmoteRenderStrategy() {
        // InputStream inputStream = Objects.requireNonNull(ViewLoader.class.getResourceAsStream("emote/OpenMoji-Color.ttf"));
        // Font font = Font.loadFont(inputStream, 40);
        Text text = new Text();
        // text.setFont(font);
        // text.setText("\uD83D\uDE00");
        return text;
    }
}
