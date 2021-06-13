package de.uniks.stp.component;

import de.uniks.stp.emote.EmoteRenderer;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.LinkedList;

public class TextWithEmoteSupport extends TextFlow {
    private final EmoteRenderer renderer = new EmoteRenderer().setScalingFactor(2);

    public TextWithEmoteSupport setText(String text) {
        getChildren().clear();
        LinkedList<Node> renderResult = renderer.render(text);

        for (Node node : renderResult) {
            getChildren().add(node);
        }

        return this;
    }

    public void setFont(Font font) {
        for (Node node : getChildren()) {
            if (node instanceof Text) {
                ((Text) node).setFont(font);
            }
        }
    }

    public EmoteRenderer getRenderer() {
        return renderer;
    }
}
