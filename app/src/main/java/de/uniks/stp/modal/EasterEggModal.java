package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.minigame.GameCommand;
import de.uniks.stp.model.User;
import de.uniks.stp.network.websocket.WebSocketService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import javax.inject.Named;

public class EasterEggModal extends AbstractModal {
    public final String ROCK = "rock";
    public final String PAPER = "paper";
    public final String SCISSORS = "scissor";
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

    private final User opponentUser;
    private final ViewLoader viewLoader;
    private final WebSocketService webSocketService;
    private String action;  //saves current selected action of currentUser
    private String opponentAction;  //saves current selected action of opponent
    private boolean revanche = false;  //used to save whether one player already invited the other for a revanche

    @AssistedInject
    public EasterEggModal(ViewLoader viewLoader,
                          WebSocketService webSocketService,
                          @Named("primaryStage") Stage primaryStage,
                          @Assisted Parent root,
                          @Assisted User opponentUser,
                          @Assisted EventHandler<ActionEvent> closeHandler) {
        super(root, primaryStage);
        this.opponentUser = opponentUser;
        this.viewLoader = viewLoader;
        this.webSocketService = webSocketService;

        setTitle(viewLoader.loadLabel(Constants.LBL_EASTER_EGG_TITLE));
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
            battle();  //battle as soon as both have selected an action
        }
    }

    private void onRockButtonClicked(ActionEvent actionEvent) {
        webSocketService.sendPrivateMessage(opponentUser.getName(), GameCommand.CHOOSE_ROCK.command);
        action = ROCK;
        reactToActionSelected();
    }

    private void onScissorsButtonClicked(ActionEvent actionEvent) {
        webSocketService.sendPrivateMessage(opponentUser.getName(), GameCommand.CHOOSE_SCISSOR.command);
        action = SCISSORS;
        reactToActionSelected();
    }

    private void onPaperButtonClicked(ActionEvent actionEvent) {
        webSocketService.sendPrivateMessage(opponentUser.getName(), GameCommand.CHOOSE_PAPER.command);
        action = PAPER;
        reactToActionSelected();
    }

    /**
     * Called everytime the currentPlayer selected an action
     */
    private void reactToActionSelected() {
        colorOwnButton();
        if (opponentAction != null) {
            battle();  //battle as soon as both have selected an action
        }
    }

    /**
     * Used to show your current choice
     */
    private void colorOwnButton() {
        resetButtonColor();
        if(action.equals(ROCK)) {
            rockButton.setStyle("-fx-background-color: green;");
        } else if(action.equals(PAPER)){
            paperButton.setStyle("-fx-background-color: green;");
        } else if(action.equals(SCISSORS)){
            scissorsButton.setStyle("-fx-background-color: green;");
        }
    }

    private void resetButtonColor(){
        rockButton.setStyle("-fx-background-color: transparent;");
        paperButton.setStyle("-fx-background-color: transparent;");
        scissorsButton.setStyle("-fx-background-color: transparent;");
    }

    /**
     * Called when both players choiced. Shows result of battle.
     */
    private void battle() {
        int result = determineWinner();
        colorOpponentButton(result);
        if (result == 0) {
            Platform.runLater(() -> actionLabel.setText(viewLoader.loadLabel(Constants.LBL_RESULT_DRAW)));
        } else if (result == 1) {
            Platform.runLater(() -> actionLabel.setText(viewLoader.loadLabel(Constants.LBL_RESULT_WIN)));
        } else {
            Platform.runLater(() -> actionLabel.setText(viewLoader.loadLabel(Constants.LBL_RESULT_LOSS)));
        }

        Platform.runLater(() -> {
            revancheButton.setVisible(true);
            rockButton.setDisable(true);
            paperButton.setDisable(true);
            scissorsButton.setDisable(true);
        });
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

    /**
     * Called to show the opponents choice. At this moment the own choice is also already shown
     */
    private void colorOpponentButton(int res) {
        if(res == 0){
            // draw -> mark common choice yellow
            if(action.equals(ROCK)){
                Platform.runLater(() -> rockButton.setStyle("-fx-background-color: yellow;"));
            } else if(action.equals(PAPER)){
                Platform.runLater(() -> paperButton.setStyle("-fx-background-color: yellow;"));
            } else if(action.equals(SCISSORS)){
                Platform.runLater(() -> scissorsButton.setStyle("-fx-background-color: yellow;"));
            }
        } else if(opponentAction.equals(ROCK)){
            Platform.runLater(() -> rockButton.setStyle("-fx-background-color: red;"));
        } else if(opponentAction.equals(PAPER)){
            Platform.runLater(() -> paperButton.setStyle("-fx-background-color: red;"));
        } else{
            Platform.runLater(() -> scissorsButton.setStyle("-fx-background-color: red;"));
        }
    }

    private void onRevancheButtonClicked(ActionEvent actionEvent) {
        if(revanche){
            webSocketService.sendPrivateMessage(opponentUser.getName(), GameCommand.REVANCHE.command);
            playAgain();
        } else{
            revanche = true;
            revancheButton.setVisible(false);
            webSocketService.sendPrivateMessage(opponentUser.getName(), GameCommand.REVANCHE.command);
            actionLabel.setText(viewLoader.loadLabel(Constants.LBL_REVANCHE_WAIT));
        }
    }

    public void incomingRevanche(){
        if(revanche){
            playAgain();
        } else{
            revanche = true;
            Platform.runLater(() -> actionLabel.setText(viewLoader.loadLabel(Constants.LBL_REVANCHE_RESPOND)));
        }
    }

    /**
     * prepares everything for a new battle
     */
    private void playAgain() {
        revanche = false;
        Platform.runLater(() -> {
            revancheButton.setVisible(false);
            actionLabel.setText(viewLoader.loadLabel(Constants.LBL_CHOOSE_ACTION));
            resetButtonColor();
            rockButton.setDisable(false);
            paperButton.setDisable(false);
            scissorsButton.setDisable(false);
        });
        action = null;
        opponentAction = null;
    }

    public void opponentLeft(){
        revanche = false;
        Platform.runLater(() -> actionLabel.setText(viewLoader.loadLabel(Constants.LBL_GAME_LEFT)));
    }

    @Override
    public void close() {
        webSocketService.sendPrivateMessage(opponentUser.getName(), GameCommand.LEAVE.command);
        revancheButton.setOnAction(null);
        rockButton.setOnAction(null);
        scissorsButton.setOnAction(null);
        paperButton.setOnAction(null);
        cancelButton.setOnAction(null);
        super.close();
    }

    @AssistedFactory
    public interface EasterEggModalFactory {
        EasterEggModal create(Parent view, User opponentUser, EventHandler<ActionEvent> closeEventHandler);
    }
}
