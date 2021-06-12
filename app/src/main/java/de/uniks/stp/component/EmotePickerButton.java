package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import de.uniks.stp.emote.EmoteRenderer;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.function.Consumer;

public class EmotePickerButton extends VBox {
    private final EmotePickerPopup popup = new EmotePickerPopup();

    public EmotePickerButton() {
        final FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.EMOTER_PICKER_BUTTON);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        EmoteRenderer renderer = new EmoteRenderer();
        getChildren().addAll(renderer.setSize(26).render(":" + "grinning_face" + ":"));
        setOnMouseClicked(this::onEmoteClicked);
    }

    public void setOnEmoteClicked(Consumer<String> onEmoteClicked) {
        popup.setOnEmoteClicked(onEmoteClicked);
    }

    private void onEmoteClicked(MouseEvent mouseEvent) {
        popup.show(this);
    }
}
