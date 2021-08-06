package de.uniks.stp.network.voice;

import de.uniks.stp.model.Channel;

import javax.sound.sampled.Mixer;

public interface VoiceChatClientFactory {
    VoiceChatClient create(VoiceChatService voiceChatService, Channel channel);
}
