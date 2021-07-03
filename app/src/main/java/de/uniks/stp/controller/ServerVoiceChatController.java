package de.uniks.stp.controller;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.StageManager;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.component.VoiceChatUserEntry;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.User;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.sound.sampled.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER + Constants.ROUTE_CHANNEL)
public class ServerVoiceChatController implements ControllerInterface {
    public static final String VOICE_CHANNEL_USER_CONTAINER_ID = "#voice-channel-user-container";
    public static final String VOICE_CHANNEL_USER_SCROLL_CONTAINER_ID = "#voice-channel-user-scroll-container";
    public static final String AUDIO_INPUT_BTN_ID = "#audio-input-btn";
    public static final String AUDIO_OUTPUT_BTN_ID = "#audio-output-btn";
    public static final String HANG_UP_BTN_ID = "#hang-up-btn";
    private static final Logger log = LoggerFactory.getLogger(ServerVoiceChatController.class);
    private static final Image initAudioInputImg = ViewLoader.loadImage("microphone.png");
    private static final Image otherAudioInputImg = ViewLoader.loadImage("microphone-mute.png");
    private static final Image initAudioOutputImg = ViewLoader.loadImage("volume.png");
    private static final Image otherAudioOutputImg = ViewLoader.loadImage("volume-mute.png");
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    private final HashMap<User, VoiceChatUserEntry> userVoiceChatUserHashMap = new HashMap<>();
    private final Channel model;
    private final RestClient restClient;
    private final VBox view;
    private final Editor editor;
    private VBox serverVoiceChatView;
    private FlowPane voiceChannelUserContainer;
    private final PropertyChangeListener audioMembersPropertyChangeListener = this::onAudioMembersPropertyChange;
    private JFXButton hangUpButton;
    private JFXButton audioInputButton;
    private JFXButton audioOutputButton;
    private ImageView audioInputImgView;
    private ImageView audioOutputImgView;

    private Boolean audioInMute = false;
    private TargetDataLine audioInDataLine;
    private Future<?> audioInFuture;

    private Boolean audioOutMute = false;

    public ServerVoiceChatController(VBox view, Editor editor, Channel model) {
        this.view = view;
        this.editor = editor;
        this.model = model;
        restClient = NetworkClientInjector.getRestClient();
    }

    private void onAudioMembersPropertyChange(PropertyChangeEvent propertyChangeEvent) {
        final User oldValue = (User) propertyChangeEvent.getOldValue();
        final User newValue = (User) propertyChangeEvent.getNewValue();

        if (Objects.isNull(oldValue)) {
            userJoined(newValue);
        } else if (Objects.isNull(newValue)) {
            userLeft(oldValue);
        }
    }

    @Override
    public void init() {
        serverVoiceChatView = (VBox) ViewLoader.loadView(Views.SERVER_VOICE_CHAT_SCREEN);
        view.getChildren().add(serverVoiceChatView);

        final ScrollPane voiceChannelUserScrollContainer = (ScrollPane) serverVoiceChatView.lookup(VOICE_CHANNEL_USER_SCROLL_CONTAINER_ID);
        voiceChannelUserContainer = (FlowPane) voiceChannelUserScrollContainer.getContent().lookup(VOICE_CHANNEL_USER_CONTAINER_ID);
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

        model.getAudioMembers().forEach(this::userJoined);
        model.listeners().addPropertyChangeListener(Channel.PROPERTY_AUDIO_MEMBERS, audioMembersPropertyChangeListener);

        restClient.joinAudioChannel(this.model, this::joinAudioChannelCallback);
    }

    private void userLeft(User user) {
        if (Objects.nonNull(user)) {
            Platform.runLater(() -> {
                final VoiceChatUserEntry voiceChatUserEntry = userVoiceChatUserHashMap.remove(user);
                voiceChannelUserContainer.getChildren().remove(voiceChatUserEntry);
            });
        }
    }

    private void userJoined(User user) {
        if (Objects.nonNull(user)) {
            Platform.runLater(() -> {
                final VoiceChatUserEntry voiceChatUserEntry = new VoiceChatUserEntry(user);
                voiceChannelUserContainer.getChildren().add(voiceChatUserEntry);
                userVoiceChatUserHashMap.put(user, voiceChatUserEntry);
            });
        }
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
        // TODO implement
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

        // Join UDP-Voicestream
        initAudio();

        if (Objects.nonNull(audioInFuture)) {
            audioInFuture.cancel(true);
        }
        audioInFuture = executorService.submit(this::recordAndSendAudio);
    }

    private void recordAndSendAudio() {
        if (Objects.isNull(audioInDataLine)) {
            log.error("Audio recording not startet. The dataLine is not set up properly.");
            return;
        }

        final JsonObject metadataObject = Json.createObjectBuilder()
            .add("channel", model.getId())
            .add("name", StageManager.getEditor().getCurrentUserName())
            .build();
        byte[] metadataBytes = metadataObject.toString().getBytes(StandardCharsets.UTF_8);
        if (metadataBytes.length > Constants.AUDIOSTREAM_METADATA_BUFFER_SIZE) {
            log.error("There are more bytes required for metadata than the package allows. Cancelling..");
            return;
        }
        byte[] audioBuf = new byte[Constants.AUDIOSTREAM_METADATA_BUFFER_SIZE + Constants.AUDIOSTREAM_AUDIO_BUFFER_SIZE];
        System.arraycopy(metadataBytes, 0, audioBuf, 0, metadataBytes.length);
        InetAddress address;
        try {
            address = InetAddress.getByName(Constants.AUDIOSTREAM_BASE_URL);
        } catch (UnknownHostException e) {
            log.error("The host address on {} could not be resolved and is unknown.", Constants.AUDIOSTREAM_BASE_URL, e);
            return;
        }
        DatagramSocket audioInDatagramSocket;
        try {
            audioInDatagramSocket = new DatagramSocket();
        } catch (SocketException e) {
            log.error("Failed to initialize the DatagramSocket.", e);
            return;
        }
        while (!audioInMute) {
            audioInDataLine.read(audioBuf, Constants.AUDIOSTREAM_METADATA_BUFFER_SIZE, audioBuf.length);
            final DatagramPacket audioInDatagramPacket = new DatagramPacket(audioBuf, audioBuf.length, address, Constants.AUDIOSTREAM_PORT);
            try {
                audioInDatagramSocket.send(audioInDatagramPacket);
                log.debug("Sent audio packet {}", audioBuf);
            } catch (IOException e) {
                log.error("Failed to send an audio packet.", e);
                return;
            }
        }
        audioInDatagramSocket.close();
    }

    private void initAudio() {
        final AudioFormat audioFormat = new AudioFormat(
            Constants.AUDIOSTREAM_SAMPLE_RATE,
            Constants.AUDIOSTREAM_SAMPLE_SIZE_BITS,
            Constants.AUDIOSTREAM_CHANNEL,
            Constants.AUDIOSTREAM_SIGNED,
            Constants.AUDIOSTREAM_BIG_ENDIAN
        );
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
        if (AudioSystem.isLineSupported(info)) {
            try {
                audioInDataLine = (TargetDataLine) AudioSystem.getLine(info);
                audioInDataLine.open(audioFormat);
                audioInDataLine.start();
            } catch (LineUnavailableException e) {
                log.error("Audio is currently unavailable on this system.");
            }
        } else {
            log.error("Audio is not supported on this system.");
        }
    }

    private void leaveAudioChannelCallback(HttpResponse<JsonNode> response) {
        final String status = response.getBody().getObject().getString("status");
        final String message = response.getBody().getObject().getString("message");
        final JSONObject data = response.getBody().getObject().getJSONObject("data");

        log.debug("{}: {}", status, message);

    }

    @Override
    public void stop() {
        if (Objects.nonNull(audioInFuture)) {
            audioInFuture.cancel(true);
            audioInFuture = null;
        }

        if (Objects.nonNull(audioInDataLine)) {
            audioInDataLine.close();
            audioInDataLine.drain();
            audioInDataLine.stop();
            audioInDataLine = null;
        }
        restClient.leaveAudioChannel(this.model, this::leaveAudioChannelCallback);
        model.listeners().removePropertyChangeListener(Channel.PROPERTY_AUDIO_MEMBERS, audioMembersPropertyChangeListener);

        audioInputButton.setOnMouseClicked(null);
        hangUpButton.setOnMouseClicked(null);
    }
}
