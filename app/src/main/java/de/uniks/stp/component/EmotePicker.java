package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import de.uniks.stp.emote.EmoteParser;
import de.uniks.stp.emote.EmoteRenderer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Flow;
import java.util.function.Consumer;
import java.util.function.Function;

public class EmotePicker extends VBox {
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
        FlowPane result = new FlowPane();
        result.setOrientation(Orientation.HORIZONTAL);
        result.setAlignment(Pos.TOP_LEFT);
        EmoteRenderer renderer = new EmoteRenderer().setSize(30);

        for (String emote: EmoteParser.getAllEmoteNames()) {
            // LinkedList<Node> nodes = (LinkedList<Node>) renderer.imageEmoteRenderStrategy(emote);
            LinkedList<Node> nodes = (LinkedList<Node>) renderer.defaultEmoteRenderStrategy(emote);

            for (Node node : nodes) {
                node.setOnMouseClicked((k) -> {
                    emoteClickHandler.accept(emote);
                });
                result.getChildren().add(node);
            }
        }

        Platform.runLater(() -> {
            emotePickerContainer.getChildren().add(result);
        });
    }
}
