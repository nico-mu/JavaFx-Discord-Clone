package de.uniks.stp.component;

import javafx.geometry.Point2D;
import javafx.scene.control.Control;
import javafx.stage.Popup;
import javafx.stage.Window;

import java.util.function.Consumer;

public class EmotePickerPopup extends Popup {
    private final EmotePicker emotePicker = new EmotePicker();

    public EmotePickerPopup() {
        this.setAutoFix(false);
        this.setAutoHide(true);
        this.setHideOnEscape(true);
        this.setConsumeAutoHidingEvents(false);

        this.getContent().add(emotePicker);
        emotePicker.render();
    }

    public EmotePickerPopup setOnEmoteClicked(Consumer<String> emoteClickHandler) {
        emotePicker.setOnEmoteClicked((emote) -> {
            emoteClickHandler.accept(emote);
            this.hide();
        });
        return this;
    }

    public void show(Control anchor) {
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
