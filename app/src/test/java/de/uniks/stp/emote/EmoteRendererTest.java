package de.uniks.stp.emote;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EmoteRendererTest {
    @Test
    public void testEmoteRenderer() {
        EmoteRenderer renderer = new EmoteRenderer();

        TextFlow renderResult = renderer.render("Hello :grinning_face: World:grinning_face:!");

        Assertions.assertEquals(5, renderResult.getChildren().size());
        Assertions.assertEquals("Hello ", ((Text) renderResult.getChildren().get(0)).getText());
        Assertions.assertEquals("\uD83D\uDE00", ((Text) renderResult.getChildren().get(1)).getText());
        Assertions.assertEquals(" World", ((Text) renderResult.getChildren().get(2)).getText());
        Assertions.assertEquals("\uD83D\uDE00", ((Text) renderResult.getChildren().get(3)).getText());
        Assertions.assertEquals("!", ((Text) renderResult.getChildren().get(4)).getText());

        renderResult = renderer.render("Hello :grinning_face: World:grinning_fac:!");

        Assertions.assertEquals(3, renderResult.getChildren().size());
        Assertions.assertEquals("Hello ", ((Text) renderResult.getChildren().get(0)).getText());
        Assertions.assertEquals("\uD83D\uDE00", ((Text) renderResult.getChildren().get(1)).getText());
        Assertions.assertEquals(" World:grinning_fac:!", ((Text) renderResult.getChildren().get(2)).getText());

        renderResult = renderer.render("Hello :grinning_face: World:grinning_fac::grinning_face_with_big_eyes:");

        Assertions.assertEquals(4, renderResult.getChildren().size());
        Assertions.assertEquals("Hello ", ((Text) renderResult.getChildren().get(0)).getText());
        Assertions.assertEquals("\uD83D\uDE00", ((Text) renderResult.getChildren().get(1)).getText());
        Assertions.assertEquals(" World:grinning_fac:", ((Text) renderResult.getChildren().get(2)).getText());
        Assertions.assertEquals("\uD83D\uDE03", ((Text) renderResult.getChildren().get(3)).getText());
    }
}
