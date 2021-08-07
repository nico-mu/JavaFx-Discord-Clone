package de.uniks.stp.minigame;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GameMatcher {
    private GameCommand ownCommand;
    private GameCommand opponentCommand;

    public Optional<GameCommand> getOwnCommand() {
        return Optional.ofNullable(ownCommand);
    }

    public GameMatcher setOwnCommand(GameCommand command) {
        ownCommand = command;
        return this;
    }

    public Optional<GameCommand> getOpponentCommand() {
        return Optional.ofNullable(opponentCommand);
    }

    public GameMatcher setOpponentCommand(GameCommand command) {
        opponentCommand = command;
        return this;
    }

    public void recycle() {
        ownCommand = null;
        opponentCommand = null;
    }

    public boolean isCommandMatch() {
        return getOwnCommand().flatMap(
            ownCommand -> getOpponentCommand().map(
                ownCommand::equals
            )
        ).orElse(false);
    }

    public GameMatcher ifCommandMatch(Consumer<GameCommand> matchFn) {
        return getOwnCommand().flatMap(
            (ownCommand) -> getOpponentCommand().flatMap(
                    (opponentCommand) -> {
                        if (ownCommand.equals(opponentCommand)) {
                            matchFn.accept(ownCommand);
                        }
                        return Optional.of(this);
                    }
                )
        ).orElse(this);
    }

    public boolean isActionMatch() {
        return getOwnCommand().flatMap(
            ownCommand -> getOpponentCommand().map(
                opponentCommand -> GameInfo.castCommand(ownCommand).isPresent()
                    && GameInfo.castCommand(opponentCommand).isPresent()
            )
        ).orElse(false);
    }

    public GameMatcher ifActionMatch(BiConsumer<GameAction, GameAction> matchFn) {
        return getOwnCommand().flatMap(GameInfo::castCommand).flatMap(
            (ownAction) ->
                getOpponentCommand().flatMap(GameInfo::castCommand).flatMap(
                    (opponentAction) -> {
                        matchFn.accept(ownAction, opponentAction);
                        return Optional.of(this);
                    }
                )
        ).orElse(this);
    }
}
