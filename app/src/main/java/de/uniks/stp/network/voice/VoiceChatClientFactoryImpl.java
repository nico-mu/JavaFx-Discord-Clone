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
    public VoiceChatClient create(VoiceChatService voiceChatService, final Channel channel) {
        return new VoiceChatClient(voiceChatService, channel, currentUser);
    }
}
