package de.uniks.stp.controller;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.component.VoiceChatUserEntry;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.User;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.network.VoiceChatClient;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Objects;

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
    private final HashMap<User, VoiceChatUserEntry> userVoiceChatUserHashMap = new HashMap<>();
    private final Channel model;
    private RestClient restClient;
    private final VBox view;
    private final User currentUser;
    private VBox serverVoiceChatView;
    private FlowPane voiceChannelUserContainer;
    private final PropertyChangeListener audioMembersPropertyChangeListener = this::onAudioMembersPropertyChange;
    private JFXButton hangUpButton;
    private JFXButton audioInputButton;
    private JFXButton audioOutputButton;
    private ImageView audioInputImgView;
    private ImageView audioOutputImgView;

    private final PropertyChangeListener userMutePropertyChangeListener = this::onUserMutePropertyChange;
    private final PropertyChangeListener audioOffPropertyChangeListener = this::onAudioOffPropertyChange;
    private final PropertyChangeListener currentUserMutePropertyChangeListener = this::onCurrentUserMutePropertyChange;

    private VoiceChatClient voiceChatClient;

    private void onCurrentUserMutePropertyChange(PropertyChangeEvent propertyChangeEvent) {
        boolean isMute = (boolean) propertyChangeEvent.getNewValue();
        Image nextImg;
        if (isMute) {
            nextImg = otherAudioInputImg;
        } else {
            nextImg = initAudioInputImg;
        }
        // If ever called outside of the JFX-Thread the setImage must be wrapped in Platform.runLater()
        audioInputImgView.setImage(nextImg);
    }

    private void onAudioOffPropertyChange(PropertyChangeEvent propertyChangeEvent) {
        final boolean isAudioOff = (boolean) propertyChangeEvent.getNewValue();
        Image nextImg;
        if (isAudioOff) {
            nextImg = otherAudioOutputImg;
        } else {
            nextImg = initAudioOutputImg;
        }
        // If ever called outside of the JFX-Thread the setImage must be wrapped in Platform.runLater()
        audioOutputImgView.setImage(nextImg);
    }

    private void onUserMutePropertyChange(PropertyChangeEvent propertyChangeEvent) {
        final User user = (User) propertyChangeEvent.getSource();
        if (!currentUser.equals(user) && !voiceChatClient.isAudioInUnavailable()) {
            boolean isMute = (boolean) propertyChangeEvent.getNewValue();
            final VoiceChatUserEntry voiceChatUserEntry = userVoiceChatUserHashMap.get(user);

            if (isMute) {
                voiceChatClient.withFilteredUsers(user);
            } else {
                voiceChatClient.withoutFilteredUsers(user);
            }
            voiceChatUserEntry.setMute(isMute);
        }
    }

    public ServerVoiceChatController(VBox view, Editor editor, Channel model) {
        this.view = view;
        this.model = model;
        restClient = NetworkClientInjector.getRestClient();
        currentUser = editor.getOrCreateAccord().getCurrentUser();
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
        VBox.setVgrow(serverVoiceChatView, Priority.ALWAYS);
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
        currentUser.listeners().addPropertyChangeListener(User.PROPERTY_MUTE, currentUserMutePropertyChangeListener);
        currentUser.listeners().addPropertyChangeListener(User.PROPERTY_AUDIO_OFF, audioOffPropertyChangeListener);

        restClient.joinAudioChannel(this.model, this::joinAudioChannelCallback);
    }

    private void userLeft(User user) {
        if (Objects.nonNull(user)) {
            final VoiceChatUserEntry voiceChatUserEntry = userVoiceChatUserHashMap.remove(user);
            removeMutePropertyChangeListener(user);
            Platform.runLater(() -> voiceChannelUserContainer.getChildren().remove(voiceChatUserEntry));
        }
    }

    private void userJoined(User user) {
        if (Objects.nonNull(user)) {
            user.listeners().addPropertyChangeListener(User.PROPERTY_MUTE, userMutePropertyChangeListener);
            final VoiceChatUserEntry voiceChatUserEntry = new VoiceChatUserEntry(user);
            userVoiceChatUserHashMap.put(user, voiceChatUserEntry);
            Platform.runLater(() -> voiceChannelUserContainer.getChildren().add(voiceChatUserEntry));
        }
    }

    private void onAudioOutputButtonClick(MouseEvent mouseEvent) {
        log.debug("AudioOutput Button clicked");
        if (!voiceChatClient.isAudioOutUnavailable()) {
            final boolean audioOutMute = currentUser.isAudioOff();
            currentUser.setAudioOff(!audioOutMute);
        }
    }

    private void onHangUpButtonClick(MouseEvent mouseEvent) {
        log.debug("HangUp Button clicked");
        final String serverId = Router.getCurrentArgs().get(":id");
        Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, new RouteArgs().addArgument(":id", serverId));
    }

    private void onAudioInputButtonClick(MouseEvent mouseEvent) {
        log.debug("AudioInput Button clicked");
        if (!voiceChatClient.isAudioInUnavailable()) {
            final boolean audioInMute = currentUser.isMute();
            currentUser.setMute(!audioInMute);
        }
    }

    private void joinAudioChannelCallback(HttpResponse<JsonNode> response) {
        if (response.isSuccess()) {
            // Join UDP-Voicestream
            if (Objects.isNull(voiceChatClient)) {
                voiceChatClient = NetworkClientInjector.getVoiceChatClient(currentUser, model);
            }
            try {
                voiceChatClient.init();
            } catch (UnknownHostException | SocketException e) {
                log.error("Connecting to the DatagramSocket failed.", e);
            }
        } else {
            final String status = response.getBody().getObject().getString("status");
            final String message = response.getBody().getObject().getString("message");

            log.error("Joining the AudioChannel failed.\n{}: {}", status, message);
        }
    }

    private void leaveAudioChannelCallback(HttpResponse<JsonNode> response) {
        if (!response.isSuccess()) {
            final String status = response.getBody().getObject().getString("status");
            final String message = response.getBody().getObject().getString("message");

            log.error("Leaving the AudioChannel failed. Cleaning up continues anyways.\n{}: {}", status, message);
        }
    }

    @Override
    public void stop() {
        log.debug("stop() called");

        if (Objects.nonNull(voiceChatClient)) {
            voiceChatClient.stop();
            voiceChatClient = null;
        }

        if(Objects.nonNull(restClient)) {
            restClient.leaveAudioChannel(this.model, this::leaveAudioChannelCallback);
            restClient = null;
        }
        model.listeners().removePropertyChangeListener(Channel.PROPERTY_AUDIO_MEMBERS, audioMembersPropertyChangeListener);
        model.getAudioMembers().forEach(this::removeMutePropertyChangeListener);
        currentUser.listeners().removePropertyChangeListener(User.PROPERTY_AUDIO_OFF, audioOffPropertyChangeListener);
        currentUser.listeners().removePropertyChangeListener(User.PROPERTY_MUTE, currentUserMutePropertyChangeListener);

        userVoiceChatUserHashMap.clear();
        audioInputButton.setOnMouseClicked(null);
        hangUpButton.setOnMouseClicked(null);
        audioOutputButton.setOnMouseClicked(null);
    }

    private void removeMutePropertyChangeListener(User user) {
        user.listeners().removePropertyChangeListener(User.PROPERTY_MUTE, userMutePropertyChangeListener);
    }
}
