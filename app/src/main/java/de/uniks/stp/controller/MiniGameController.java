package de.uniks.stp.controller;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.emote.EmoteParser;
import de.uniks.stp.minigame.*;
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
    private final GameScore gameScore = new GameScore();
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
                easterEggModal.setScoreText(viewLoader.loadLabel(Constants.LBL_REVANCHE_RESPOND));
                gameMatcher.ifCommandMatch(command -> {
                    easterEggModal.playAgain();
                });
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

        gameMatcher.ifActionMatch(this::handleActionMatch);
    }

    // handle action match -> determine winner, set score, recycle
    private void handleActionMatch(GameInfo.Action ownAction, GameInfo.Action opponentAction) {
        GameInfo.Result result = GameInfo.determineWinner(ownAction, opponentAction);

        if (result.equals(GameInfo.Result.LOSS)) {
            gameScore.increaseOpponentScore();
            easterEggModal.setButtonColor(ownAction, "red");
        } else if (result.equals(GameInfo.Result.DRAW)) {
            easterEggModal.setButtonColor(ownAction, "yellow");
        } else {
            gameScore.increaseOwnScore();
            easterEggModal.setButtonColor(ownAction, "green");
        }

        if (gameScore.isOwnWin()) {
            easterEggModal.setScoreText(gameScore + ", " + viewLoader.loadLabel(Constants.LBL_RESULT_WIN));
            easterEggModal.endGame();
            gameScore.recycle();
        } else if (gameScore.isOwnLoss()) {
            easterEggModal.setScoreText(gameScore + ", " + viewLoader.loadLabel(Constants.LBL_RESULT_LOSS));
            easterEggModal.endGame();
            gameScore.recycle();
        } else {
            easterEggModal.setScoreText(gameScore + ", " + viewLoader.loadLabel(Constants.LBL_CHOOSE_ACTION));
        }
        gameMatcher.recycle();
    }

    private void handleActionSelect(GameCommand command) {
        GameInfo.castCommand(command).flatMap(
            (action -> {
                webSocketService.sendPrivateMessage(chatPartner.getName(), command.command);
                gameMatcher.setOwnCommand(command);
                easterEggModal.setButtonColor(action, "white");

                return Optional.empty();
            })
        );

        gameMatcher.ifActionMatch(this::handleActionMatch);
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
            easterEggModal.setOnPaperButtonClicked(event -> handleActionSelect(GameCommand.CHOOSE_PAPER));
            easterEggModal.setOnRockButtonClicked(event -> handleActionSelect(GameCommand.CHOOSE_ROCK));
            easterEggModal.setOnScissorsButtonClicked(event -> handleActionSelect(GameCommand.CHOOSE_SCISSOR));
            easterEggModal.setOnRevancheButtonClicked(event -> {
                webSocketService.sendPrivateMessage(chatPartner.getName(), GameCommand.REVANCHE.command);
                gameMatcher.setOwnCommand(GameCommand.REVANCHE);
                easterEggModal.getRevancheButton().setVisible(false);
                easterEggModal.setScoreText(viewLoader.loadLabel(Constants.LBL_REVANCHE_WAIT));

                gameMatcher.ifCommandMatch(command -> {
                    easterEggModal.playAgain();
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
