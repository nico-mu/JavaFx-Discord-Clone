package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import de.uniks.stp.emote.EmoteRenderer;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.io.IOException;
import java.util.function.Consumer;

public class EmotePickerButton extends VBox {
    private final EmotePickerPopup popup;

    @Inject
    public EmotePickerButton(ViewLoader viewLoader, EmotePickerPopup popup) {
        this.popup = popup;

        final FXMLLoader fxmlLoader = viewLoader.getFXMLComponentLoader(Components.EMOTE_PICKER_BUTTON);
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
