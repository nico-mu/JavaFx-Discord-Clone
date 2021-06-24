package de.uniks.stp.util;

public class InviteInfo {
    String serverId;
    String inviteId;

    public InviteInfo setServerId(String serverId) {
        this.serverId = serverId;
        return this;
    }

    public String getServerId() {
        return serverId;
    }

    public InviteInfo setInviteId(String inviteId) {
        this.inviteId = inviteId;
        return this;
    }

    public String getInviteId() {
        return inviteId;
    }
}
