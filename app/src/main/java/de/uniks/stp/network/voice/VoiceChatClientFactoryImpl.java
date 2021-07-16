package de.uniks.stp.network.voice;

import de.uniks.stp.model.Channel;
import de.uniks.stp.model.User;

import javax.inject.Named;

public class VoiceChatClientFactoryImpl implements VoiceChatClientFactory {

    private final User currentUser;
    private final VoiceChatService voiceChatService;

    public VoiceChatClientFactoryImpl(@Named("currentUser") User currentUser, VoiceChatService voiceChatService) {
        this.currentUser = currentUser;
        this.voiceChatService = voiceChatService;
    }

    @Override
    public VoiceChatClient create(final Channel channel) {
        return new VoiceChatClient(channel, currentUser, voiceChatService);
    }
}
