package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Server;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class ConfirmationModal extends AbstractModal {
    public static final String TITLE_LABEL = "#title-label";
    public static final String CONFIRM_LABEL = "#confirm-label";
    public static final String YES_BUTTON = "#yes-button";
    public static final String NO_BUTTON = "#no-button";

    private final Label titleLabel;
    private final Label confirmLabel;
    private final JFXButton yesButton;
    private final JFXButton noButton;

    private static final Logger log = LoggerFactory.getLogger(ConfirmationModal.class);

    public ConfirmationModal(Parent root, String titleLBL, String confirmLBL, EventHandler<ActionEvent> yesHandler, EventHandler<ActionEvent> noHandler) {
        super(root);

        initStyle(StageStyle.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);

        titleLabel = (Label) view.lookup(TITLE_LABEL);
        confirmLabel = (Label) view.lookup(CONFIRM_LABEL);
        yesButton = (JFXButton) view.lookup(YES_BUTTON);
        noButton = (JFXButton) view.lookup(NO_BUTTON);

        titleLabel.setText(ViewLoader.loadLabel(titleLBL));
        if(confirmLabel == null){
            System.out.println("null!!");
        }
        else{
            confirmLabel.setText(ViewLoader.loadLabel(confirmLBL));
        }

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
