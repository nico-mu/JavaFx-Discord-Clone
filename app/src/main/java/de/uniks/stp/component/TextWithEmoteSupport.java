package de.uniks.stp.component;

import de.uniks.stp.emote.EmoteRenderer;
import javafx.application.Platform;
import javafx.scene.Node;
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

    public EmoteRenderer getRenderer() {
        return renderer;
    }
}
