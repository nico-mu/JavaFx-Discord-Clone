package de.uniks.stp.controller;

import com.jfoenix.controls.JFXButton;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.component.VoiceChatUserEntry;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.User;
import de.uniks.stp.network.rest.SessionRestClient;
import de.uniks.stp.network.voice.VoiceChatService;
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
import java.util.HashMap;
import java.util.Objects;

@Route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER + Constants.ROUTE_CHANNEL)
public class ServerVoiceChatController extends BaseController implements ControllerInterface {
    private final VoiceChatService voiceChatService;
    private VoiceChatUserEntry.VoiceChatUserEntryFactory voiceChatUserEntryFactory;

    public static final String VOICE_CHANNEL_USER_CONTAINER_ID = "#voice-channel-user-container";
    public static final String VOICE_CHANNEL_USER_SCROLL_CONTAINER_ID = "#voice-channel-user-scroll-container";
    public static final String AUDIO_INPUT_BTN_ID = "#audio-input-btn";
    public static final String AUDIO_OUTPUT_BTN_ID = "#audio-output-btn";
    public static final String HANG_UP_BTN_ID = "#hang-up-btn";
    private static final Logger log = LoggerFactory.getLogger(ServerVoiceChatController.class);
    private final ViewLoader viewLoader;
    private final Router router;
    private final SessionRestClient restClient;
    private Image initAudioInputImg;
    private Image otherAudioInputImg;
    private Image initAudioOutputImg;
    private Image otherAudioOutputImg;
    private final HashMap<User, VoiceChatUserEntry> userVoiceChatUserHashMap = new HashMap<>();
    private final Channel model;
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

    @AssistedInject
    public ServerVoiceChatController(@Assisted VBox view,
                                     @Assisted Channel model,
                                     Editor editor,
                                     ViewLoader viewLoader,
                                     Router router,
                                     SessionRestClient restClient,
                                     VoiceChatUserEntry.VoiceChatUserEntryFactory voiceChatUserEntryFactory,
                                     VoiceChatService voiceChatService) {
        this.view = view;
        this.model = model;
        this.viewLoader = viewLoader;
        this.router = router;
        this.restClient = restClient;
        this.voiceChatUserEntryFactory = voiceChatUserEntryFactory;
        this.voiceChatService = voiceChatService;
        currentUser = editor.getOrCreateAccord().getCurrentUser();
    }

    @Override
    public void init() {
        initAudioInputImg = viewLoader.loadImage("microphone.png");
        otherAudioInputImg = viewLoader.loadImage("microphone-mute.png");
        initAudioOutputImg = viewLoader.loadImage("volume.png");
        otherAudioOutputImg = viewLoader.loadImage("volume-mute.png");
        serverVoiceChatView = (VBox) viewLoader.loadView(Views.SERVER_VOICE_CHAT_SCREEN);
        VBox.setVgrow(serverVoiceChatView, Priority.ALWAYS);
        view.getChildren().add(serverVoiceChatView);

        final ScrollPane voiceChannelUserScrollContainer = (ScrollPane) serverVoiceChatView.lookup(VOICE_CHANNEL_USER_SCROLL_CONTAINER_ID);
        voiceChannelUserContainer = (FlowPane) voiceChannelUserScrollContainer.getContent().lookup(VOICE_CHANNEL_USER_CONTAINER_ID);
        audioInputButton = (JFXButton) serverVoiceChatView.lookup(AUDIO_INPUT_BTN_ID);
        audioOutputButton = (JFXButton) serverVoiceChatView.lookup(AUDIO_OUTPUT_BTN_ID);
        hangUpButton = (JFXButton) serverVoiceChatView.lookup(HANG_UP_BTN_ID);

        audioInputImgView = (ImageView) audioInputButton.getGraphic();
        final Image currentInput = currentUser.isMute() ? otherAudioInputImg : initAudioInputImg;
        audioInputImgView.setImage(currentInput);

        audioOutputImgView = (ImageView) audioOutputButton.getGraphic();
        final Image currentOutput = currentUser.isAudioOff() ? otherAudioOutputImg : initAudioOutputImg;
        audioOutputImgView.setImage(currentOutput);

        audioInputButton.setOnMouseClicked(this::onAudioInputButtonClick);
        audioOutputButton.setOnMouseClicked(this::onAudioOutputButtonClick);
        hangUpButton.setOnMouseClicked(this::onHangUpButtonClick);

        model.getAudioMembers().forEach(this::userJoined);
        model.listeners().addPropertyChangeListener(Channel.PROPERTY_AUDIO_MEMBERS, audioMembersPropertyChangeListener);

        currentUser.listeners().addPropertyChangeListener(User.PROPERTY_MUTE, currentUserMutePropertyChangeListener);
        currentUser.listeners().addPropertyChangeListener(User.PROPERTY_AUDIO_OFF, audioOffPropertyChangeListener);

        restClient.joinAudioChannel(this.model, this::joinAudioChannelCallback);
    }

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
        boolean isMute = (boolean) propertyChangeEvent.getNewValue();
        final VoiceChatUserEntry voiceChatUserEntry = userVoiceChatUserHashMap.get(user);
        if (!currentUser.equals(user)) {
            voiceChatUserEntry.setMute(isMute);
        } else if (voiceChatService.isMicrophoneAvailable()) {
            // Current user and audioIn available, ignores change if audioIn is unavailable
            voiceChatUserEntry.setMute(isMute);
        }
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
            final VoiceChatUserEntry voiceChatUserEntry = voiceChatUserEntryFactory.create(user);
            userVoiceChatUserHashMap.put(user, voiceChatUserEntry);
            Platform.runLater(() -> voiceChannelUserContainer.getChildren().add(voiceChatUserEntry));
        }
    }

    private void onAudioOutputButtonClick(MouseEvent mouseEvent) {
        log.debug("AudioOutput Button clicked");
        if (voiceChatService.isSpeakerAvailable()) {
            final boolean audioOutMute = currentUser.isAudioOff();
            currentUser.setAudioOff(!audioOutMute);
        }
    }

    private void onHangUpButtonClick(MouseEvent mouseEvent) {
        log.debug("HangUp Button clicked");
        final String serverId = router.getCurrentArgs().get(":id");
        router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, new RouteArgs().addArgument(":id", serverId));
    }

    private void onAudioInputButtonClick(MouseEvent mouseEvent) {
        log.debug("AudioInput Button clicked");
        if (voiceChatService.isMicrophoneAvailable()) {
            final boolean audioInMute = currentUser.isMute();
            currentUser.setMute(!audioInMute);
        }
    }

    private void joinAudioChannelCallback(HttpResponse<JsonNode> response) {
        if (response.isSuccess()) {
            // Join UDP-Voicestream
            voiceChatService.addVoiceChatClient(model);
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
        super.stop();

        voiceChatService.removeVoiceChatClient(model);

        if (Objects.nonNull(model.getServer())) {
            restClient.leaveAudioChannel(this.model, this::leaveAudioChannelCallback);
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

    @AssistedFactory
    public interface ServerVoiceChatControllerFactory {
        ServerVoiceChatController create(VBox view, Channel channel);
    }
}
