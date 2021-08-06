package de.uniks.stp.network.voice;

import de.uniks.stp.AccordApp;
import de.uniks.stp.dagger.components.test.AppTestComponent;
import de.uniks.stp.dagger.components.test.DaggerAppTestComponent;
import de.uniks.stp.dagger.components.test.SessionTestComponent;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.User;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.sound.sampled.Mixer;

public class VoiceChatClientTest {
    @Mock
    private Mixer speaker1;
    @Mock
    private Mixer speaker2;
    @Mock
    private Mixer microphone1;
    @Mock
    private Mixer microphone2;
    private VoiceChatClient voiceChatClient;

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

        voiceChatClient = sessionTestComponent.getVoiceChatClientFactory().create(null, new Channel(), speaker1, microphone1);
    }

    @Test
    public void changeVolumeTest() {
        voiceChatClient.setInputVolume(50);
        voiceChatClient.setOutputVolume(50);
    }

    @Test
    public void changeDevicesTest() {
        voiceChatClient.changeSpeaker(speaker2);
        voiceChatClient.changeMicrophone(microphone2);
    }

    @Test
    public void filterUsersTest() {
        final User otherUser = new User();
        voiceChatClient.withFilteredUsers(otherUser);
        voiceChatClient.withoutFilteredUsers(otherUser);
    }

    @AfterEach
    public void tearDown() {
        speaker1 = null;
        speaker2 = null;
        microphone1 = null;
        microphone2 = null;
        voiceChatClient.stop();
    }
}
