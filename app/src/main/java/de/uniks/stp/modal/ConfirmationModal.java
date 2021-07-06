package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.ViewLoader;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.inject.Named;

public class ConfirmationModal extends AbstractModal {
    public static final String TITLE_LABEL = "#title-label";
    public static final String CONFIRM_LABEL = "#confirm-label";
    public static final String YES_BUTTON = "#yes-button";
    public static final String NO_BUTTON = "#no-button";

    private final JFXButton yesButton;
    private final JFXButton noButton;

    /**
     * ConfirmationModal can be used whenever there is need for a modal to confirm any action.
     * @param root
     * @param titleLBL Constant of Label that can be loaded as title
     * @param confirmLBL Constant of Label that can be loaded as confirmation question/explaining text
     * @param yesHandler EventHandler that will be called when Yes-button is pressed
     * @param noHandler EventHandler that will be called when No-button is pressed
     */

    @AssistedInject
    public ConfirmationModal(ViewLoader viewLoader,
                             @Named("primaryStage") Stage primaryStage,
                             @Assisted Parent root,
                             @Assisted("titleLBL") String titleLBL,
                             @Assisted("confirmLBL") String confirmLBL,
                             @Assisted("yesHandler") EventHandler<ActionEvent> yesHandler,
                             @Assisted("noHandler") EventHandler<ActionEvent> noHandler) {
        super(root, primaryStage);

        initStyle(StageStyle.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);

        Label titleLabel = (Label) view.lookup(TITLE_LABEL);
        Label confirmLabel = (Label) view.lookup(CONFIRM_LABEL);
        yesButton = (JFXButton) view.lookup(YES_BUTTON);
        noButton = (JFXButton) view.lookup(NO_BUTTON);

        titleLabel.setText(viewLoader.loadLabel(titleLBL));
        confirmLabel.setText(viewLoader.loadLabel(confirmLBL));

        yesButton.setOnAction(yesHandler);
        yesButton.setDefaultButton(true);  // use Enter in order to press button
        noButton.setOnAction(noHandler);
        noButton.setCancelButton(true);  // use Escape in order to press button
    }

    @Override
    public void close() {
        yesButton.setOnAction(null);
        noButton.setOnAction(null);
        super.close();
    }

    @AssistedFactory
    public interface ConfirmationModalFactory {
        ConfirmationModal create(Parent root,
                                 @Assisted("titleLBL") String titleLBL,
                                 @Assisted("confirmLBL") String confirmLBL,
                                 @Assisted("yesHandler") EventHandler<ActionEvent> yesHandler,
                                 @Assisted("noHandler") EventHandler<ActionEvent> noHandler);
    }
}
