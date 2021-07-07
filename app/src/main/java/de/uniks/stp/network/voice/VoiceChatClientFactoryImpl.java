package de.uniks.stp.network.voice;

import de.uniks.stp.model.Channel;
import de.uniks.stp.model.User;

import javax.inject.Named;

public class VoiceChatClientFactoryImpl implements VoiceChatClientFactory {

    private final User currentUser;

    public VoiceChatClientFactoryImpl(@Named("currentUser") User currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public VoiceChatClient create(final Channel channel) {
        return new VoiceChatClient(channel, currentUser);
    }
}
