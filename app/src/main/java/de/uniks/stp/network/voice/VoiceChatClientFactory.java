package de.uniks.stp.network.voice;

import de.uniks.stp.model.Channel;

public interface VoiceChatClientFactory {
    VoiceChatClient create(Channel channel);
}
