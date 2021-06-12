package de.uniks.stp.emote;

import de.uniks.stp.ViewLoader;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class EmoteRenderer {
    private Function<String, List<Node>> emoteRenderStrategy;
    private Paint textFill = Color.WHITE;
    private int size = 16;
    private double scalingFactor = 1;

    public EmoteRenderer() {
        this.emoteRenderStrategy = this::imageEmoteRenderStrategy;
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

    public double getScalingFactor() {
        return scalingFactor;
    }

    public EmoteRenderer setEmoteRenderStrategy(Function<String, List<Node>> emoteRenderStrategy) {
        this.emoteRenderStrategy = emoteRenderStrategy;
        return this;
    }

    public EmoteRenderer setTextFill(Paint textFill) {
        this.textFill = textFill;
        return this;
    }

    public EmoteRenderer setScalingFactor(double scalingFactor) {
        this.scalingFactor = scalingFactor;
        return this;
    }

    public EmoteRenderer setSize(Integer size) {
        this.size = size;
        return this;
    }

    public LinkedList<Node> render(String input) {
        input = EmoteParser.convertTextWithUnicodeToNames(input);
        LinkedList<Node> renderResult = new LinkedList<>();
        LinkedList<EmoteParserResult> parsingResult = EmoteParser.parse(input);
        int from = 0;

        for (EmoteParserResult emoteInfo : parsingResult) {
            if (input.substring(from, emoteInfo.getStartIndex()).length() > 0) {
                Text text = createTextNode(input.substring(from, emoteInfo.getStartIndex()));
                renderResult.add(text);
            }
            renderResult.addAll(emoteRenderStrategy.apply(emoteInfo.getEmoteName()));
            from = emoteInfo.getEndIndex() + 1;
        }
        if (from < input.length()) {
            Text text = createTextNode(input.substring(from));
            renderResult.add(text);
        }

        return renderResult;
    }

    public List<Node> imageEmoteRenderStrategy(String emoteName) {
        LinkedList<Node> nodeList = new LinkedList<>();
        String hexCode = (String) getEmoteByName(emoteName).codePoints().mapToObj(Integer::toHexString).toArray()[0];
        try {
            InputStream inputStream = Objects.requireNonNull(ViewLoader.class.getResourceAsStream("emote/openmoji/" + hexCode.toUpperCase() + ".png"));
            Image image = new Image(inputStream);
            ImageView imageView = new ImageView();
            // Important to make the emotes properly displayed in chat
            imageView.setScaleX(getScalingFactor());
            imageView.setScaleY(getScalingFactor());
            imageView.setImage(image);
            if (scalingFactor >= 2) {
                imageView.setFitHeight(getSize() - 5);
                imageView.setFitWidth(getSize() - 5);
                nodeList.add(createSpacer());
                nodeList.add(imageView);
                nodeList.add(createSpacer());
            } else {
                imageView.setFitHeight(getSize());
                imageView.setFitWidth(getSize());
                nodeList.add(imageView);
            }
        } catch (Exception e) {
            // Emotes where no picture can be found are not displayed.
            return nodeList;
        }

        return nodeList;
    }

    public List<Node> textEmoteRenderStrategy(String emoteName) {
        LinkedList<Node> list = new LinkedList<>();
        list.add(createTextNode(getEmoteByName(emoteName)));
        return list;
    }

    private Text createTextNode(String text) {
        Text textNode = new Text();
        textNode.setText(text);
        textNode.setFill(getTextFill());
        textNode.setFont(Font.font(getSize()));

        return textNode;
    }

    private Text createSpacer() {
        Text text = createTextNode("  ");
        text.setFont(Font.font(10));
        return text;
    }
}
