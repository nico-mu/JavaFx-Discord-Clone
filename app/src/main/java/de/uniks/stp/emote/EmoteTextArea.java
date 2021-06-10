package de.uniks.stp.emote;

import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.TextExt;
import org.fxmisc.richtext.model.*;
import org.reactfx.collection.LiveList;
import org.reactfx.util.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class EmoteTextArea extends GenericStyledArea<ParStyle, Either<String, LinkedImage>, TextStyle> {
    private final static TextOps<String, TextStyle> styledTextOps = SegmentOps.styledTextOps();
    private final static LinkedImageOps<TextStyle> linkedImageOps = new LinkedImageOps<>();
    private final static String PLACEHOLDER_TEXT = ViewLoader.loadLabel(Constants.LBL_TEXT_AREA_PLACEHOLDER);

    public EmoteTextArea() {
        super(
            ParStyle.EMPTY, // default paragraph style
            (paragraph, style) -> paragraph.setStyle(style.toCss()),  // paragraph style setter
            TextStyle.EMPTY.updateFontSize(13).updateTextColor(Color.WHITE),  // default segment style
            styledTextOps._or(linkedImageOps, (s1, s2) -> Optional.empty()), // segment operations
            seg -> createNode(seg, (text, style) -> text.setStyle(style.toCss()))); // Node creator and segment style setter
        setAutoScrollOnDragDesired(false);
        setStyle("-fx-background-color: #23272a;");
        setWrapText(true);
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
        insert(index, ros);
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

    private void configurePlaceholder() {
        final AtomicBoolean hasPlaceholder = new AtomicBoolean(true);
        insertPlaceholder();
        this.focusedProperty().addListener((k) -> {
            if (isFocused() && hasPlaceholder.get()) {
                clear();
                ReadOnlyStyledDocument<ParStyle, Either<String, LinkedImage>, TextStyle> ros =
                    ReadOnlyStyledDocument.fromSegment(Either.left(""),
                        ParStyle.EMPTY, TextStyle.EMPTY, this.getSegOps());
                insert(0, ros);
                hasPlaceholder.set(false);
            } else if (getStringContent().isEmpty()) {
                insertPlaceholder();
                hasPlaceholder.set(true);
            }
        });
    }

    private void insertPlaceholder() {
        ReadOnlyStyledDocument<ParStyle, Either<String, LinkedImage>, TextStyle> ros =
            ReadOnlyStyledDocument.fromSegment(Either.left(EmoteTextArea.PLACEHOLDER_TEXT),
                ParStyle.EMPTY, TextStyle.EMPTY.updateTextColor(Color.GRAY), this.getSegOps());
        insert(0, ros);
    }

    private static Node createNode(StyledSegment<Either<String, LinkedImage>, TextStyle> seg, BiConsumer<? super TextExt, TextStyle> applyStyle) {
        return seg.getSegment().unify(
            text -> StyledTextArea.createStyledTextNode(text, seg.getStyle(), applyStyle),
            LinkedImage::createNode
        );
    }
}
