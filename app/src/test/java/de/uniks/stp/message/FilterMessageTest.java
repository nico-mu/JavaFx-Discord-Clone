package de.uniks.stp.message;

import de.uniks.stp.util.MessageUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FilterMessageTest {
    @Test
    public void filterMessages() {
        String[] samples = {
            "###quoteInit###repliedToText123###quoteMessage###sendMessage245234[###messageId243525###repliedToId314235][###235233452###2143423532]###quoteStop###",
            "!hangman testInput 123",
            "!guess testInput 456",
            "!stop test 123",
            "!imagebot string 987",
            "!randomimage test 134",
            "!tictactoe143254",
            "!tictactoe 12324",
            "###quoteInit###repliedToText123###quoteMessage###!hangman test @@@123[###messageId243525###repliedToId314235][###235233452###2143423532]###quoteStop###"
        };
        String[] expectedResults = {
            "sendMessage245234",
            "testInput 123",
            "testInput 456",
            "test 123",
            "string 987",
            "test 134",
            "!tictactoe143254",
            "12324",
            "test @@@123"
        };
        for (int index = 0; index < samples.length; ++index) {
            Assertions.assertEquals(expectedResults[index], MessageUtil.filterContent(samples[index]));
        }
    }
}
