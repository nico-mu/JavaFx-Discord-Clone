package de.uniks.stp.component;

import de.uniks.stp.AccordApp;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.emote.*;
import javafx.scene.Node;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.TextExt;
import org.fxmisc.richtext.model.*;
import org.reactfx.collection.LiveList;
import org.reactfx.util.Either;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class EmoteTextArea extends GenericStyledArea<ParStyle, Either<String, LinkedImage>, TextStyle> {
    private final static TextOps<String, TextStyle> styledTextOps = SegmentOps.styledTextOps();
    private final static LinkedImageOps<TextStyle> linkedImageOps = new LinkedImageOps<>();

    private final String placeholderText;
    private final ReadOnlyStyledDocument<ParStyle, Either<String, LinkedImage>, TextStyle> placeholderNode;

    private final AtomicBoolean hasPlaceholder = new AtomicBoolean(true);

    @Inject
    public EmoteTextArea(ViewLoader viewLoader) {
        super(
            ParStyle.EMPTY, // default paragraph style
            (paragraph, style) -> {  // paragraph style setter
                paragraph.getStyleClass().add("par-style");
            },
            TextStyle.EMPTY,  // default segment style
            styledTextOps._or(linkedImageOps, (s1, s2) -> Optional.empty()), // segment operations
            seg -> createNode(seg, (text, style) -> {
                text.getStyleClass().add(style.getStyleClass());
            })); // Node creator and segment style setter

        placeholderText = viewLoader.loadLabel(Constants.LBL_TEXT_AREA_PLACEHOLDER);
        placeholderNode = ReadOnlyStyledDocument.fromSegment(Either.left(placeholderText),
            ParStyle.EMPTY,
            TextStyle.GRAY,
            this.getSegOps());

        setAutoScrollOnDragDesired(false);
        setWrapText(true);
        getStyleClass().add("emote-text-area");
        getStylesheets().add(Objects.requireNonNull(AccordApp.class.getResource("/de/uniks/stp/style/css/emote-text-area.css")).toExternalForm());
        // Add placeholder by default and delete it on focus
        configurePlaceholder();
    }

    public void insertEmote(String emoteName) {
        insertEmote(emoteName, getCaretPosition());
    }

    public void insertEmote(String emoteName, int index) {
        ReadOnlyStyledDocument<ParStyle, Either<String, LinkedImage>, TextStyle> ros =
            ReadOnlyStyledDocument.fromSegment(Either.right(new RealLinkedImage(emoteName)),
                ParStyle.EMPTY, TextStyle.EMPTY, this.getSegOps());

        if (hasPlaceholder.get() && getStringContent().equals(placeholderText)) {
            deleteText(0, placeholderText.length());
            hasPlaceholder.set(false);
            insert(0, ros);
        } else {
            insert(index, ros);
        }
    }

    public String getStringContent() {
        EditableStyledDocument<ParStyle, Either<String, LinkedImage>, TextStyle> content = getContent();
        LiveList<Paragraph<ParStyle, Either<String, LinkedImage>, TextStyle>> paragraphs = content.getParagraphs();
        StringBuilder sb = new StringBuilder();

        paragraphs.forEach((paragraph) -> {
            for (Either<String, LinkedImage> segment : paragraph.getSegments()) {
                segment.ifLeft(sb::append);
                segment.ifRight(image -> {
                    sb.append(":").append(image.toString()).append(":");
                });
            }
            sb.append(System.getProperty("line.separator"));
        });

        return sb.toString().stripTrailing();
    }

    public void disable() {
        setDisable(true);
        clear();
        appendText("Offline");
    }

    public void enable() {
        clear();
        setDisable(false);
    }

    public AtomicBoolean hasPlaceholder() {
        return hasPlaceholder;
    }

    public void setHasPlaceholder(boolean hasPlaceholder) {
        this.hasPlaceholder.set(hasPlaceholder);
    }

    private void configurePlaceholder() {
        insertPlaceholder();
        ReadOnlyStyledDocument<ParStyle, Either<String, LinkedImage>, TextStyle> ros =
            ReadOnlyStyledDocument.fromSegment(Either.left(""),
                ParStyle.EMPTY, TextStyle.EMPTY, this.getSegOps());

        focusedProperty().addListener((k) -> {
            if (isFocused() && hasPlaceholder.get()) {
                clear();
                insert(0, ros);
                hasPlaceholder.set(false);
            } else if (!isFocused() && !hasPlaceholder.get() && getStringContent().isEmpty()) {
                insertPlaceholder();
                hasPlaceholder.set(true);
            }
        });

        caretPositionProperty().addListener((k) -> {
            if (hasPlaceholder.get() && getStringContent().equals(placeholderText)) {
                deleteText(0, placeholderText.length());
                hasPlaceholder.set(false);
            }
        });
    }

    private void insertPlaceholder() {
        insert(0, placeholderNode);
    }

    private static Node createNode(StyledSegment<Either<String, LinkedImage>, TextStyle> seg, BiConsumer<? super TextExt, TextStyle> applyStyle) {
        return seg.getSegment().unify(
            text -> StyledTextArea.createStyledTextNode(text, seg.getStyle(), applyStyle),
            LinkedImage::createNode
        );
    }
}
