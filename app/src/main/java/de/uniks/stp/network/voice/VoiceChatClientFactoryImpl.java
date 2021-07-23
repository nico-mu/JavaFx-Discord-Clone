package de.uniks.stp.network.voice;

import de.uniks.stp.AudioService;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.User;

import javax.inject.Named;
import javax.sound.sampled.Mixer;

public class VoiceChatClientFactoryImpl implements VoiceChatClientFactory {

    private final User currentUser;
    private final AudioService audioService;

    public VoiceChatClientFactoryImpl(@Named("currentUser") User currentUser, AudioService audioService) {
        this.currentUser = currentUser;
        this.audioService = audioService;
    }

    @Override
    public VoiceChatClient create(final Channel channel, Mixer speaker, Mixer microphone) {
        return new VoiceChatClient(channel, currentUser, speaker, microphone, audioService);
    }
}
