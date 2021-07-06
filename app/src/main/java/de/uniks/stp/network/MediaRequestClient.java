package de.uniks.stp.network;

import de.uniks.stp.Constants;
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

    public void loadImage(String content, ChatMessage messageNode, String url) {
        executor.submit(() -> messageNode.addImage(content, url));
    }

    public void loadVideo(String content, ChatMessage messageNode, String url) {
        executor.submit(() -> messageNode.addVideo(content, url));
    }

    public void getMediaInformation(String url, Callback<JsonNode> callback) {
        if (!url.contains(Constants.REST_SERVER_BASE_URL)) {
            HttpRequest<?> req = Unirest.get(url);
            sendRequest(req, callback);
        }
    }

    public void handleMediaInformation(HttpResponse<JsonNode> response, ChatMessage messageNode) {
        JsonNode jsonNode = response.getBody();
        JSONObject jsonObject = jsonNode.getObject();
        String html = "";
        String url = "";
        try {
            html = jsonObject.getString("html");
        } catch (JSONException ignored) {
        }
        try {
            url = jsonObject.getString("url");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (jsonObject.getString("url").contains("giphy")) {
            messageNode.addImage("<body style=\"margin:0\"><img src=\"" + jsonObject.getString("url") + "\" style=\"width:200; height:200\"></body>", url);
            return;
        }
        try {
            JSONObject file = (JSONObject) jsonObject.getJSONObject("links").getJSONArray("file").get(0);
            String contentType = file.getString("type");
            if (contentType.startsWith("image")) {
                loadImage(html, messageNode, url);
            } else {
                loadVideo(html, messageNode, url);
            }
        } catch (JSONException e) {
            loadVideo(html, messageNode, url);
        }
    }

    private void sendRequest(HttpRequest<?> req, Callback<JsonNode> callback) {
        executor.execute(() -> req.asJsonAsync(callback));
    }
}

