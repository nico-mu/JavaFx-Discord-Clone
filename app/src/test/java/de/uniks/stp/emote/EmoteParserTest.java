package de.uniks.stp.emote;

import de.uniks.stp.util.Triple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

public class EmoteParserTest {
    @Test
    public void testEmoteParser() {
        Assertions.assertEquals(2, EmoteParser.getEmoteMapping().size());
        Assertions.assertEquals("\uD83D\uDE00", EmoteParser.getEmoteMapping().get("grinning_face"));

        LinkedList<Triple<Integer, Integer, String>> parsingResult = EmoteParser.parse(":grinning_face:");

        Assertions.assertEquals(1, parsingResult.size());
        Assertions.assertEquals(0, parsingResult.get(0).getFirst());
        Assertions.assertEquals(14, parsingResult.get(0).getSecond());
        Assertions.assertEquals("grinning_face", parsingResult.get(0).getThird());

        parsingResult = EmoteParser.parse("Hallo Welt :grinning_face:, :grinning_face_with_big_eyes:");

        Assertions.assertEquals(2, parsingResult.size());
        Assertions.assertEquals(11, parsingResult.get(0).getFirst());
        Assertions.assertEquals(25, parsingResult.get(0).getSecond());
        Assertions.assertEquals("grinning_face", parsingResult.get(0).getThird());
        Assertions.assertEquals("grinning_face_with_big_eyes", parsingResult.get(1).getThird());

        parsingResult = EmoteParser.parse("Hallo Welt :grinning_fac:, :grinning_face_with_big_eyes:");

        Assertions.assertEquals(1, parsingResult.size());
        Assertions.assertEquals("grinning_face_with_big_eyes", parsingResult.get(0).getThird());

        parsingResult = EmoteParser.parse(":grinning_fac::grinning_face_with_big_eyes:");
        Assertions.assertEquals(1, parsingResult.size());
    }
}
