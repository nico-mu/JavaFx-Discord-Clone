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
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.io.IOException;
import java.util.LinkedList;
import java.util.function.Consumer;

public class EmotePicker extends ScrollPane {
    @FXML
    private VBox emotePickerContainer;

    private Consumer<String> emoteClickHandler;

    @Inject
    public EmotePicker(ViewLoader viewLoader) {
        FXMLLoader fxmlLoader = viewLoader.getFXMLComponentLoader(Components.EMOTE_PICKER);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        setFitToWidth(true);
        setFitToHeight(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
    }

    public EmotePicker setOnEmoteClicked(Consumer<String> emoteClickHandler) {
        this.emoteClickHandler = emoteClickHandler;
        return this;
    }

    public void render() {
        FlowPane result = new FlowPane();
        result.setOrientation(Orientation.HORIZONTAL);
        result.setAlignment(Pos.TOP_CENTER);
        EmoteRenderer renderer = new EmoteRenderer().setSize(30);

        for (String emote: EmoteParser.getAllEmoteNames()) {
            LinkedList<Node> nodes = (LinkedList<Node>) renderer.imageEmoteRenderStrategy(emote);

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
