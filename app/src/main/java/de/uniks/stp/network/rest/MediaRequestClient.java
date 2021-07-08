package de.uniks.stp.network.rest;

import de.uniks.stp.Constants;
import de.uniks.stp.component.ChatMessage;
import de.uniks.stp.model.Message;
import de.uniks.stp.util.UrlUtil;
import kong.unirest.*;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;

import javax.inject.Inject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaRequestClient {
    private static final int POOL_SIZE = 4;
    private final ExecutorService executor;
    protected final UnirestInstance instance;

    @Inject
    public MediaRequestClient() {
        executor = Executors.newFixedThreadPool(POOL_SIZE);
        instance = new UnirestInstance(new Config());
    }

    public void stop() {
        executor.shutdownNow();
        instance.shutDown();
    }

    public void loadImage(String content, ChatMessage messageNode, String url) {
        executor.execute(() -> messageNode.addImage(content, url));
    }

    public void loadVideo(String content, ChatMessage messageNode, String url) {
        executor.execute(() -> messageNode.addVideo(content, url));
    }

    public void getMediaInformation(String url, Callback<JsonNode> callback) {
        if (!url.contains(Constants.REST_SERVER_BASE_URL)) {
            HttpRequest<?> req = instance.get(url);
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
        } catch (JSONException ignored) {
        }
        try {
            JSONObject file = (JSONObject) jsonObject.getJSONObject("links").getJSONArray("file").get(0);
            String contentType = file.getString("type");
            if (contentType.startsWith("image")) {
                loadImage(html, messageNode, url);
            } else {
                html = "";
                loadVideo(html, messageNode, url);
            }
        } catch (JSONException e) {
            loadVideo(html, messageNode, url);
        }
    }

    public void handleImgurGiphyResponse(HttpResponse<JsonNode> response, ChatMessage messageNode) {
        JsonNode jsonNode = response.getBody();
        JSONObject jsonObject = jsonNode.getObject();
        String html = jsonObject.getString("html");
        String url = jsonObject.getString("url");
        loadImage(html, messageNode, url);
    }

    private void sendRequest(HttpRequest<?> req, Callback<JsonNode> callback) {
        executor.execute(() -> req.asJsonAsync(callback));
    }

    public void addMedia(Message message, ChatMessage messageNode) {
        for (String url : UrlUtil.extractURLs(message.getMessage())) {
            if (!url.contains("https")) {
                url = url.replace("http", "https");
            }
            if (url.contains("giphy") || url.contains("imgur") || url.contains("gph.is")) {
                getMediaInformation("https://iframe-embed.qwertzuioplmnbvc.workers.dev/parse/" + UrlUtil.encodeURL(url), (msg) -> handleImgurGiphyResponse(msg, messageNode));
            } else {
                getMediaInformation("https://iframe.ly/api/iframely?url=" + url + "&api_key=914710cf5b1fd52a6d415c&html5=1&ssl=1&maxheight=240", (msg) -> handleMediaInformation(msg, messageNode));
            }
        }
    }
}
