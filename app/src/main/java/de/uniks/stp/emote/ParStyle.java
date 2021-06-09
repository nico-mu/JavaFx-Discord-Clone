package de.uniks.stp.emote;

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

        sb.append("-fx-background-color: #23272a;");
        sb.append("-fx-fill: white;");
        sb.append("-fx-text-fill: white;");
        sb.append("-fx-font-size: 13px");

        return sb.toString();
    }
}
