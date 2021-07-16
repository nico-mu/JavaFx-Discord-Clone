package de.uniks.stp.network.voice;

import de.uniks.stp.model.Channel;
import de.uniks.stp.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.TargetDataLine;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VoiceChatClient {
    private static final Logger log = LoggerFactory.getLogger(VoiceChatClient.class);

    private final User currentUser;
    private final Channel channel;
    private final VoiceChatService voiceChatService;

    private List<User> filteredUsers;
    private TargetDataLine audioInDataLine;

    public VoiceChatClient(Channel channel, User currentUser, VoiceChatService voiceChatService) {
        this.channel = channel;
        this.currentUser = currentUser;
        this.voiceChatService = voiceChatService;
        withFilteredUsers(currentUser);
    }

    public void init() {
    }

    public void stop() {

    }

    public VoiceChatClient withFilteredUsers(User value) {
        if (this.filteredUsers == null) {
            this.filteredUsers = new ArrayList<>();
        }
        if (!this.filteredUsers.contains(value)) {
            this.filteredUsers.add(value);
        }
        return this;
    }

    public VoiceChatClient withFilteredUsers(User... value) {
        for (final User item : value) {
            this.withFilteredUsers(item);
        }
        return this;
    }

    public VoiceChatClient withFilteredUsers(Collection<? extends User> value) {
        for (final User item : value) {
            this.withFilteredUsers(item);
        }
        return this;
    }

    public VoiceChatClient withoutFilteredUsers(User value) {
        if (this.filteredUsers != null) {
            this.filteredUsers.remove(value);
        }
        return this;
    }

    public VoiceChatClient withoutFilteredUsers(User... value) {
        for (final User item : value) {
            this.withoutFilteredUsers(item);
        }
        return this;
    }

    public VoiceChatClient withoutFilteredUsers(Collection<? extends User> value) {
        for (final User item : value) {
            this.withoutFilteredUsers(item);
        }
        return this;
    }
}
