package de.uniks.stp.emote;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.fxmisc.flowless.Virtualized;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.TextEditingArea;

public class VirtualizedScrollPaneDecorator<V extends Node & Virtualized & TextEditingArea<?, ?, ?>> extends StackPane {

    private final V textEditingArea;

    private final VirtualizedScrollPane<V> scrollPane;

    private final Text placeholderText;

    public VirtualizedScrollPaneDecorator(V textEditingArea, String placeholder) {

        this.textEditingArea = textEditingArea;
        this.scrollPane = new VirtualizedScrollPane<>(textEditingArea);
        this.placeholderText = new Text(placeholder);
        placeholderText.setFill(Color.WHITE);
        placeholderText.setFont(Font.font(14));

        textEditingArea.textProperty().addListener((observable, oldValue, newValue) -> {
                placeholderText.setVisible(newValue == null || newValue.isEmpty());
                // getChildren().remove(0);
                // getChildren().add(textEditingArea);
            }
        );

        placeholderText.setStyle("-fx-fill: #7f7f7f");
        setAlignment(placeholderText, Pos.TOP_LEFT);

        // textEditingArea.setStyle("-fx-background-color: transparent");

        getChildren().addAll(placeholderText, textEditingArea);
    }

    public VirtualizedScrollPane<V> getScrollPane() {
        return scrollPane;
    }

    public V getTextEditingArea() {
        return textEditingArea;
    }

    public void setPlaceholder(String value) {
        placeholderText.setText(value);
    }
}
