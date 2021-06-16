package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.User;
import de.uniks.stp.network.WebSocketService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasterEggModal extends AbstractModal {
    private static final Logger log = LoggerFactory.getLogger(EasterEggModal.class);

    public final String ROCK = "rock";
    public final String PAPER = "paper";
    public final String SCISSORS = "scissors";
    public final String ACTION_LABEL = "#action-label";
    public final String REVANCHE_BUTTON = "#revanche-button";
    public final String ROCK_BUTTON = "#rock-button";
    public final String PAPER_BUTTON = "#paper-button";
    public final String SCISSORS_BUTTON = "#scissors-button";
    public final String CANCEL_BUTTON = "#cancel-button";

    private final Label actionLabel;
    private final JFXButton revancheButton;
    private final Button rockButton;
    private final Button scissorsButton;
    private final Button paperButton;
    private final JFXButton cancelButton;

    private final User currentUser;
    private final User opponentUser;
    private String action;
    private String opponentAction;
    private boolean revanche = false;

    public EasterEggModal(Parent root, User currentUser, User opponentUser, EventHandler<ActionEvent> closeHandler) {
        super(root);
        this.currentUser = currentUser;
        this.opponentUser = opponentUser;

        setTitle(ViewLoader.loadLabel(Constants.LBL_EASTER_EGG_TITLE));
        actionLabel = (Label) view.lookup(ACTION_LABEL);
        revancheButton = (JFXButton) view.lookup(REVANCHE_BUTTON);
        rockButton = (Button) view.lookup(ROCK_BUTTON);
        scissorsButton = (Button) view.lookup(SCISSORS_BUTTON);
        paperButton = (Button) view.lookup(PAPER_BUTTON);
        cancelButton = (JFXButton) view.lookup(CANCEL_BUTTON);

        revancheButton.setOnAction(this::onRevancheButtonClicked);
        rockButton.setOnAction(this::onRockButtonClicked);
        scissorsButton.setOnAction(this::onScissorsButtonClicked);
        paperButton.setOnAction(this::onPaperButtonClicked);
        cancelButton.setOnAction(closeHandler);
    }

    public void setOpponentAction(String action) {
        opponentAction = action;
        if (action != null) {
            fight();
        }
    }

    private void onRockButtonClicked(ActionEvent actionEvent) {
        WebSocketService.sendPrivateMessage(opponentUser.getName(), Constants.COMMAND_CHOOSE_ROCK);
        action = ROCK;
        if (opponentAction != null) {
            fight();
        }
    }

    private void onScissorsButtonClicked(ActionEvent actionEvent) {
        WebSocketService.sendPrivateMessage(opponentUser.getName(), Constants.COMMAND_CHOOSE_SCISSOR);
        action = SCISSORS;
        if (opponentAction != null) {
            fight();
        }
    }

    private void onPaperButtonClicked(ActionEvent actionEvent) {
        WebSocketService.sendPrivateMessage(opponentUser.getName(), Constants.COMMAND_CHOOSE_PAPER);
        action = PAPER;
        if (opponentAction != null) {
            fight();
        }
    }

    private void fight() {
        int result = determineWinner();
        if (result == 0) {
            Platform.runLater(() -> actionLabel.setText(ViewLoader.loadLabel(Constants.LBL_RESULT_DRAW)));
        } else if (result == 1) {
            Platform.runLater(() -> actionLabel.setText(ViewLoader.loadLabel(Constants.LBL_RESULT_WIN)));
        } else {
            Platform.runLater(() -> actionLabel.setText(ViewLoader.loadLabel(Constants.LBL_RESULT_LOSS)));
        }
        revancheButton.setVisible(true);

    }

    /**
     * @return 1 if currentUser wins, 0 if it's a draw, -1 if opponent wins
     */
    private int determineWinner() {
        if (action.equals(opponentAction)) {
            return 0;
        } else if (action.equals(ROCK)) {
            if (opponentAction.equals(PAPER)) {
                return -1;
            } else {
                return 1;
            }
        } else if (action.equals(PAPER)) {
            if (opponentAction.equals(ROCK)) {
                return 1;
            } else {
                return -1;
            }
        } else {
            if (opponentAction.equals(ROCK)) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    private void onRevancheButtonClicked(ActionEvent actionEvent) {
        if(revanche){
            WebSocketService.sendPrivateMessage(opponentUser.getName(), Constants.COMMAND_REVANCHE);
            playAgain();
        } else{
            revanche = true;
            WebSocketService.sendPrivateMessage(opponentUser.getName(), Constants.COMMAND_REVANCHE);
            actionLabel.setText("Waiting for oppponent to accept revanche");
        }
    }

    public void incomingRevanche(){
        if(revanche){
            playAgain();
        } else{
            revanche = true;
            Platform.runLater(() -> actionLabel.setText("Your oppponent asks for a revanche"));
        }
    }

    private void playAgain() {
        revanche = false;
        Platform.runLater(() -> {
            revancheButton.setVisible(false);
            actionLabel.setText(ViewLoader.loadLabel(Constants.LBL_CHOOSE_ACTION));
        });
        action = null;
        opponentAction = null;
    }

    public void opponentLeft(){
        revanche = false;
        Platform.runLater(() -> actionLabel.setText("Your oppponent left the game"));
    }

    @Override
    public void close() {
        WebSocketService.sendPrivateMessage(opponentUser.getName(), Constants.COMMAND_LEAVE);
        revancheButton.setOnAction(null);
        rockButton.setOnAction(null);
        scissorsButton.setOnAction(null);
        paperButton.setOnAction(null);
        cancelButton.setOnAction(null);
        super.close();
    }
}
