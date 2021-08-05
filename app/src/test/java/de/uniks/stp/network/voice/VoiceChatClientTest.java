package de.uniks.stp.network.voice;

import de.uniks.stp.model.Channel;
import de.uniks.stp.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

public class VoiceChatClientTest {
    @Mock
    private VoiceChatService voiceChatService;
    @Mock
    private DatagramSocket datagramSocket;
    @Mock
    private InetAddress address;
    private VoiceChatClient voiceChatClient;
    private Channel channel;
    private User currentUser;
    private User otherUser;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        currentUser = new User().setName("test-user1").setId("test-user1-id");
        otherUser = new User().setId("abc").setName("user1");

        channel = new Channel().setId("channelId").withAudioMembers(otherUser);

        voiceChatClient = new VoiceChatClient(voiceChatService, channel, currentUser)
            .setDatagramSocket(datagramSocket)
            .setAddress(address);
    }

    @Test
    public void filterUsersTest() {
        final User otherUser = new User();
        voiceChatClient.withFilteredUsers(otherUser);
        voiceChatClient.withoutFilteredUsers(otherUser);
    }

    @Test
    public void userUnMuteUnAudioTest() {
        SourceDataLine sourceDataLineMock = Mockito.mock(SourceDataLine.class);
        TargetDataLine targetDataLineMock = Mockito.mock(TargetDataLine.class);
        try {
            doReturn(sourceDataLineMock).when(voiceChatService).createUsableSourceDataLine();
            doReturn(targetDataLineMock).when(voiceChatService).createUsableTargetDataLine();
        } catch (LineUnavailableException ignored) { }
        doReturn(true).when(voiceChatService).isMicrophoneAvailable();
        doReturn(true).when(voiceChatService).isSpeakerAvailable();
        currentUser.setMute(true);
        currentUser.setAudioOff(true);
        otherUser.setMute(true);

        voiceChatClient.init();

        currentUser.setMute(false);
        currentUser.setAudioOff(false);
        otherUser.setMute(false);

        currentUser.setMute(true);
        currentUser.setAudioOff(true);
        otherUser.setMute(true);
        try {
            Thread.sleep(3*1000); // receiveAndPlay & recordAndSend executables need some time
        } catch (InterruptedException ignored) { }
    }

    @Test
    public void otherUserTest() {
        SourceDataLine sourceDataLineMock = Mockito.mock(SourceDataLine.class);
        try {
            doReturn(sourceDataLineMock).when(voiceChatService).createUsableSourceDataLine();
        } catch (LineUnavailableException ignored) { }
        doReturn(true).when(voiceChatService).isSpeakerAvailable();
        voiceChatClient.init();

        channel.withoutAudioMembers(otherUser);
        channel.withAudioMembers(otherUser);
    }

    @Test
    public void noLinesAvailableTest() {
        try {
            doThrow(LineUnavailableException.class).when(voiceChatService).createUsableSourceDataLine();
            doThrow(LineUnavailableException.class).when(voiceChatService).createUsableTargetDataLine();
        } catch (LineUnavailableException ignored) { }
        doReturn(true).when(voiceChatService).isMicrophoneAvailable();
        doReturn(true).when(voiceChatService).isSpeakerAvailable();
        voiceChatClient.init();

        final User user1 = new User().setId("abc").setName("user1");
        channel.withAudioMembers(user1);
        channel.withoutAudioMembers(user1);

        voiceChatClient.onMicrophoneChanged();
        voiceChatClient.onSpeakerChanged();
    }

    @Test
    public void devicesChangeTest() {
        SourceDataLine sourceDataLineMock = Mockito.mock(SourceDataLine.class);
        TargetDataLine targetDataLineMock = Mockito.mock(TargetDataLine.class);
        try {
            doReturn(sourceDataLineMock).when(voiceChatService).createUsableSourceDataLine();
            doReturn(targetDataLineMock).when(voiceChatService).createUsableTargetDataLine();
        } catch (LineUnavailableException ignored) { }
        doReturn(true).when(voiceChatService).isMicrophoneAvailable();
        doReturn(true).when(voiceChatService).isSpeakerAvailable();

        voiceChatClient.init();
        voiceChatClient.onMicrophoneChanged();
        voiceChatClient.onSpeakerChanged();

    }

    @AfterEach
    public void tearDown() {
        voiceChatClient.stop();
        voiceChatService = null;
        datagramSocket = null;
        address = null;
        voiceChatClient = null;
        channel = null;
        currentUser = null;
    }
}
