package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.model.Channel;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import javafx.scene.Parent;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER + Constants.ROUTE_VOICE_CHANNEL)
public class ServerVoiceChatController implements ControllerInterface {
    private static final Logger log = LoggerFactory.getLogger(ServerVoiceChatController.class);

    private final Channel model;
    private final RestClient restClient;
    private final Parent view;
    private final Editor editor;

    public ServerVoiceChatController(Parent view, Editor editor, Channel model) {
        this.view = view;
        this.editor = editor;
        this.model = model;
        this.restClient = NetworkClientInjector.getRestClient();
    }

    @Override
    public void init() {
        restClient.joinAudioChannel(this.model, this::joinAudioChannelCallback);
    }

    private void joinAudioChannelCallback(HttpResponse<JsonNode> response) {
        final String status = response.getBody().getObject().getString("status");
        final String message = response.getBody().getObject().getString("message");
        final JSONObject data = response.getBody().getObject().getJSONObject("data");

        log.debug("{}: {}", status, message);

    }

    private void leaveAudioChannelCallback(HttpResponse<JsonNode> response) {
        final String status = response.getBody().getObject().getString("status");
        final String message = response.getBody().getObject().getString("message");
        final JSONObject data = response.getBody().getObject().getJSONObject("data");

        log.debug("{}: {}", status, message);

    }

    @Override
    public void stop() {
        NetworkClientInjector.getRestClient().leaveAudioChannel(this.model, this::leaveAudioChannelCallback);

    }
}
