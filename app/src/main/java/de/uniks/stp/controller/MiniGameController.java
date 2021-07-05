package de.uniks.stp.controller;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.emote.EmoteParser;
import de.uniks.stp.minigame.GameCommand;
import de.uniks.stp.minigame.GameInvitation;
import de.uniks.stp.minigame.GameInvitationState;
import de.uniks.stp.modal.EasterEggModal;
import de.uniks.stp.model.DirectMessage;
import de.uniks.stp.model.User;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.BiConsumer;

public class MiniGameController implements ControllerInterface {
    private final GameInvitation invitation = new GameInvitation();
    private final HashMap<String, BiConsumer<String, Long>> incomingCommandHandler = new HashMap<>();
    private final ViewLoader viewLoader;
    private EasterEggModal easterEggModal;
    private final User chatPartner;

    @Inject
    EasterEggModal.EasterEggModalFactory easterEggModalFactory;

    @AssistedInject
    public MiniGameController(ViewLoader viewLoader, @Assisted User chatPartner) {
        this.chatPartner = chatPartner;
        this.viewLoader = viewLoader;
    }

    @Override
    public void init() {
        incomingCommandHandler.put(GameCommand.PLAY.command, this::handleIncomingPlayCommand);
        incomingCommandHandler.put(GameCommand.CHOOSE_ROCK.command, this::handleIncomingChooseActionCommand);
        incomingCommandHandler.put(GameCommand.CHOOSE_SCISSOR.command, this::handleIncomingChooseActionCommand);
        incomingCommandHandler.put(GameCommand.CHOOSE_PAPER.command, this::handleIncomingChooseActionCommand);
        incomingCommandHandler.put(GameCommand.REVANCHE.command, this::handleIncomingRevancheCommand);
        incomingCommandHandler.put(GameCommand.LEAVE.command, this::handleIncomingLeaveCommand);
    }

    @Override
    public void stop() {
        incomingCommandHandler.clear();
        easterEggModal = null;
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
            easterEggModal.show();
        });
    }

    private void closeEasterEggModal(ActionEvent actionEvent) {
        if (Objects.nonNull(easterEggModal)) {
            easterEggModal.close();
            easterEggModal = null;
        }
    }

    public boolean isIncomingCommandMessage(String message) {
        message = EmoteParser.convertTextWithUnicodeToNames(message);
        return incomingCommandHandler.containsKey(message);
    }

    public static boolean isPlayMessage(String message) {
        message = EmoteParser.convertTextWithUnicodeToNames(message);
        return message.equals(GameCommand.PLAY.command);
    }

    public void handleIncomingMessage(DirectMessage message) {
        String messageText = EmoteParser.convertTextWithUnicodeToNames(message.getMessage());
        if (isIncomingCommandMessage(messageText)) {
            incomingCommandHandler.get(messageText).accept(messageText, message.getTimestamp());
        }
    }

    public void handleOutgoingPlayMessage(String message) {
        message = EmoteParser.convertTextWithUnicodeToNames(message);
        if (isPlayMessage(message)) {
            handleOutgoingPlayCommand(message);
        }
    }

    private void handleIncomingPlayCommand(String messageText, long timestamp) {
        if (invitation.isSent()) {
            invitation.recycle();
            showEasterEgg();
        } else {
            invitation.setState(GameInvitationState.RECEIVED).setCreationTime(timestamp);
        }
    }

    private void handleIncomingRevancheCommand(String messageText, long timestamp) {
        if (Objects.nonNull(easterEggModal)) {
            easterEggModal.incomingRevanche();
        }
    }

    private void handleIncomingLeaveCommand(String messageText, long timestamp) {
        if (Objects.nonNull(easterEggModal)) {
            easterEggModal.opponentLeft();
        }
    }

    private void handleIncomingChooseActionCommand(String messageText, long timestamp) {
        Scanner scanner = new Scanner(messageText);
        scanner.next();
        String action = scanner.next();
        if (Objects.nonNull(easterEggModal)) {
            easterEggModal.setOpponentAction(action);
        }
    }

    private void handleOutgoingPlayCommand(String message) {
        if (invitation.isReceived()) {
            invitation.recycle();
            showEasterEgg();
        } else {
            invitation.setState(GameInvitationState.SENT).setCreationTime(new Date().getTime());
        }
    }

    @AssistedFactory
    public interface MiniGameControllerFactory {
        MiniGameController create(User chatPartner);
    }
}
