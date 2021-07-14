package de.uniks.stp.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "MUTED_CHANNELS")
public class MutedChannelDTO {
    @Id
    @Column(name = "CHANNEL_ID", nullable = false, unique = true)
    private String channelId;

    @Column(name = "USERNAME", updatable = false)
    private String username;

    public String getChannelId() {
        return channelId;
    }

    public MutedChannelDTO setChannelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public MutedChannelDTO setUsername(String userName) {
        this.username = userName;
        return this;
    }
}
