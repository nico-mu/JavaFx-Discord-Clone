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
    public static final String WIN_COLOR = "green";
    public static final String LOSS_COLOR = "red";
    public static final String DRAW_COLOR = "yellow";
    public static final String SELECTED_ACTION_COLOR = "white";
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
    private final ViewLoader viewLoader;
    private final EventHandler<ActionEvent> closeHandler;

    @AssistedInject
    public EasterEggModal(ViewLoader viewLoader,
                          @Named("primaryStage") Stage primaryStage,
                          @Assisted Parent root,
                          @Assisted EventHandler<ActionEvent> closeHandler) {
        super(root, primaryStage);
        this.viewLoader = viewLoader;
        this.closeHandler = closeHandler;

        setTitle(viewLoader.loadLabel(Constants.LBL_EASTER_EGG_TITLE));
        actionLabel = (Label) view.lookup(ACTION_LABEL);
        revancheButton = (JFXButton) view.lookup(REVANCHE_BUTTON);
        rockButton = (Button) view.lookup(ROCK_BUTTON);
        scissorsButton = (Button) view.lookup(SCISSORS_BUTTON);
        paperButton = (Button) view.lookup(PAPER_BUTTON);
        cancelButton = (JFXButton) view.lookup(CANCEL_BUTTON);

        cancelButton.setOnAction((event) -> {
            this.close();
        });
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

    public void setButtonColor(GameInfo.Action action, String color) {
        final String COLORED_BG = "-fx-background-color: " + color + ";";
        resetButtonColor();
        Platform.runLater(() -> {
            switch (action) {
                case ROCK -> rockButton.setStyle(COLORED_BG);
                case PAPER -> paperButton.setStyle(COLORED_BG);
                case SCISSORS -> scissorsButton.setStyle(COLORED_BG);
            }
        });
    }

    public void setActionText(String text) {
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

    public void playAgain() {
        Platform.runLater(() -> {
            revancheButton.setVisible(false);
            // TODO: remove dependency for Constants here
            actionLabel.setText(viewLoader.loadLabel(Constants.LBL_CHOOSE_ACTION));
            resetButtonColor();
            rockButton.setDisable(false);
            paperButton.setDisable(false);
            scissorsButton.setDisable(false);
        });
    }

    public JFXButton getRevancheButton() {
        return revancheButton;
    }

    private void resetButtonColor() {
        final String TRANSPARENT_BG = "-fx-background-color: transparent;";
        rockButton.setStyle(TRANSPARENT_BG);
        paperButton.setStyle(TRANSPARENT_BG);
        scissorsButton.setStyle(TRANSPARENT_BG);
    }

    @Override
    public void close() {
        closeHandler.handle(null);
        revancheButton.setOnAction(null);
        rockButton.setOnAction(null);
        scissorsButton.setOnAction(null);
        paperButton.setOnAction(null);
        cancelButton.setOnAction(null);
        super.close();
    }

    @AssistedFactory
    public interface EasterEggModalFactory {
        EasterEggModal create(Parent view, EventHandler<ActionEvent> closeEventHandler);
    }
}
