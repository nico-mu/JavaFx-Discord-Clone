package de.uniks.stp.emote;

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
