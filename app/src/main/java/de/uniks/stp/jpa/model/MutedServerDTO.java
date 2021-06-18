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

    public String getServerId() {
        return serverId;
    }

    public MutedServerDTO setServerId(String serverId) {
        this.serverId = serverId;
        return this;
    }
}
