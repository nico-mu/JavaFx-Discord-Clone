package de.uniks.stp.network.rest;

import de.uniks.stp.component.ChatMessage;
import de.uniks.stp.model.Message;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import javax.inject.Inject;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class MediaRequestClientTest {

    @Spy
    private MediaRequestClient mediaRequestClientSpy;

    @Inject
    private ChatMessage chatMessage;

    @Mock
    private HttpResponse<JsonNode> res;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        doNothing().when(mediaRequestClientSpy).sendRequest(any(), any());
        doNothing().when(mediaRequestClientSpy).loadVideo(any(), any(), any());
        doNothing().when(mediaRequestClientSpy).loadImage(any(), any(), any());
    }

    @Test
    public void httpTest() {
        Message message = new Message().setMessage("http://www.youtube.com/watch?v=1IkXwOOgN8U");
        mediaRequestClientSpy.addMedia(message, chatMessage);
    }

    @Test
    public void youtubeTest() {
        Message message = new Message().setMessage("https://www.youtube.com/watch?v=1IkXwOOgN8U");
        mediaRequestClientSpy.addMedia(message, chatMessage);
    }

    @Test
    public void imgurTest() {
        Message message = new Message().setMessage("https://imgur.com/gallery/BMSSPFJ");
        mediaRequestClientSpy.addMedia(message, chatMessage);
    }

    @Test
    public void handleImgurGiphyResponseTest() {
        JSONObject j = new JSONObject()
            .put("url", "testUrl")
            .put("html", "testHtml");
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);

        mediaRequestClientSpy.handleImgurGiphyResponse(res, chatMessage);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void handleHtmlImageTest() {
        JSONObject j = new JSONObject()
            .put("url", "testImage.png")
            .put("html", "testHtml");
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);

        mediaRequestClientSpy.handleMediaInformation(res, chatMessage);
    }

    @Test
    public void handleHtmlVideoTest() {
        JSONObject j = new JSONObject()
            .put("url", "testVideo.mp4")
            .put("html", "testHtml");
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);

        mediaRequestClientSpy.handleMediaInformation(res, chatMessage);
    }

    @Test
    public void handleImageTest() {
        JSONObject j = new JSONObject()
            .put("url", "testImage.png")
            .put("links", new JSONObject()
                .put("file", new JSONArray().put(new JSONObject()
                    .put("type", "image/png"))));
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);

        mediaRequestClientSpy.handleMediaInformation(res, chatMessage);
    }

    @Test
    public void handleVideoTest() {
        JSONObject j = new JSONObject()
            .put("url", "testImage.png")
            .put("links", new JSONObject()
                .put("file", new JSONArray().put(new JSONObject()
                    .put("type", "video/mp4"))));
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));
        when(res.isSuccess()).thenReturn(true);

        mediaRequestClientSpy.handleMediaInformation(res, chatMessage);
    }

    @AfterEach
    public void tearDown() {
        mediaRequestClientSpy.stop();
        mediaRequestClientSpy = null;
        chatMessage = null;
        res = null;
    }
}
