package de.uniks.stp.emote;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

public class EmoteParserTest {
    @Test
    public void testEmoteParsing() {
        Assertions.assertEquals("\uD83D\uDE00", EmoteParser.getEmoteMapping().get("grinning_face"));

        LinkedList<EmoteParserResult> parsingResult = EmoteParser.parse(":grinning_face:");

        Assertions.assertEquals(1, parsingResult.size());
        Assertions.assertEquals(0, parsingResult.get(0).getStartIndex());
        Assertions.assertEquals(14, parsingResult.get(0).getEndIndex());
        Assertions.assertEquals("grinning_face", parsingResult.get(0).getEmoteName());

        parsingResult = EmoteParser.parse("Hallo Welt :grinning_face:, :grinning_face_with_smiling_eyes:");

        Assertions.assertEquals(2, parsingResult.size());
        Assertions.assertEquals(11, parsingResult.get(0).getStartIndex());
        Assertions.assertEquals(25, parsingResult.get(0).getEndIndex());
        Assertions.assertEquals("grinning_face", parsingResult.get(0).getEmoteName());
        Assertions.assertEquals("grinning_face_with_smiling_eyes", parsingResult.get(1).getEmoteName());

        parsingResult = EmoteParser.parse("Hallo Welt :grinning_fac:, :grinning_face_with_smiling_eyes:");

        Assertions.assertEquals(1, parsingResult.size());
        Assertions.assertEquals("grinning_face_with_smiling_eyes", parsingResult.get(0).getEmoteName());

        parsingResult = EmoteParser.parse(":grinning_fac::grinning_face_with_smiling_eyes:");
        Assertions.assertEquals(1, parsingResult.size());

        parsingResult = EmoteParser.parse(":grinning_face_with_smiling_eyes::grinning_face_with_smiling_eyes:");
        Assertions.assertEquals(2, parsingResult.size());
        Assertions.assertEquals(0, parsingResult.get(0).getStartIndex());
        Assertions.assertEquals(32, parsingResult.get(0).getEndIndex());
        Assertions.assertEquals(33, parsingResult.get(1).getStartIndex());
    }

    @Test
    public void testUnicodeToEmoteNames() {
        String parsingResult = EmoteParser.convertTextWithUnicodeToNames("Hello World \uD83D\uDE00\uD83D\uDE04");
        Assertions.assertEquals("Hello World :grinning_face::smiling_face_with_open_mouth_and_smiling_eyes:", parsingResult);
    }

    @Test
    public void testEmoteAlias() {
        String parsingResult = EmoteParser.convertTextWithAliasToNames("\\:D");
        Assertions.assertEquals(":D", parsingResult);
        parsingResult = EmoteParser.convertTextWithAliasToNames(":D");
        Assertions.assertEquals(":" + EmoteAlias.SMILING_FACE_WITH_OPEN_MOUTH.toString().toLowerCase() + ":", parsingResult);
    }
}
