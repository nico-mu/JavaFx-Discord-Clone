package de.uniks.stp.network.voice;

import de.uniks.stp.model.Channel;

import javax.sound.sampled.Mixer;

public interface VoiceChatClientFactory {
    VoiceChatClient create(Channel channel, Mixer speaker, Mixer microphone);
}
