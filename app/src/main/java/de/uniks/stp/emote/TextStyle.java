package de.uniks.stp.emote;

import javafx.scene.paint.Color;
import org.fxmisc.richtext.model.Codec;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Holds information about the style of a text fragment.
 */
public class TextStyle {

    public static final TextStyle EMPTY = new TextStyle().setStyleClass("text-style");
    public static final TextStyle GRAY = new TextStyle().setStyleClass("text-style-gray");

    private String styleClass;

    public String getStyleClass() {
        return styleClass;
    }

    public TextStyle setStyleClass(String styleClass) {
        this.styleClass = styleClass;
        return this;
    }
}
