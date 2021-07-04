package de.uniks.stp.network;

import de.uniks.stp.component.ChatMessage;
import de.uniks.stp.util.UrlUtil;
import kong.unirest.*;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaRequestClient {
    private static final int POOL_SIZE = 4;
    private final ExecutorService executor;

    public MediaRequestClient() {
        executor = Executors.newFixedThreadPool(POOL_SIZE);
    }

    public void stop() {
        executor.shutdown();
    }

    public void loadImage(String content, ChatMessage messageNode) {
        executor.submit(() -> messageNode.addImage(content));
    }

    public void loadVideo(String content, ChatMessage messageNode) {
        executor.submit(() -> messageNode.addVideo(content));
    }

    public void getMediaInformation(String url, Callback<JsonNode> callback) {
        HttpRequest<?> req = Unirest.get(url);
        sendRequest(req, callback);
    }

    public void handleMediaInformation(HttpResponse<JsonNode> response, ChatMessage messageNode) {
        JsonNode jsonNode = response.getBody();
        JSONObject jsonObject = jsonNode.getObject();
        String html = jsonObject.getString("html");
        try {
            JSONObject file = (JSONObject) jsonObject.getJSONObject("links").getJSONArray("file").get(0);
            String contentType = file.getString("type");
            if (contentType.startsWith("image")) {
                loadImage(html, messageNode);
            } else {
                loadVideo(html, messageNode);
            }
        } catch (JSONException e) {
            loadVideo(html, messageNode);
        }
    }

    private void sendRequest(HttpRequest<?> req, Callback<JsonNode> callback) {
        executor.execute(() -> req.asJsonAsync(callback));
    }
}

