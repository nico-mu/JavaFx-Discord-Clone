package de.uniks.stp.util;

import de.uniks.stp.component.ChatMessage;
import de.uniks.stp.model.Message;
import de.uniks.stp.network.MediaRequestClient;
import de.uniks.stp.network.NetworkClientInjector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class UrlUtil {
    public static List<String> extractURLs(String string) {
        return new ArrayList<>(Arrays.asList(Pattern.compile("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]").matcher(string).results().map(MatchResult::group).toArray(String[]::new)));
    }

    public static void addMedia(Message message, ChatMessage messageNode) {
        MediaRequestClient mediaRequestClient = NetworkClientInjector.getMediaRequestClient();
        for (String url : extractURLs(message.getMessage())) {
            mediaRequestClient.getMediaInformation("https://iframe.ly/api/iframely?url=" + url + "&api_key=914710cf5b1fd52a6d415c&html5=1&ssl=1&maxheight=240", (msg) -> mediaRequestClient.handleMediaInformation(msg, messageNode));
        }
    }
}
