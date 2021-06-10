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

    public static final TextStyle EMPTY = new TextStyle();

    public static final Codec<TextStyle> CODEC = new Codec<TextStyle>() {

        private final Codec<Optional<String>> OPT_STRING_CODEC =
            Codec.optionalCodec(Codec.STRING_CODEC);
        private final Codec<Optional<Color>> OPT_COLOR_CODEC =
            Codec.optionalCodec(Codec.COLOR_CODEC);

        @Override
        public String getName() {
            return "text-style";
        }

        @Override
        public void encode(DataOutputStream os, TextStyle s)
            throws IOException {
            os.writeInt(encodeOptionalUint(s.fontSize));
            OPT_STRING_CODEC.encode(os, s.fontFamily);
            OPT_COLOR_CODEC.encode(os, s.textColor);
            OPT_COLOR_CODEC.encode(os, s.backgroundColor);
        }

        @Override
        public TextStyle decode(DataInputStream is) throws IOException {
            byte bius = is.readByte();
            Optional<Integer> fontSize = decodeOptionalUint(is.readInt());
            Optional<String> fontFamily = OPT_STRING_CODEC.decode(is);
            Optional<Color> textColor = OPT_COLOR_CODEC.decode(is);
            Optional<Color> bgrColor = OPT_COLOR_CODEC.decode(is);
            return new TextStyle(fontSize, fontFamily, textColor, bgrColor);
        }

        private int encodeOptionalUint(Optional<Integer> oi) {
            return oi.orElse(-1);
        }

        private Optional<Integer> decodeOptionalUint(int i) {
            return (i < 0) ? Optional.empty() : Optional.of(i);
        }
    };

    static String cssColor(Color color) {
        int red = (int) (color.getRed() * 255);
        int green = (int) (color.getGreen() * 255);
        int blue = (int) (color.getBlue() * 255);
        return "rgb(" + red + ", " + green + ", " + blue + ")";
    }

    final Optional<Integer> fontSize;
    final Optional<String> fontFamily;
    final Optional<Color> textColor;
    final Optional<Color> backgroundColor;

    public TextStyle() {
        this(
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
        );
    }

    public TextStyle(
        Optional<Integer> fontSize,
        Optional<String> fontFamily,
        Optional<Color> textColor,
        Optional<Color> backgroundColor) {
        this.fontSize = fontSize;
        this.fontFamily = fontFamily;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fontSize, fontFamily, textColor, backgroundColor);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TextStyle) {
            TextStyle that = (TextStyle) other;
            return Objects.equals(this.fontSize, that.fontSize) &&
                Objects.equals(this.fontFamily, that.fontFamily) &&
                Objects.equals(this.textColor, that.textColor) &&
                Objects.equals(this.backgroundColor, that.backgroundColor);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        List<String> styles = new ArrayList<>();

        fontSize.ifPresent(s -> styles.add(s.toString()));
        fontFamily.ifPresent(f -> styles.add(f.toString()));
        textColor.ifPresent(c -> styles.add(c.toString()));
        backgroundColor.ifPresent(b -> styles.add(b.toString()));

        return String.join(",", styles);
    }

    public String toCss() {
        StringBuilder sb = new StringBuilder();
        fontSize.ifPresent(integer -> sb.append("-fx-font-size: ").append(integer).append("px;"));

        fontFamily.ifPresent(s -> sb.append("-fx-font-family: ").append(s).append(";"));

        if (textColor.isPresent()) {
            Color color = textColor.get();
            sb.append("-fx-fill: ").append(cssColor(color)).append(";");
        } else {
            sb.append("-fx-fill: ").append("white").append(";");
        }

        if (backgroundColor.isPresent()) {
            Color color = backgroundColor.get();
            sb.append("-rtfx-background-color: ").append(cssColor(color)).append(";");
        }

        return sb.toString();
    }

    public TextStyle updateFontSize(int fontSize) {
        return new TextStyle(Optional.of(fontSize), fontFamily, textColor, backgroundColor);
    }

    public TextStyle updateTextColor(Color textColor) {
        return new TextStyle(fontSize, fontFamily, Optional.of(textColor), backgroundColor);
    }
}
