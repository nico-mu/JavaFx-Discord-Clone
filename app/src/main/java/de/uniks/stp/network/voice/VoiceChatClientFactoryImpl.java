package de.uniks.stp.network.voice;

import de.uniks.stp.model.Channel;
import de.uniks.stp.model.User;

import javax.inject.Named;
import javax.sound.sampled.Mixer;

public class VoiceChatClientFactoryImpl implements VoiceChatClientFactory {

    private final User currentUser;

    public VoiceChatClientFactoryImpl(@Named("currentUser") User currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public VoiceChatClient create(final Channel channel, Mixer speaker, Mixer microphone) {
        return new VoiceChatClient(channel, currentUser, speaker, microphone);
    }
}
