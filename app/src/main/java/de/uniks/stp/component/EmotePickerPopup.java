package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;

import javax.inject.Inject;
import java.util.function.Consumer;

public class EmotePickerPopup extends Popup {
    private final EmotePicker emotePicker;

    @Inject
    public EmotePickerPopup(ViewLoader viewLoader) {
        emotePicker = new EmotePicker(viewLoader);
        this.setAutoFix(false);
        this.setAutoHide(true);
        this.setHideOnEscape(true);
        this.setConsumeAutoHidingEvents(false);

        Platform.runLater(() -> {
            this.getContent().add(emotePicker);
            emotePicker.render();
        });
    }

    public EmotePickerPopup setOnEmoteClicked(Consumer<String> emoteClickHandler) {
        emotePicker.setOnEmoteClicked((emote) -> {
            emoteClickHandler.accept(emote);
            this.hide();
        });
        return this;
    }

    public void show(VBox anchor) {
        Window parent = anchor.getScene().getWindow();
        Point2D origin = anchor.localToScene(0, 0);
        double anchorY = parent.getY() + origin.getY()
            + emotePicker.getScene().getY();
        double anchorX = parent.getX() + origin.getX() + parent.getScene().getX();
        this.setOpacity(0);
        this.show(parent, anchorX, anchorY);
        anchorX = parent.getX() + origin.getX() + anchor.getScene().getX() - this.getWidth() / 2 + anchor.getWidth() / 2;
        // 5px space to the button
        anchorY = parent.getY() + origin.getY()
            + anchor.getScene().getY() - this.getHeight() - 5;
        this.setOpacity(1);
        this.show(parent, anchorX, anchorY);
    }
}
