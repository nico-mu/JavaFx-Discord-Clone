package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import de.uniks.stp.emote.EmoteParser;
import de.uniks.stp.emote.EmoteRenderer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class EmotePicker extends VBox {
    private static final Logger log = LoggerFactory.getLogger(EmotePicker.class);

    @FXML
    private VBox emotePickerContainer;
    private Consumer<String> emoteClickHandler;

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

    public EmotePicker setOnEmoteClicked(Consumer<String> emoteClickHandler) {
        this.emoteClickHandler = emoteClickHandler;
        return this;
    }

    public void render() {
        TextFlow result = new TextFlow();

        for (String emote: EmoteParser.getAllEmoteNames()) {
            TextFlow renderedEmote = new EmoteRenderer().setSize(24).render(":" + emote + ":");

            renderedEmote.setOnMouseClicked((k) -> {
                emoteClickHandler.accept(emote);
            });

            result.getChildren().add(renderedEmote);
        }

        emotePickerContainer.getChildren().add(result);
    }
}
