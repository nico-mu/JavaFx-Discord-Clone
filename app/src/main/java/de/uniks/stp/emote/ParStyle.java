package de.uniks.stp.emote;

import static javafx.scene.text.TextAlignment.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import org.fxmisc.richtext.model.Codec;

/**
 * Holds information about the style of a paragraph.
 */
class ParStyle {

    public static final ParStyle EMPTY = new ParStyle();

    @Override
    public String toString() {
        return toCss();
    }

    public String toCss() {
        StringBuilder sb = new StringBuilder();

        sb.append("-fx-background-color: gray;");

        return sb.toString();
    }
}
