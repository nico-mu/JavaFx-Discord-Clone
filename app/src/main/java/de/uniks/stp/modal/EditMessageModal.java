package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.ServerMessage;
import de.uniks.stp.network.rest.SessionRestClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.Objects;

public class EditMessageModal extends AbstractModal {
    public static final String EDIT_MESSAGE_BUTTON = "#save-button";
    public static final String CANCEL_BUTTON = "#cancel-button";
    public static final String ERROR_LABEL = "#error-message-label";
    public static final String ENTER_MESSAGE_TEXT_FIELD = "#message-text-field";

    private final JFXButton saveButton;
    private final JFXButton cancelButton;
    private final Label errorLabel;
    private final JFXTextField textField;

    private final ServerMessage model;

    private final Logger log = LoggerFactory.getLogger(EditMessageModal.class);
    private final SessionRestClient restClient;
    private final ViewLoader viewLoader;

    @AssistedInject
    public EditMessageModal(ViewLoader viewLoader,
                            SessionRestClient restClient,
                            @Named("primaryStage") Stage primaryStage,
                            @Assisted Parent root,
                            @Assisted ServerMessage model) {
        super(root, primaryStage);
        this.model = model;
        this.restClient = restClient;
        this.viewLoader = viewLoader;

        setTitle(viewLoader.loadLabel(Constants.LBL_EDIT_MESSAGE));

        saveButton = (JFXButton) view.lookup(EDIT_MESSAGE_BUTTON);
        cancelButton = (JFXButton) view.lookup(CANCEL_BUTTON);
        errorLabel = (Label) view.lookup(ERROR_LABEL);
        textField = (JFXTextField) view.lookup(ENTER_MESSAGE_TEXT_FIELD);

        saveButton.setOnAction(this::onSave);
        saveButton.setDefaultButton(true);
        cancelButton.setOnAction(this::onCancel);
        cancelButton.setCancelButton(true);

        Platform.runLater(() ->  textField.setText(model.getMessage()));
    }

    private void onCancel(ActionEvent actionEvent) {
        this.close();
        setErrorMessage(null);
    }

    private void onSave(ActionEvent actionEvent) {
        setErrorMessage(null);
        if (!textField.getText().isEmpty()) {
            String text = textField.getText();

            textField.setDisable(true);
            saveButton.setDisable(true);
            cancelButton.setDisable(true);

            String serverId = model.getChannel().getServer().getId();
            String categoryId = model.getChannel().getCategory().getId();
            String channelId = model.getChannel().getId();
            String messageId = model.getId();

            restClient.updateMessage(serverId, categoryId, channelId, messageId, text, this::handleEditMessageResponse);
        } else {
            setErrorMessage(Constants.LBL_NO_CHANGES);
        }
    }

    private void handleEditMessageResponse(HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toPrettyString());

        if (response.isSuccess()) {
            Platform.runLater(this::close);
        } else {
            log.error("Edit message failed!");
            setErrorMessage(Constants.LBL_EDIT_MESSAGE_FAILED);

            Platform.runLater(() -> {
                saveButton.setDisable(false);
                cancelButton.setDisable(false);
                textField.setDisable(false);
            });
        }
    }

    private void setErrorMessage(String label) {
        if (Objects.isNull(label)) {
            Platform.runLater(() -> {
                errorLabel.setText("");
            });
            return;
        }
        String message = viewLoader.loadLabel(label);
        Platform.runLater(() -> {
            errorLabel.setText(message);
        });
    }

    @AssistedFactory
    public interface EditMessageModalFactory {
        EditMessageModal create(Parent view, ServerMessage message);
    }
}
