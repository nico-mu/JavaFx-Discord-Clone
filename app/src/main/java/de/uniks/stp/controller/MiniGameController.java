package de.uniks.stp.controller;

import de.uniks.stp.ViewLoader;
import de.uniks.stp.emote.EmoteParser;
import de.uniks.stp.modal.EasterEggModal;
import de.uniks.stp.model.DirectMessage;
import de.uniks.stp.model.User;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MiniGameController implements ControllerInterface {
    public enum GameCommand {
        PLAY("!play :handshake:"),
        CHOOSE_ROCK("!choose rock"),
        CHOOSE_PAPER("!choose paper"),
        CHOOSE_SCISSOR("!choose scissor"),
        REVANCHE("!play revanche"),
        LEAVE("!play quit");

        public final String command;

        GameCommand(String command) {
            this.command = command;
        }
    }
    private static final Logger log = LoggerFactory.getLogger(ServerChatController.class);
    private final GameInvitation invitation = new GameInvitation();
    private final HashMap<String, BiConsumer<String, Long>> incomingCommandHandler = new HashMap<>();
    private final HashMap<String, Consumer<String>> outgoingCommandHandler = new HashMap<>();
    private EasterEggModal easterEggModal;
    private final User chatPartner;

    public MiniGameController(User chatPartner) {
        this.chatPartner = chatPartner;
    }

    @Override
    public void init() {
        incomingCommandHandler.put(GameCommand.PLAY.command, this::handleIncomingPlayCommand);
        incomingCommandHandler.put(GameCommand.CHOOSE_ROCK.command, this::handleIncomingChooseActionCommand);
        incomingCommandHandler.put(GameCommand.CHOOSE_SCISSOR.command, this::handleIncomingChooseActionCommand);
        incomingCommandHandler.put(GameCommand.CHOOSE_PAPER.command, this::handleIncomingChooseActionCommand);
        incomingCommandHandler.put(GameCommand.REVANCHE.command, this::handleIncomingRevancheCommand);
        incomingCommandHandler.put(GameCommand.LEAVE.command, this::handleIncomingLeaveCommand);
        outgoingCommandHandler.put(GameCommand.PLAY.command, this::handleOutgoingPlayCommand);
    }

    @Override
    public void stop() {
        incomingCommandHandler.clear();
        outgoingCommandHandler.clear();
        easterEggModal = null;
    }

    /**
     * Initializes and shows the EasterEggModal, is called when both users sent the play command
     */
    private void showEasterEgg() {
        closeEasterEggModal(null);
        Platform.runLater(() -> {
            Parent easterEggModalView = ViewLoader.loadView(Views.EASTER_EGG_MODAL);
            easterEggModal = new EasterEggModal(easterEggModalView,
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

    public boolean isOutgoingCommandMessage(String message) {
        message = EmoteParser.convertTextWithUnicodeToNames(message);
        return outgoingCommandHandler.containsKey(message);
    }

    public void handleIncomingMessage(DirectMessage message) {
        String messageText = EmoteParser.convertTextWithUnicodeToNames(message.getMessage());
        if (isIncomingCommandMessage(messageText)) {
            incomingCommandHandler.get(messageText).accept(messageText, message.getTimestamp());
        }
    }

    public void handleOutgoingMessage(String message) {
        message = EmoteParser.convertTextWithUnicodeToNames(message);
        if (isOutgoingCommandMessage(message)) {
            outgoingCommandHandler.get(message).accept(message);
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

    private enum GameInvitationState {
        PENDING,
        SENT,
        RECEIVED
    }
    private static class GameInvitation {
        public final int TIMEOUT = 30 * 1000; // 30 seconds
        private GameInvitationState state = GameInvitationState.PENDING;
        private Long creationTime;

        public long getCreationTime() {
            return creationTime;
        }

        public GameInvitationState getState() {
            return state;
        }

        public boolean hasNotTimeout() {
            return getCreationTime() >= new Date().getTime() - TIMEOUT;
        }

        public boolean isSent() {
            return getState().equals(GameInvitationState.SENT) && hasNotTimeout();
        }

        public boolean isReceived() {
            return getState().equals(GameInvitationState.RECEIVED) && hasNotTimeout();
        }

        public GameInvitation setCreationTime(long creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public GameInvitation setState(GameInvitationState state) {
            this.state = state;
            return this;
        }

        public void recycle() {
            setState(GameInvitationState.PENDING);
            creationTime = null;
        }
    }
}
