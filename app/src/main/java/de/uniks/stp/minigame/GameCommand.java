package de.uniks.stp.minigame;

public enum GameCommand {
    PLAY("!play :handshake:"),
    CHOOSE_ROCK("!choose rock"),
    CHOOSE_PAPER("!choose paper"),
    CHOOSE_SCISSOR("!choose scissors"),
    REVANCHE("!play revanche"),
    LEAVE("!play quit");

    public final String command;

    GameCommand(String command) {
        this.command = command;
    }
}
