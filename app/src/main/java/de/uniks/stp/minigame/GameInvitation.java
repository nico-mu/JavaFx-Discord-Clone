package de.uniks.stp.minigame;

import java.util.Date;

public class GameInvitation {
    public enum State {
        PENDING,
        SENT,
        RECEIVED
    }

    public static final int TIMEOUT = 30 * 1000; // 30 seconds
    private State state = State.PENDING;
    private Long creationTime;

    public long getCreationTime() {
        return creationTime;
    }

    public State getState() {
        return state;
    }

    public boolean hasNotTimeout() {
        return getCreationTime() >= new Date().getTime() - TIMEOUT;
    }

    public boolean isSent() {
        return getState().equals(State.SENT) && hasNotTimeout();
    }

    public boolean isReceived() {
        return getState().equals(State.RECEIVED) && hasNotTimeout();
    }

    public GameInvitation setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public GameInvitation setState(State state) {
        this.state = state;
        return this;
    }

    public void recycle() {
        setState(State.PENDING);
        creationTime = null;
    }
}
