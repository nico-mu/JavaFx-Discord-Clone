package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.ViewLoader;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
    public ConfirmationModal(Parent root, String titleLBL, String confirmLBL, Stage stage, EventHandler<ActionEvent> yesHandler, EventHandler<ActionEvent> noHandler) {
        super(root, stage);

        initStyle(StageStyle.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);

        Label titleLabel = (Label) view.lookup(TITLE_LABEL);
        Label confirmLabel = (Label) view.lookup(CONFIRM_LABEL);
        yesButton = (JFXButton) view.lookup(YES_BUTTON);
        noButton = (JFXButton) view.lookup(NO_BUTTON);

        titleLabel.setText(ViewLoader.loadLabel(titleLBL));
        confirmLabel.setText(ViewLoader.loadLabel(confirmLBL));

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
}
