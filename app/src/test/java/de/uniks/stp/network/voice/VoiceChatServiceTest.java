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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.sound.sampled.Mixer;
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
