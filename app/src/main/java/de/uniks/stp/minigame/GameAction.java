package de.uniks.stp.minigame;

public enum GameAction {
    ROCK("rock"),
    PAPER("paper"),
    SCISSORS("scissors");

    public final String action;

    GameAction(String action) {
        this.action = action;
    }
}
