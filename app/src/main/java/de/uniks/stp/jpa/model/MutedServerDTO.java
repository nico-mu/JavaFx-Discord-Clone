package de.uniks.stp.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "MUTED_SERVERS")
public class MutedServerDTO {
    @Id
    @Column(name = "SERVER_ID", nullable = false, unique = true)
    private String serverId;

    @Column(name = "USERNAME", updatable = false)
    private String username;

    public String getServerId() {
        return serverId;
    }

    public MutedServerDTO setServerId(String serverId) {
        this.serverId = serverId;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public MutedServerDTO setUsername(String userName) {
        this.username = userName;
        return this;
    }
}
