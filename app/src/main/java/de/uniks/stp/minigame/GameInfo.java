package de.uniks.stp.minigame;

import java.util.*;

public class GameInfo {
    public enum Action {
        ROCK("rock"),
        PAPER("paper"),
        SCISSORS("scissors");

        public final String action;

        Action(String action) {
            this.action = action;
        }
    }

    public enum Command {
        PLAY("!play :handshake:"),
        CHOOSE_ROCK("!choose " + Action.ROCK),
        CHOOSE_PAPER("!choose " + Action.PAPER),
        CHOOSE_SCISSOR("!choose " + Action.SCISSORS),
        REVANCHE("!revanche"),
        LEAVE("!play quit");

        public final String command;

        Command(String command) {
            this.command = command;
        }
    }

    public enum Result {
        WIN,
        DRAW,
        LOSS
    }

    // Own action -> opponent action
    private final static Map<Action, Action> winConditions = new HashMap<>();

    static {
        winConditions.put(Action.ROCK, Action.SCISSORS);
        winConditions.put(Action.PAPER, Action.ROCK);
        winConditions.put(Action.SCISSORS, Action.PAPER);
    }

    public static Result determineWinner(Action ownAction, Action opponentAction) {
        if (ownAction.equals(opponentAction)) {
            return Result.DRAW;
        }
        if (winConditions.get(ownAction).equals(opponentAction)) {
            return Result.WIN;
        }

        return Result.LOSS;
    }

    public static Optional<Action> castAction(String action) {
        Action castedAction;

        try {
            castedAction = Action.valueOf(action.toUpperCase());
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }

        return Optional.of(castedAction);
    }

    public static Optional<Action> castCommand(GameCommand command) {
        if (command.equals(GameCommand.CHOOSE_PAPER)) {
            return Optional.of(Action.PAPER);
        }
        if (command.equals(GameCommand.CHOOSE_ROCK)) {
            return Optional.of(Action.ROCK);
        }
        if (command.equals(GameCommand.CHOOSE_SCISSOR)) {
            return Optional.of(Action.SCISSORS);
        }

        return Optional.empty();
    }
}
