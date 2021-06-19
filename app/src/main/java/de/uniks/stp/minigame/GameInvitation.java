package de.uniks.stp.minigame;

import java.util.Date;

public class GameInvitation {
    public final int TIMEOUT = 30 * 1000; // 30 seconds
    private GameInvitationState state = GameInvitationState.PENDING;
    private Long creationTime;

    public long getCreationTime() {
        return creationTime;
    }

    public GameInvitationState getState() {
        return state;
    }

    public boolean hasNotTimeout() {
        return getCreationTime() >= new Date().getTime() - TIMEOUT;
    }

    public boolean isSent() {
        return getState().equals(GameInvitationState.SENT) && hasNotTimeout();
    }

    public boolean isReceived() {
        return getState().equals(GameInvitationState.RECEIVED) && hasNotTimeout();
    }

    public GameInvitation setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public GameInvitation setState(GameInvitationState state) {
        this.state = state;
        return this;
    }

    public void recycle() {
        setState(GameInvitationState.PENDING);
        creationTime = null;
    }
}
