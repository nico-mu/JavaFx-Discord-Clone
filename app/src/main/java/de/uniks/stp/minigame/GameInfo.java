package de.uniks.stp.minigame;

import java.util.*;

public class GameInfo {
    // Own action -> opponent action
    private final static Map<GameAction, GameAction> winConditions = new HashMap<>();

    static {
        winConditions.put(GameAction.ROCK, GameAction.SCISSORS);
        winConditions.put(GameAction.PAPER, GameAction.ROCK);
        winConditions.put(GameAction.SCISSORS, GameAction.PAPER);
    }

    public static GameResult determineWinner(GameAction ownAction, GameAction opponentAction) {
        if (ownAction.equals(opponentAction)) {
            return GameResult.DRAW;
        }
        if (winConditions.get(ownAction).equals(opponentAction)) {
            return GameResult.WIN;
        }

        return GameResult.LOSS;
    }

    public static Optional<GameAction> castAction(String action) {
        GameAction castedAction;

        try {
            castedAction = GameAction.valueOf(action.toUpperCase());
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }

        return Optional.of(castedAction);
    }

    public static Optional<GameAction> castCommand(GameCommand command) {
        if (command.equals(GameCommand.CHOOSE_PAPER)) {
            return Optional.of(GameAction.PAPER);
        }
        if (command.equals(GameCommand.CHOOSE_ROCK)) {
            return Optional.of(GameAction.ROCK);
        }
        if (command.equals(GameCommand.CHOOSE_SCISSOR)) {
            return Optional.of(GameAction.SCISSORS);
        }

        return Optional.empty();
    }
}
