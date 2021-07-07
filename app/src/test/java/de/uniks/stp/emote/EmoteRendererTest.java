package de.uniks.stp.emote;

import javafx.scene.Node;
import javafx.scene.text.Text;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.LinkedList;

public class EmoteRendererTest {
    @Test
    public void testEmoteRenderer() {
        EmoteRenderer renderer = new EmoteRenderer();
        renderer.setEmoteRenderStrategy(renderer::textEmoteRenderStrategy);

        LinkedList<Node> renderResult = renderer.render("Hello :grinning_face: World:grinning_face:!");

        Assertions.assertEquals(5, renderResult.size());
        Assertions.assertEquals("Hello ", ((Text) renderResult.get(0)).getText());
        Assertions.assertEquals(EmoteParser.getEmoteMapping().get("grinning_face"), ((Text) renderResult.get(1)).getText());
        Assertions.assertEquals(" World", ((Text) renderResult.get(2)).getText());
        Assertions.assertEquals(EmoteParser.getEmoteMapping().get("grinning_face"), ((Text) renderResult.get(3)).getText());
        Assertions.assertEquals("!", ((Text) renderResult.get(4)).getText());

        renderResult = renderer.render("Hello :grinning_face: World:grinning_fac:!");

        Assertions.assertEquals(3, renderResult.size());
        Assertions.assertEquals("Hello ", ((Text) renderResult.get(0)).getText());
        Assertions.assertEquals(EmoteParser.getEmoteMapping().get("grinning_face"), ((Text) renderResult.get(1)).getText());
        Assertions.assertEquals(" World:grinning_fac:!", ((Text) renderResult.get(2)).getText());

        renderResult = renderer.render("Hello :grinning_face: World:grinning_fac::grinning_face_with_smiling_eyes:");

        Assertions.assertEquals(4, renderResult.size());
        Assertions.assertEquals("Hello ", ((Text) renderResult.get(0)).getText());
        Assertions.assertEquals(EmoteParser.getEmoteMapping().get("grinning_face"), ((Text) renderResult.get(1)).getText());
        Assertions.assertEquals(" World:grinning_fac:", ((Text) renderResult.get(2)).getText());
        Assertions.assertEquals(EmoteParser.getEmoteMapping().get("grinning_face_with_smiling_eyes"), ((Text) renderResult.get(3)).getText());

        renderResult = renderer.render(":grinning_face_with_smiling_eyes::grinning_face_with_smiling_eyes:");
        Assertions.assertEquals(2, renderResult.size());
        Assertions.assertEquals(EmoteParser.getEmoteMapping().get("grinning_face_with_smiling_eyes"), ((Text) renderResult.get(0)).getText());
    }
}
