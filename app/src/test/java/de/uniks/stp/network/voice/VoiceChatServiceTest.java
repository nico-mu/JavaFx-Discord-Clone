package de.uniks.stp.network.voice;

import de.uniks.stp.AccordApp;
import de.uniks.stp.Constants;
import de.uniks.stp.dagger.components.test.AppTestComponent;
import de.uniks.stp.dagger.components.test.DaggerAppTestComponent;
import de.uniks.stp.dagger.components.test.SessionTestComponent;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.User;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.sound.sampled.Mixer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

public class VoiceChatServiceTest {
    private SessionDatabaseService sessionDatabaseService;
    private VoiceChatClientFactory voiceChatClientFactory;
    private VoiceChatService voiceChatService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        AppTestComponent appTestComponent = DaggerAppTestComponent
            .builder()
            .application(Mockito.mock(AccordApp.class))
            .primaryStage(Mockito.mock(Stage.class))
            .build();

        User currentUser = new User().setName("test-user1").setId("test-user1-id");

        SessionTestComponent sessionTestComponent = appTestComponent
            .sessionTestComponentBuilder()
            .currentUser(currentUser)
            .userKey("123-45")
            .build();

        sessionDatabaseService = sessionTestComponent.getSessionDatabaseService();
        voiceChatClientFactory = sessionTestComponent.getVoiceChatClientFactory();

        voiceChatService = Mockito.spy(new VoiceChatService(voiceChatClientFactory, sessionDatabaseService));
    }

    @Test
    public void adjustVolumeTest() {
        final ByteBuffer wrap = ByteBuffer
            .allocate(Constants.AUDIOSTREAM_AUDIO_BUFFER_SIZE + Constants.AUDIOSTREAM_METADATA_BUFFER_SIZE)
            .order(ByteOrder.LITTLE_ENDIAN);
        final byte zero = (byte) 0;
        for (int i = 0; i<Constants.AUDIOSTREAM_METADATA_BUFFER_SIZE; i++) {
            wrap.put(zero);
        }
        byte[] sample = {9, 0, 7, 0, 1, 0, -5, -1, -11, -1, -14, -1, -13, -1, -10, -1, -7, -1, -5, -1, -7, -1, -9, -1, -11, -1, -9, -1, -6, -1, -1, -1, 1, 0, 0, 0, -2, -1, -5, -1, -8, -1, -9, -1, -9, -1, -6, -1, -4, -1, -3, -1, -2, -1, 0, 0, 1, 0, 4, 0, 6, 0, 9, 0, 9, 0, 10, 0, 10, 0, 11, 0, 13, 0, 15, 0, 16, 0, 14, 0, 11, 0, 9, 0, 7, 0, 7, 0, 7, 0, 9, 0, 9, 0, 10, 0, 9, 0, 8, 0, 7, 0, 7, 0, 7, 0, 8, 0, 5, 0, 4, 0, 4, 0, 2, 0, 2, 0, 0, 0, -1, -1, 0, 0, 5, 0, 10, 0, 19, 0, 26, 0, 31, 0, 31, 0, 25, 0, 18, 0, 11, 0, 9, 0, 9, 0, 10, 0, 12, 0, 11, 0, 6, 0, 2, 0, 0, 0, 2, 0, 6, 0, 9, 0, 13, 0, 17, 0, 18, 0, 18, 0, 18, 0, 17, 0, 17, 0, 16, 0, 14, 0, 10, 0, 8, 0, 4, 0, 2, 0, 1, 0, 2, 0, 1, 0, 3, 0, 6, 0, 7, 0, 11, 0, 10, 0, 7, 0, 4, 0, 0, 0, -2, -1, -3, -1, -4, -1, -5, -1, -8, -1, -12, -1, -16, -1, -15, -1, -12, -1, -7, -1, 1, 0, 5, 0, 5, 0, 2, 0, -3, -1, -8, -1, -11, -1, -8, -1, -7, -1, -4, -1, -3, -1, -2, -1, -3, -1, -3, -1, -6, -1, -7, -1, -8, -1, -9, -1, -10, -1, -9, -1, -10, -1, -7, -1, -5, -1, -1, -1, 1, 0, -1, -1, -4, -1, -8, -1, -12, -1, -14, -1, -15, -1, -14, -1, -15, -1, -14, -1, -14, -1, -14, -1, -13, -1, -13, -1, -14, -1, -16, -1, -16, -1, -18, -1, -16, -1, -13, -1, -8, -1, -3, -1, 0, 0, 0, 0, -2, -1, -6, -1, -10, -1, -14, -1, -16, -1, -19, -1, -20, -1, -20, -1, -19, -1, -16, -1, -11, -1, -3, -1, 0, 0, 4, 0, 3, 0, 0, 0, -3, -1, -6, -1, -4, -1, -4, -1, -1, -1, 1, 0, 1, 0, -1, -1, -3, -1, -2, -1, -2, -1, 0, 0, 1, 0, 2, 0, -1, -1, -3, -1, -6, -1, -5, -1, -1, -1, 2, 0, 6, 0, 8, 0, 6, 0, 5, 0, 1, 0, 0, 0, 0, 0, 2, 0, 1, 0, 0, 0, -3, -1, -5, -1, -4, -1, -3, -1, 0, 0, 3, 0, 5, 0, 6, 0, 6, 0, 6, 0, 10, 0, 11, 0, 12, 0, 13, 0, 10, 0, 7, 0, 5, 0, 6, 0, 9, 0, 14, 0, 20, 0, 25, 0, 24, 0, 19, 0, 17, 0, 14, 0, 15, 0, 16, 0, 20, 0, 20, 0, 17, 0, 15, 0, 11, 0, 8, 0, 8, 0, 8, 0, 8, 0, 7, 0, 7, 0, 3, 0, 3, 0, 2, 0, 3, 0, 6, 0, 8, 0, 9, 0, 8, 0, 7, 0, 8, 0, 7, 0, 8, 0, 8, 0, 11, 0, 12, 0, 10, 0, 8, 0, 4, 0, 1, 0, -1, -1, -1, -1, 0, 0, 4, 0, 6, 0, 10, 0, 12, 0, 13, 0, 12, 0, 8, 0, 3, 0, 0, 0, -3, -1, -5, -1, -4, -1, -4, -1, -4, -1, -7, -1, -10, -1, -12, -1, -11, -1, -8, -1, -3, -1, 4, 0, 8, 0, 9, 0, 6, 0, -2, -1, -8, -1, -12, -1, -12, -1, -10, -1, -9, -1, -6, -1, -8, -1, -12, -1, -14, -1, -18, -1, -19, -1, -19, -1, -18, -1, -19, -1, -17, -1, -17, -1, -15, -1, -14, -1, -12, -1, -10, -1, -11, -1, -14, -1, -17, -1, -21, -1, -23, -1, -22, -1, -17, -1, -14, -1, -8, -1, -4, -1, -1, -1, 1, 0, 0, 0, -2, -1, -6, -1, -9, -1, -12, -1, -15, -1, -18, -1, -20, -1, -23, -1, -25, -1, -22, -1, -19, -1, -13, -1, -8, -1, -4, -1, -2, -1, -3, -1, -7, -1, -11, -1, -13, -1, -13, -1, -14, -1, -14, -1, -15, -1, -15, -1, -16, -1, -15, -1, -15, -1, -14, -1, -12, -1, -11, -1, -11, -1, -12, -1, -13, -1, -11, -1, -9, -1, -7, -1, -3, -1, -5, -1, -7, -1, -11, -1, -13, -1, -12, -1, -11, -1, -8, -1, -5, -1, -4, -1, -1, -1, -1, -1, 2, 0, 3, 0, 7, 0, 10, 0, 10, 0, 9, 0, 8, 0, 4, 0, 2, 0, -2, -1, -4, -1, -6, -1, -8, -1, -11, -1, -10, -1, -10, -1, -8, -1, -3, -1, 1, 0, 8, 0, 12, 0, 15, 0, 15, 0, 14, 0, 10, 0, 4, 0, -1, -1, -3, -1, -4, -1, -3, -1, 3, 0, 8, 0, 13, 0, 16, 0, 17, 0, 17, 0, 15, 0, 15, 0, 16, 0, 16, 0, 16, 0, 18, 0, 17, 0, 14, 0, 13, 0, 12, 0, 10, 0, 10, 0, 13, 0, 15, 0, 16, 0, 17, 0, 18, 0, 17, 0, 17, 0, 18, 0, 16, 0, 17, 0, 18, 0, 18, 0, 20, 0, 17, 0, 17, 0, 14, 0, 12, 0, 9, 0, 10, 0, 10, 0, 10, 0, 9, 0, 9, 0, 9, 0, 10, 0, 14, 0, 19, 0, 25, 0, 30, 0, 31, 0, 31, 0, 29, 0, 26, 0, 24, 0, 21, 0, 17, 0, 12, 0, 7, 0, 2, 0, 4, 0, 7, 0, 15, 0, 23, 0, 29, 0, 30, 0, 24, 0, 17, 0, 10, 0, 8, 0, 7, 0, 7, 0, 6, 0, 0, 0, -8, -1, -17, -1, -21, -1, -22, -1, -17, -1, -10, -1, -3, -1, 1, 0, 3, 0, 4, 0, 6, 0, 8, 0, 12, 0, 12, 0, 10, 0, 6, 0, 0, 0, -6, -1, -7, -1, -9, -1, -6, -1, -7, -1, -8, -1, -9, -1, -12, -1, -13, -1, -13, -1, -11, -1, -9, -1, -6, -1, -5, -1, -3, -1, -1, -1, -1, -1};
        wrap.put(sample);
        sample = wrap.array();
        byte[] adjustedVolumeSample = voiceChatService.adjustVolume(50, sample);
        byte[] expectedSample = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 1, 0, 0, 0, -1, -1, -2, -1, -3, -1, -3, -1, -2, -1, -1, -1, -1, -1, -1, -1, -2, -1, -2, -1, -2, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -2, -1, -2, -1, -2, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 3, 0, 3, 0, 4, 0, 3, 0, 2, 0, 2, 0, 1, 0, 1, 0, 1, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 1, 0, 1, 0, 1, 0, 2, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 2, 0, 4, 0, 6, 0, 7, 0, 7, 0, 6, 0, 4, 0, 2, 0, 2, 0, 2, 0, 2, 0, 3, 0, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 2, 0, 3, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 3, 0, 2, 0, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 2, 0, 2, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -2, -1, -3, -1, -4, -1, -3, -1, -3, -1, -1, -1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, -2, -1, -2, -1, -2, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -2, -1, -2, -1, -2, -1, -2, -1, -2, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, -1, -1, -2, -1, -3, -1, -3, -1, -3, -1, -3, -1, -3, -1, -3, -1, -3, -1, -3, -1, -3, -1, -3, -1, -3, -1, -4, -1, -4, -1, -4, -1, -4, -1, -3, -1, -2, -1, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -2, -1, -3, -1, -4, -1, -4, -1, -5, -1, -5, -1, -4, -1, -4, -1, -2, -1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, 0, 0, 0, 0, 1, 0, 2, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 2, 0, 2, 0, 3, 0, 3, 0, 2, 0, 1, 0, 1, 0, 1, 0, 2, 0, 3, 0, 5, 0, 6, 0, 6, 0, 4, 0, 4, 0, 3, 0, 3, 0, 4, 0, 5, 0, 5, 0, 4, 0, 3, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 2, 0, 2, 0, 2, 0, 1, 0, 2, 0, 1, 0, 2, 0, 2, 0, 2, 0, 3, 0, 2, 0, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 2, 0, 3, 0, 3, 0, 3, 0, 2, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2, -1, -3, -1, -2, -1, -2, -1, 0, 0, 1, 0, 2, 0, 2, 0, 1, 0, 0, 0, -2, -1, -3, -1, -3, -1, -2, -1, -2, -1, -1, -1, -2, -1, -3, -1, -3, -1, -4, -1, -4, -1, -4, -1, -4, -1, -4, -1, -4, -1, -4, -1, -3, -1, -3, -1, -3, -1, -2, -1, -2, -1, -3, -1, -4, -1, -5, -1, -5, -1, -5, -1, -4, -1, -3, -1, -2, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -2, -1, -3, -1, -3, -1, -4, -1, -5, -1, -5, -1, -6, -1, -5, -1, -4, -1, -3, -1, -2, -1, -1, -1, 0, 0, 0, 0, -1, -1, -2, -1, -3, -1, -3, -1, -3, -1, -3, -1, -3, -1, -3, -1, -4, -1, -3, -1, -3, -1, -3, -1, -3, -1, -2, -1, -2, -1, -3, -1, -3, -1, -2, -1, -2, -1, -1, -1, 0, 0, -1, -1, -1, -1, -2, -1, -3, -1, -3, -1, -2, -1, -2, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 2, 0, 2, 0, 2, 0, 2, 0, 1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -2, -1, -2, -1, -2, -1, -2, -1, -2, -1, 0, 0, 0, 0, 2, 0, 3, 0, 3, 0, 3, 0, 3, 0, 2, 0, 1, 0, 0, 0, 0, 0, -1, -1, 0, 0, 0, 0, 2, 0, 3, 0, 4, 0, 4, 0, 4, 0, 3, 0, 3, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 3, 0, 3, 0, 3, 0, 2, 0, 2, 0, 3, 0, 3, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 4, 0, 5, 0, 4, 0, 4, 0, 3, 0, 3, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 0, 3, 0, 4, 0, 6, 0, 7, 0, 7, 0, 7, 0, 7, 0, 6, 0, 6, 0, 5, 0, 4, 0, 3, 0, 1, 0, 0, 0, 1, 0, 1, 0, 3, 0, 5, 0, 7, 0, 7, 0, 6, 0, 4, 0, 2, 0, 2, 0, 1, 0, 1, 0, 1, 0, 0, 0, -2, -1, -4, -1, -5, -1, -5, -1, -4, -1, -2, -1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 2, 0, 3, 0, 3, 0, 2, 0, 1, 0, 0, 0, -1, -1, -1, -1, -2, -1, -1, -1, -1, -1, -2, -1, -2, -1, -3, -1, -3, -1, -3, -1, -2, -1, -2, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0};
        Assertions.assertEquals(0, Arrays.compare(expectedSample, adjustedVolumeSample));
    }

    @Test
    public void availableDevicesTest() {
        List<Mixer> mixers = voiceChatService.getAvailableSpeakers();
        boolean mixerAvailable = voiceChatService.isSpeakerAvailable();
        if (mixers.isEmpty()) {
            Assertions.assertFalse(mixerAvailable);
        } else {
            Assertions.assertTrue(mixerAvailable);
        }

        mixers = voiceChatService.getAvailableMicrophones();
        mixerAvailable = voiceChatService.isMicrophoneAvailable();
        if (mixers.isEmpty()) {
            Assertions.assertFalse(mixerAvailable);
        } else {
            Assertions.assertTrue(mixerAvailable);
        }
    }

    @Test
    public void voiceChatClientTest() {
        final Channel channel = new Channel();
        voiceChatService.addVoiceChatClient(channel);
        voiceChatService.removeVoiceChatClient(channel);
    }

    @Test
    public void changeVolumesTest() {
        final int inputVolume = voiceChatService.getInputVolume();
        final int outputVolume = voiceChatService.getOutputVolume();
        voiceChatService.setInputVolume(inputVolume / 2);
        voiceChatService.setOutputVolume(outputVolume / 2);

        Assertions.assertEquals(inputVolume / 2, voiceChatService.getInputVolume());
        Assertions.assertEquals(outputVolume / 2, voiceChatService.getOutputVolume());
    }

    @AfterEach
    public void tearDown() {
        sessionDatabaseService.stop();
        sessionDatabaseService = null;
        voiceChatClientFactory = null;
        voiceChatService = null;
    }
}
