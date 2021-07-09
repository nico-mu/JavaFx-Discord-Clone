package de.uniks.stp.network.voice.test;

import de.uniks.stp.model.Channel;
import de.uniks.stp.network.voice.VoiceChatClient;
import de.uniks.stp.network.voice.VoiceChatClientFactory;
import org.mockito.Mockito;

public class VoiceChatClientTestFactory implements VoiceChatClientFactory {
    @Override
    public VoiceChatClient create(Channel channel) {
        return Mockito.mock(VoiceChatClient.class);
    }
}
