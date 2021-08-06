package de.uniks.stp.network.voice.test;

import de.uniks.stp.model.Channel;
import de.uniks.stp.network.voice.VoiceChatClient;
import de.uniks.stp.network.voice.VoiceChatClientFactory;
import de.uniks.stp.network.voice.VoiceChatService;
import org.mockito.Mockito;

import javax.sound.sampled.Mixer;

public class VoiceChatClientTestFactory implements VoiceChatClientFactory {
    @Override
    public VoiceChatClient create(VoiceChatService voiceChatService, Channel channel) {
        return Mockito.mock(VoiceChatClient.class);
    }
}
