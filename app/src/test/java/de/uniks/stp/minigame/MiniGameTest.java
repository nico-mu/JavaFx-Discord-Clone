package de.uniks.stp.minigame;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class MiniGameTest {
    @Test
    public void testMiniGame() {
        String action = "rock";
        Optional<GameInfo.Action> castedAction = GameInfo.castAction(action);

        Assertions.assertTrue(castedAction.isPresent());
    }

    @Test
    public void testMiniGameMatcher() {
        AtomicBoolean lambdaCalled = new AtomicBoolean(false);
        GameMatcher matcher = new GameMatcher();
        matcher.setOwnCommand(GameCommand.CHOOSE_SCISSOR);
        matcher.setOpponentCommand(GameCommand.CHOOSE_ROCK);

        matcher.ifActionMatch(((ownAction, opponentAction) -> {
            lambdaCalled.set(true);
            Assertions.assertEquals(GameInfo.Action.SCISSORS, ownAction);
            Assertions.assertEquals(GameInfo.Action.ROCK, opponentAction);
        }));
        Assertions.assertTrue(lambdaCalled.get());
        lambdaCalled.set(false);

        matcher.setOwnCommand(GameCommand.CHOOSE_ROCK);
        matcher.ifActionMatch(((ownAction, opponentAction) -> {
            lambdaCalled.set(true);
            Assertions.assertEquals(GameInfo.Action.ROCK, ownAction);
            Assertions.assertEquals(GameInfo.Action.ROCK, opponentAction);
        }));
        Assertions.assertTrue(lambdaCalled.get());
        lambdaCalled.set(false);

        Assertions.assertTrue(matcher.isActionMatch());

        matcher.setOpponentCommand(GameCommand.LEAVE);
        Assertions.assertFalse(matcher.isActionMatch());

        matcher.setOwnCommand(GameCommand.LEAVE);
        Assertions.assertTrue(matcher.isCommandMatch());

        matcher.ifCommandMatch((command -> {
            lambdaCalled.set(true);
            Assertions.assertEquals(GameCommand.LEAVE, command);
        }));
        Assertions.assertTrue(lambdaCalled.get());
        lambdaCalled.set(false);
    }
}
