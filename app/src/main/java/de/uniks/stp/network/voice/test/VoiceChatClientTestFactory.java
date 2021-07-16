package de.uniks.stp.network.voice.test;

import de.uniks.stp.model.Channel;
import de.uniks.stp.network.voice.VoiceChatClient;
import de.uniks.stp.network.voice.VoiceChatClientFactory;
import org.mockito.Mockito;

import javax.sound.sampled.Mixer;

public class VoiceChatClientTestFactory implements VoiceChatClientFactory {
    @Override
    public VoiceChatClient create(Channel channel, Mixer speaker, Mixer microphone) {
        return Mockito.mock(VoiceChatClient.class);
    }
}
