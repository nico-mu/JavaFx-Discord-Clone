package de.uniks.stp.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXMasonryPane;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.model.Channel;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.view.Views;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER + Constants.ROUTE_VOICE_CHANNEL)
public class ServerVoiceChatController implements ControllerInterface {
    private static final Logger log = LoggerFactory.getLogger(ServerVoiceChatController.class);

    private static final String VOICE_CHANNEL_USER_CONTAINER_ID = "#voice-channel-user-container";
    private static final String AUDIO_INPUT_BTN_ID = "#audio-input-btn";
    private static final String AUDIO_OUTPUT_BTN_ID = "#audio-output-btn";
    private static final String HANG_UP_BTN_ID = "#hang-up-btn";
    private static final String AUDIO_INPUT_IMG_ID = "#audio-input-img";
    private static final String AUDIO_OUTPUT_IMG_ID = "#audio-output-img";

    private final Channel model;
    private final RestClient restClient;
    private final VBox view;
    private final Editor editor;
    private final Image initAudioInputImg;
    private final Image otherAudioInputImg;
    private final Image initAudioOutputImg;
    private final Image otherAudioOutputImg;
    private VBox serverVoiceChatView;
    private JFXMasonryPane voiceChannelUserContainer;
    private JFXButton hangUpButton;
    private JFXButton audioInputButton;
    private JFXButton audioOutputButton;
    private ImageView audioInputImgView;
    private ImageView audioOutputImgView;
    private Image nextAudioInputImg;
    private boolean audioInMute = false;
    private boolean audioOutMute = false;

    public ServerVoiceChatController(VBox view, Editor editor, Channel model) {
        this.view = view;
        this.editor = editor;
        this.model = model;
        restClient = NetworkClientInjector.getRestClient();

        initAudioInputImg = ViewLoader.loadImage("microphone.png");
        otherAudioInputImg = ViewLoader.loadImage("microphone-mute.png");

        initAudioOutputImg = ViewLoader.loadImage("volume.png");
        otherAudioOutputImg = ViewLoader.loadImage("volume-mute.png");
    }

    @Override
    public void init() {
        serverVoiceChatView = (VBox) ViewLoader.loadView(Views.SERVER_VOICE_CHAT_SCREEN);
        view.getChildren().add(serverVoiceChatView);

        voiceChannelUserContainer = (JFXMasonryPane) serverVoiceChatView.lookup(VOICE_CHANNEL_USER_CONTAINER_ID);
        audioInputButton = (JFXButton) serverVoiceChatView.lookup(AUDIO_INPUT_BTN_ID);
        audioOutputButton = (JFXButton) serverVoiceChatView.lookup(AUDIO_OUTPUT_BTN_ID);
        hangUpButton = (JFXButton) serverVoiceChatView.lookup(HANG_UP_BTN_ID);

        audioInputImgView = (ImageView) audioInputButton.getGraphic();
        audioInputImgView.setImage(initAudioInputImg);

        audioOutputImgView = (ImageView) audioOutputButton.getGraphic();
        audioOutputImgView.setImage(initAudioOutputImg);

        audioInputButton.setOnMouseClicked(this::onAudioInputButtonClick);
        audioOutputButton.setOnMouseClicked(this::onAudioOutputButtonClick);
        hangUpButton.setOnMouseClicked(this::onHangUpButtonClick);

        restClient.joinAudioChannel(this.model, this::joinAudioChannelCallback);
    }

    private void onAudioOutputButtonClick(MouseEvent mouseEvent) {
        log.debug("AudioOutput Button clicked");
        audioOutMute = !audioOutMute;
        Image nextImg;
        if (audioOutMute) {
            nextImg = otherAudioOutputImg;
        } else {
            nextImg = initAudioOutputImg;
        }
        audioOutputImgView.setImage(nextImg);
    }

    private void onHangUpButtonClick(MouseEvent mouseEvent) {
        log.debug("HangUp Button clicked");
        // TODO implement
    }

    private void onAudioInputButtonClick(MouseEvent mouseEvent) {
        log.debug("AudioInput Button clicked");
        audioInMute = !audioInMute;
        Image nextImg;
        if (audioInMute) {
            nextImg = otherAudioInputImg;
        } else {
            nextImg = initAudioInputImg;
        }
        audioInputImgView.setImage(nextImg);
        // TODO implement
    }

    private void joinAudioChannelCallback(HttpResponse<JsonNode> response) {
        final String status = response.getBody().getObject().getString("status");
        final String message = response.getBody().getObject().getString("message");
        final JSONObject data = response.getBody().getObject().getJSONObject("data");

        log.debug("{}: {}", status, message);
        // TODO join UDP-Voicestream

    }

    private void leaveAudioChannelCallback(HttpResponse<JsonNode> response) {
        final String status = response.getBody().getObject().getString("status");
        final String message = response.getBody().getObject().getString("message");
        final JSONObject data = response.getBody().getObject().getJSONObject("data");

        log.debug("{}: {}", status, message);

    }

    @Override
    public void stop() {
        restClient.leaveAudioChannel(this.model, this::leaveAudioChannelCallback);

        audioInputButton.setOnMouseClicked(null);
        hangUpButton.setOnMouseClicked(null);
    }
}
