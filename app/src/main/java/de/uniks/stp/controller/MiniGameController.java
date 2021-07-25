package de.uniks.stp.controller;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.emote.EmoteParser;
import de.uniks.stp.minigame.GameCommand;
import de.uniks.stp.minigame.GameInfo;
import de.uniks.stp.minigame.GameInvitation;
import de.uniks.stp.minigame.GameMatcher;
import de.uniks.stp.modal.EasterEggModal;
import de.uniks.stp.model.DirectMessage;
import de.uniks.stp.model.User;
import de.uniks.stp.network.websocket.WebSocketService;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class MiniGameController implements ControllerInterface {
    private static final Logger log = LoggerFactory.getLogger(MiniGameController.class);
    private final GameInvitation invitation = new GameInvitation();
    private final HashMap<String, BiConsumer<String, Long>> incomingCommandHandler = new HashMap<>();
    private final ViewLoader viewLoader;
    private EasterEggModal easterEggModal;
    private final User chatPartner;
    private final GameMatcher gameMatcher = new GameMatcher();
    private final EasterEggModal.EasterEggModalFactory easterEggModalFactory;
    private final WebSocketService webSocketService;

    @AssistedInject
    public MiniGameController(ViewLoader viewLoader,
                              WebSocketService webSocketService,
                              EasterEggModal.EasterEggModalFactory easterEggModalFactory,
                              @Assisted User chatPartner) {
        this.chatPartner = chatPartner;
        this.viewLoader = viewLoader;
        this.easterEggModalFactory = easterEggModalFactory;
        this.webSocketService = webSocketService;
    }

    @Override
    public void init() {
        incomingCommandHandler.put(GameCommand.PLAY.command, this::incomingPlayCommand);
        incomingCommandHandler.put(GameCommand.CHOOSE_ROCK.command, this::incomingChooseActionCommand);
        incomingCommandHandler.put(GameCommand.CHOOSE_SCISSOR.command, this::incomingChooseActionCommand);
        incomingCommandHandler.put(GameCommand.CHOOSE_PAPER.command, this::incomingChooseActionCommand);

        incomingCommandHandler.put(
            GameCommand.REVANCHE.command,
            (messageText, timestamp) -> {
                gameMatcher.setOpponentCommand(GameCommand.REVANCHE);
                checkCommandMatch();
            }
        );
        incomingCommandHandler.put(
            GameCommand.LEAVE.command,
            (messageText, timestamp) -> gameMatcher.setOpponentCommand(GameCommand.LEAVE)
        );
    }

    @Override
    public void stop() {
        incomingCommandHandler.clear();
        easterEggModal = null;
    }

    public static boolean isPlayMessage(String message) {
        message = EmoteParser.convertTextWithUnicodeToNames(message);
        return message.equals(GameCommand.PLAY.command);
    }

    public boolean isIncomingCommandMessage(String message) {
        message = EmoteParser.convertTextWithUnicodeToNames(message);
        return incomingCommandHandler.containsKey(message);
    }

    public void handleIncomingMessage(DirectMessage message) {
        String messageText = EmoteParser.convertTextWithUnicodeToNames(message.getMessage());
        if (isIncomingCommandMessage(messageText)) {
            incomingCommandHandler.get(messageText).accept(messageText, message.getTimestamp());
        }
    }

    public void handleOutgoingPlayMessage(String message) {
        message = EmoteParser.convertTextWithUnicodeToNames(message);
        if (!isPlayMessage(message)) {
            return;
        }
        if (invitation.isReceived()) {
            invitation.recycle();
            showEasterEgg();
        } else {
            invitation.setState(GameInvitation.State.SENT).setCreationTime(new Date().getTime());
        }
    }

    private void checkCommandMatch() {
        gameMatcher.ifCommandMatch(command -> {

            }
        );
    }

    private void incomingPlayCommand(String messageText, long timestamp) {
        if (invitation.isSent()) {
            invitation.recycle();
            showEasterEgg();
        } else {
            invitation.setState(GameInvitation.State.RECEIVED).setCreationTime(timestamp);
        }
    }

    private void incomingChooseActionCommand(String messageText, long timestamp) {
        Scanner scanner = new Scanner(messageText);
        scanner.next();
        String rawOpponentAction = scanner.next();

        GameInfo.castAction(rawOpponentAction)
            .map(
                (action -> {
                    switch (action) {
                        case ROCK:
                            return GameCommand.CHOOSE_ROCK;
                        case PAPER:
                            return GameCommand.CHOOSE_PAPER;
                        case SCISSORS:
                            return GameCommand.CHOOSE_SCISSOR;
                    }
                    return null;
                })
            ).map(gameMatcher::setOpponentCommand);

        gameMatcher.ifActionMatch((ownAction, opponentAction) -> {
            log.debug("MATCH");
        });
    }

    /**
     * Initializes and shows the EasterEggModal, is called when both users sent the play command
     */
    private void showEasterEgg() {
        closeEasterEggModal(null);
        Platform.runLater(() -> {
            Parent easterEggModalView = viewLoader.loadView(Views.EASTER_EGG_MODAL);
            easterEggModal = easterEggModalFactory.create(easterEggModalView,
                chatPartner,
                this::closeEasterEggModal);
            easterEggModal.setOnPaperButtonClicked(event -> {
                webSocketService.sendPrivateMessage(chatPartner.getName(), GameCommand.CHOOSE_PAPER.command);
                gameMatcher.setOwnCommand(GameCommand.CHOOSE_PAPER);
                easterEggModal.setButtonColor(GameInfo.Action.PAPER, "green");
                gameMatcher.ifActionMatch((ownAction, opponentAction) -> {
                    GameInfo.Result result = GameInfo.determineWinner(ownAction, opponentAction);

                    if (result.equals(GameInfo.Result.LOSS)) {
                        easterEggModal.setButtonColor(ownAction, "red");
                        gameMatcher.recycle();
                    }
                });
            });
            easterEggModal.setOnRockButtonClicked(event -> {
                webSocketService.sendPrivateMessage(chatPartner.getName(), GameCommand.CHOOSE_ROCK.command);
                gameMatcher.setOwnCommand(GameCommand.CHOOSE_ROCK);
                easterEggModal.setButtonColor(GameInfo.Action.ROCK, "green");
                gameMatcher.ifActionMatch((ownAction, opponentAction) -> {
                    GameInfo.Result result = GameInfo.determineWinner(ownAction, opponentAction);

                    if (result.equals(GameInfo.Result.LOSS)) {
                        easterEggModal.setButtonColor(ownAction, "red");
                        gameMatcher.recycle();
                    }
                });
            });
            easterEggModal.setOnScissorsButtonClicked(event -> {
                webSocketService.sendPrivateMessage(chatPartner.getName(), GameCommand.CHOOSE_SCISSOR.command);
                gameMatcher.setOwnCommand(GameCommand.CHOOSE_SCISSOR);
                easterEggModal.setButtonColor(GameInfo.Action.SCISSORS, "green");
                gameMatcher.ifActionMatch((ownAction, opponentAction) -> {
                    GameInfo.Result result = GameInfo.determineWinner(ownAction, opponentAction);

                    if (result.equals(GameInfo.Result.LOSS)) {
                        easterEggModal.setButtonColor(ownAction, "red");
                        gameMatcher.recycle();
                    }
                });
            });
            easterEggModal.show();
        });
    }

    private void closeEasterEggModal(ActionEvent actionEvent) {
        if (Objects.nonNull(easterEggModal)) {
            easterEggModal.close();
            easterEggModal = null;
        }
    }

    @AssistedFactory
    public interface MiniGameControllerFactory {
        MiniGameController create(User chatPartner);
    }
}
