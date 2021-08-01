package de.uniks.stp.network.voice;

import de.uniks.stp.AccordApp;
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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.sound.sampled.Mixer;
import java.util.List;
import static org.mockito.Mockito.doReturn;
import static org.mockito.ArgumentMatchers.any;

public class VoiceChatServiceTest {
    @Mock
    private Mixer speaker1;
    @Mock
    private Mixer speaker2;
    @Mock
    private Mixer microphone1;
    @Mock
    private Mixer microphone2;

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
        doReturn(Mockito.mock(VoiceChatClient.class)).when(voiceChatClientFactory).create(any(), any(), any());

        voiceChatService = Mockito.spy(new VoiceChatService(voiceChatClientFactory, sessionDatabaseService));
    }

    @Test
    public void changeDevicesTest() {
        voiceChatService.setSelectedMicrophone(microphone1);
        Assertions.assertEquals(microphone1, voiceChatService.getSelectedMicrophone());
        voiceChatService.setSelectedSpeaker(speaker1);
        Assertions.assertEquals(speaker1, voiceChatService.getSelectedSpeaker());
        voiceChatService.setSelectedMicrophone(microphone2);
        Assertions.assertEquals(microphone2, voiceChatService.getSelectedMicrophone());
        voiceChatService.setSelectedSpeaker(speaker2);
        Assertions.assertEquals(speaker2, voiceChatService.getSelectedSpeaker());
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
        int inputVolume = voiceChatService.getInputVolume();
        int outputVolume = voiceChatService.getOutputVolume();
        voiceChatService.setInputVolume(inputVolume/2);
        voiceChatService.setOutputVolume(outputVolume/2);

        inputVolume = voiceChatService.getInputVolume();
        outputVolume = voiceChatService.getOutputVolume();
        Assertions.assertEquals(inputVolume/2, inputVolume);
        Assertions.assertEquals(outputVolume/2, outputVolume);
    }

    @AfterEach
    public void tearDown() {
        speaker1 = null;
        speaker2 = null;
        microphone1 = null;
        microphone2 = null;
        sessionDatabaseService.stop();
        sessionDatabaseService = null;
        voiceChatClientFactory = null;
        voiceChatService = null;
    }
}
