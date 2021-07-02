package de.uniks.stp.util;

import de.uniks.stp.component.ChatMessage;
import de.uniks.stp.model.Message;
import de.uniks.stp.network.NetworkClientInjector;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class UrlUtil {

    public static URL createURL(String URL) {
        try {
            URL u = new URL(URL); // this would check for the protocol
            u.toURI(); // does the extra checking required for validation of URI
            return u;
        } catch (MalformedURLException | URISyntaxException e) {
            return null;
        }
    }

    public static String getContentType(URL url) {
        URLConnection connection;
        try {
            connection = url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return connection.getHeaderField("Content-Type");
    }

    public static List<String> extractURLs(String string) {
        return new ArrayList<>(Arrays.asList(Pattern.compile("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]").matcher(string).results().map(MatchResult::group).toArray(String[]::new)));
    }

    public static void addMedia(Message message, ChatMessage messageNode) {
        for (String url : UrlUtil.extractURLs(message.getMessage())) {
            String contentType = UrlUtil.getContentType(Objects.requireNonNull(createURL(url)));
            if (Objects.isNull(contentType)) {
                continue;
            }
            if (contentType.startsWith("image/")) {
                NetworkClientInjector.getMediaRequestClient().addImage(url, messageNode);
            } else if (contentType.startsWith("video/")) {
                // TODO: add video
            }
        }
    }
}
