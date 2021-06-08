package de.uniks.stp.emote;

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
import java.util.function.BiConsumer;

public class EmoteTextArea extends GenericStyledArea<ParStyle, Either<String, LinkedImage>, TextStyle> {
    private static final Logger log = LoggerFactory.getLogger(EmoteTextArea.class);

    private final static TextOps<String, TextStyle> styledTextOps = SegmentOps.styledTextOps();
    private final static LinkedImageOps<TextStyle> linkedImageOps = new LinkedImageOps<>();

    public EmoteTextArea() {
        super(
            ParStyle.EMPTY,                                               // default paragraph style
            (paragraph, style) -> paragraph.setStyle(style.toCss()),        // paragraph style setter
            TextStyle.EMPTY.updateFontSize(12).updateFontFamily("Serif").updateTextColor(Color.BLACK),  // default segment style
            styledTextOps._or(linkedImageOps, (s1, s2) -> Optional.empty()),                            // segment operations
            seg -> createNode(seg, (text, style) -> text.setStyle(style.toCss())));                     // Node creator and segment style setter
        setStyle("-fx-background-color: gray;");
        setWrapText(true);
        // setAutoScrollOnDragDesired(true);
        // Really important line
        // requestFollowCaret();
    }

    public void insertEmote(String emoteName) {
        ReadOnlyStyledDocument<ParStyle, Either<String, LinkedImage>, TextStyle> ros =
            ReadOnlyStyledDocument.fromSegment(Either.right(new RealLinkedImage(emoteName + ".png")),
                ParStyle.EMPTY, TextStyle.EMPTY, this.getSegOps());
        insert(getCaretPosition(), ros);
    }

    public void insertEmote(String emoteName, int index) {
        ReadOnlyStyledDocument<ParStyle, Either<String, LinkedImage>, TextStyle> ros =
            ReadOnlyStyledDocument.fromSegment(Either.right(new RealLinkedImage(emoteName + ".png")),
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
        });

        return sb.toString();
    }

    private static Node createNode(StyledSegment<Either<String, LinkedImage>, TextStyle> seg, BiConsumer<? super TextExt, TextStyle> applyStyle) {
        return seg.getSegment().unify(
            text -> {
                TextExt textNode = (TextExt) StyledTextArea.createStyledTextNode(text, seg.getStyle(), applyStyle);
                return textNode;
            },
            LinkedImage::createNode
        );
    }
}
