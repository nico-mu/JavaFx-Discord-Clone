package de.uniks.stp.minigame;


public enum GameCommand {
    PLAY("!play :handshake:"),
    CHOOSE_ROCK("!choose " + GameAction.ROCK),
    CHOOSE_PAPER("!choose " + GameAction.PAPER),
    CHOOSE_SCISSOR("!choose " + GameAction.SCISSORS),
    REVANCHE("!revanche"),
    LEAVE("!play quit");

    public final String command;

    GameCommand(String command) {
        this.command = command;
    }
}
