package de.uniks.stp.message;

import de.uniks.stp.util.UrlUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;

public class MediaMessageTest {
    @Test
    public void testURLParsing() {
        final String testMessage = "https://www.google.com/ qwer qwe.wda test.com https://www.google.com/ https://www.youtube.com/";
        Assertions.assertEquals(3 ,UrlUtil.extractURLs(testMessage).size());
    }
}

