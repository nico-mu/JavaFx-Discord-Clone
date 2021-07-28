package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.minigame.GameCommand;
import de.uniks.stp.minigame.GameInfo;
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
import java.util.function.Consumer;

public class EasterEggModal extends AbstractModal {
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

    private final User opponentUser;
    private final ViewLoader viewLoader;
    private final WebSocketService webSocketService;

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

        cancelButton.setOnAction(closeHandler);
    }

    public void setOnRockButtonClicked(EventHandler<ActionEvent> onRockButtonClicked) {
        rockButton.setOnAction(onRockButtonClicked);
    }

    public void setOnScissorsButtonClicked(EventHandler<ActionEvent> onScissorsButtonClicked) {
        scissorsButton.setOnAction(onScissorsButtonClicked);
    }

    public void setOnPaperButtonClicked(EventHandler<ActionEvent> onPaperButtonClicked) {
        paperButton.setOnAction(onPaperButtonClicked);
    }

    public void setOnRevancheButtonClicked(EventHandler<ActionEvent> onRevancheButtonClicked) {
        revancheButton.setOnAction(onRevancheButtonClicked);
    }

    /**
     * Used to show your current choice
     */
    public void setButtonColor(GameInfo.Action action, String color) {
        resetButtonColor();
        Platform.runLater(() -> {
            switch (action) {
                case ROCK -> rockButton.setStyle("-fx-background-color: " + color + ";");
                case PAPER -> paperButton.setStyle("-fx-background-color: " + color + ";");
                case SCISSORS -> scissorsButton.setStyle("-fx-background-color: " + color + ";");
            }
        });
    }

    public void setScoreText(String text) {
        Platform.runLater(() -> actionLabel.setText(text));
    }

    public void endGame() {
        Platform.runLater(() -> {
            revancheButton.setVisible(true);
            rockButton.setDisable(true);
            paperButton.setDisable(true);
            scissorsButton.setDisable(true);
        });
    }

    public JFXButton getRevancheButton() {
        return revancheButton;
    }

    private void resetButtonColor() {
        rockButton.setStyle("-fx-background-color: transparent;");
        paperButton.setStyle("-fx-background-color: transparent;");
        scissorsButton.setStyle("-fx-background-color: transparent;");
    }

    /**
     * Called to show the opponents choice. At this moment the own choice is also already shown
     */
    private void colorOpponentButton(int res) {

    }

    /**
     * prepares everything for a new battle
     */
    public void playAgain() {
        Platform.runLater(() -> {
            revancheButton.setVisible(false);
            actionLabel.setText(viewLoader.loadLabel(Constants.LBL_CHOOSE_ACTION));
            resetButtonColor();
            rockButton.setDisable(false);
            paperButton.setDisable(false);
            scissorsButton.setDisable(false);
        });
    }

    public void opponentLeft() {
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
