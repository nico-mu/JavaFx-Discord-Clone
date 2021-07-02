package de.uniks.stp.util;

import de.uniks.stp.Constants;
import de.uniks.stp.component.ChatMessage;
import de.uniks.stp.model.Message;
import de.uniks.stp.network.NetworkClientInjector;
import kong.unirest.*;
import kong.unirest.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class UrlUtil {
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public URL createURL(String URL) {
        try {
            URL u = new URL(URL); // this would check for the protocol
            u.toURI(); // does the extra checking required for validation of URI
            return u;
        } catch (MalformedURLException | URISyntaxException e) {
            return null;
        }
    }

    public String getContentType(URL url) {
        URLConnection connection;
        try {
            connection = url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return connection.getContentType();
    }

    public List<String> extractURLs(String string) {
        return new ArrayList<>(Arrays.asList(Pattern.compile("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]").matcher(string).results().map(MatchResult::group).toArray(String[]::new)));
    }

    public void addMedia(Message message, ChatMessage messageNode) {
        for (String url : extractURLs(message.getMessage())) {
            getMediaInformation("https://iframe.ly/api/iframely?url=" + url + "&api_key=914710cf5b1fd52a6d415c&html5=1&ssl=1", (msg) -> handleMediaInformation(msg, messageNode));
            /*
            String contentType = UrlUtil.getContentType(Objects.requireNonNull(createURL(url)));
            if(url.startsWith("https://www.youtube.com/")) {
                String embedUrl;
                if(url.contains("watch?v=")) {
                    embedUrl = url.replace("watch?v=", "embed/");
                }else {
                    embedUrl = url.replace("https://www.youtube.com/", "https://www.youtube.com/embed/");
                }
                NetworkClientInjector.getMediaRequestClient().addYouTubeVideo(url, messageNode);
            }
            if (Objects.isNull(contentType)) {
                continue;
            }
            if (contentType.startsWith("image/") || message.getMessage().endsWith(".gif") || message.getMessage().endsWith(".png") || message.getMessage().endsWith(".jpg")) {
                NetworkClientInjector.getMediaRequestClient().addImage(url, messageNode);
            } else if (contentType.startsWith("video/") || contentType.equals("application/octet-stream")) {
                NetworkClientInjector.getMediaRequestClient().addVideo(url, contentType, messageNode);
            }*/
        }
    }

    private void handleMediaInformation(HttpResponse<JsonNode> response, ChatMessage messageNode) {
        JsonNode jsonNode = response.getBody();
        JSONObject jsonObject = jsonNode.getObject();
        String html = jsonObject.getString("html");
        NetworkClientInjector.getMediaRequestClient().loadContent(html, messageNode);
    }

    private void getMediaInformation(String url, Callback<JsonNode> callback) {
        HttpRequest<?> req = Unirest.get(url);
        sendRequest(req, callback);
    }

    private void sendRequest(HttpRequest<?> req, Callback<JsonNode> callback) {
        executorService.execute(() -> req.asJsonAsync(callback));
    }
}
