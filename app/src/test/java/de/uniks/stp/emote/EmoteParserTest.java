package de.uniks.stp.emote;

import de.uniks.stp.util.Triple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

public class EmoteParserTest {
    @Test
    public void testEmoteParsing() {
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

        parsingResult = EmoteParser.parse(":grinning_face_with_big_eyes::grinning_face_with_big_eyes:");
        Assertions.assertEquals(2, parsingResult.size());
        Assertions.assertEquals(0, parsingResult.get(0).getFirst());
        Assertions.assertEquals(28, parsingResult.get(0).getSecond());
        Assertions.assertEquals(29, parsingResult.get(1).getFirst());
    }

    @Test
    public void testUnicodeToEmoteNames() {
        String parsingResult = EmoteParser.convertTextWithUnicodeToNames("Hello World \uD83D\uDE00\uD83D\uDE04");
        Assertions.assertEquals("Hello World :grinning_face::smiling_face_with_open_mouth_and_smiling_eyes:", parsingResult);
    }
}
