package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import de.uniks.stp.emote.EmoteRenderer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class EmotePicker extends VBox {
    private static final Logger log = LoggerFactory.getLogger(EmotePicker.class);

    @FXML
    private VBox emotePickerContainer;

    private Function<String, Node> renderStrategy;

    public EmotePicker() {
        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.EMOTE_PICKER);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.setWidth(400);
    }

    public EmotePicker setRenderStrategy(Function<String, Node> renderStrategy) {
        this.renderStrategy = renderStrategy;
        return this;
    }

    public void render() {
        if (Objects.isNull(renderStrategy)) {
            renderStrategy = this::defaultRenderStrategy;
        }

        List<String> emotes = new LinkedList<>();
        emotes.add("Emoji1");
        TextFlow result = new TextFlow();

        for (String emote: emotes) {
            result.getChildren().add(renderStrategy.apply(emote));
        }

        emotePickerContainer.getChildren().add(result);
    }

    private TextFlow defaultRenderStrategy(String emote) {
        /* Label label = new Label("\uD83D\uDE04");
        label.setOnMouseClicked((k) -> System.out.println("Clicked"));
        label.setStyle("-fx-text-fill: white");/*
         */
        TextFlow result = new EmoteRenderer().render("");

        log.debug("{}", result.getChildren().size());

        return result;
    }
}
