package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.model.Server;
import de.uniks.stp.network.rest.SessionRestClient;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Objects;

public class ServerSettingsModal extends AbstractModal {
    public static final String SERVERNAME_TEXT_FIELD = "#servername-text-field";
    public static final String NOTIFICATIONS_TOGGLE_BUTTON = "#notifications-toggle-button";
    public static final String NOTIFICATIONS_ACTIVATED_LABEL = "#notifications-activated-label";
    public static final String SPINNER = "#spinner";
    public static final String ERROR_MESSAGE_LABEL = "#error-message-label";
    public static final String SAVE_BUTTON = "#save-button";
    public static final String CANCEL_BUTTON = "#cancel-button";
    public static final String DELETE_BUTTON = "#delete-button";

    private final JFXTextField servernameTextField;
    private final JFXToggleButton notificationsToggleButton;
    private final Label notificationsLabel;
    private final Label errorLabel;
    private final JFXSpinner spinner;
    private final JFXButton saveButton;
    private final JFXButton cancelButton;
    private final JFXButton deleteButton;

    private final Server model;
    private final ViewLoader viewLoader;
    private final SessionDatabaseService databaseService;
    private final SessionRestClient restClient;
    private ConfirmationModal confirmationModal;
    private static final Logger log = LoggerFactory.getLogger(ServerSettingsModal.class);
    private final boolean owner;

    @Inject
    ConfirmationModal.ConfirmationModalFactory confirmationModalFactory;

    public ServerSettingsModal(SessionDatabaseService databaseService,
                               ViewLoader viewLoader,
                               SessionRestClient restClient,
                               @Assisted Parent root,
                               @Assisted Server model,
                               @Assisted boolean owner) {
        super(root);
        this.model = model;
        this.owner = owner;
        this.viewLoader = viewLoader;
        this.databaseService = databaseService;
        this.restClient = restClient;

        setTitle(viewLoader.loadLabel(Constants.LBL_EDIT_SERVER_TITLE));

        servernameTextField = (JFXTextField) view.lookup(SERVERNAME_TEXT_FIELD);
        notificationsToggleButton = (JFXToggleButton) view.lookup(NOTIFICATIONS_TOGGLE_BUTTON);
        notificationsLabel = (Label) view.lookup(NOTIFICATIONS_ACTIVATED_LABEL);
        errorLabel = (Label) view.lookup(ERROR_MESSAGE_LABEL);
        spinner = (JFXSpinner) view.lookup(SPINNER);
        saveButton = (JFXButton) view.lookup(SAVE_BUTTON);
        cancelButton = (JFXButton) view.lookup(CANCEL_BUTTON);
        deleteButton = (JFXButton) view.lookup(DELETE_BUTTON);

        boolean muted = databaseService.isServerMuted(model.getId());
        notificationsToggleButton.setSelected(!muted);
        notificationsLabel.setText(viewLoader.loadLabel(muted ? Constants.LBL_OFF : Constants.LBL_ON));
        notificationsToggleButton.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            notificationsLabel.setText(viewLoader.loadLabel(!notificationsToggleButton.isSelected() ? Constants.LBL_OFF : Constants.LBL_ON));
        }));

        servernameTextField.setText(model.getName());

        saveButton.setOnAction(this::onSaveButtonClicked);
        saveButton.setDefaultButton(true);  // use Enter in order to press button
        cancelButton.setOnAction(this::onCancelButtonClicked);
        cancelButton.setCancelButton(true);  // use Escape in order to press button

        if (!this.owner) {
            deleteButton.setText(viewLoader.loadLabel(Constants.LBL_LEAVE_SERVER));
            deleteButton.setOnAction(this::onLeaveButtonClicked);
        } else {
            deleteButton.setOnAction(this::onDeleteButtonClicked);
        }
    }

    @Override
    public void close() {
        saveButton.setOnAction(null);
        cancelButton.setOnAction(null);
        deleteButton.setOnAction(null);
        super.close();
    }

    /**
     * Removes any error message, checks whether anything was typed/changed and then applies this change
     * - when something is written in servernameTextField: disables control elements, shows spinner and sends rename request
     * @param actionEvent
     */
    private void onSaveButtonClicked(ActionEvent actionEvent) {
        setErrorMessage(null);

        if (! servernameTextField.getText().isEmpty()){
            String name = servernameTextField.getText();

            servernameTextField.setDisable(true);
            notificationsToggleButton.setDisable(true);
            saveButton.setDisable(true);
            cancelButton.setDisable(true);
            deleteButton.setDisable(true);
            spinner.setVisible(true);

            boolean muted = !notificationsToggleButton.isSelected();
            if(muted) {
                databaseService.addMutedServerId(model.getId());
            }else  {
                databaseService.removeMutedServerId(model.getId());
            }

            if(servernameTextField.getText().equals(model.getName())) {
                this.close();
                return;
            }
            restClient.renameServer(model.getId(), name, this::handleRenameServerResponse);
        }
        else{
            setErrorMessage(Constants.LBL_NO_CHANGES);
        }
    }

    private void onCancelButtonClicked(ActionEvent actionEvent) {
        this.close();
    }

    /**
     * Shows ConfirmationModal
     * @param actionEvent
     */
    private void onDeleteButtonClicked(ActionEvent actionEvent) {
        Parent confirmationModalView = viewLoader.loadView(Views.CONFIRMATION_MODAL);
        confirmationModal = confirmationModalFactory.create(confirmationModalView,
            Constants.LBL_DELETE_SERVER,
            Constants.LBL_CONFIRM_DELETE_SERVER,
            this::onYesDeleteButtonClicked,
            this::onNoButtonClicked);
        confirmationModal.show();

        // disabling buttons improves the view
        saveButton.setDisable(true);
        cancelButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    /**
     * Used as onAction Method of Yes-Button in ConfirmationModal for server deletion
     * @param actionEvent
     */
    private void onYesDeleteButtonClicked(ActionEvent actionEvent) {
        closeConfirmationModel();
        restClient.deleteServer(model.getId(), this::handleDeleteServerResponse);
    }

    /**
     * Used as onAction Method of No-Button in ConfirmationModal for server deletion
     * @param actionEvent
     */
    private void onNoButtonClicked(ActionEvent actionEvent) {
        Platform.runLater(confirmationModal::close);
        saveButton.setDisable(false);
        cancelButton.setDisable(false);
        deleteButton.setDisable(false);
    }

    private void onLeaveButtonClicked(ActionEvent actionEvent) {
        Parent confirmationModalView = viewLoader.loadView(Views.CONFIRMATION_MODAL);
        confirmationModal = confirmationModalFactory.create(confirmationModalView,
            Constants.LBL_LEAVE_SERVER,
            Constants.LBL_CONFIRM_LEAVE_SERVER,
            this::onYesLeaveButtonClicked,
            this::onNoButtonClicked);
        confirmationModal.show();

        // disabling buttons improves the view
        saveButton.setDisable(true);
        cancelButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    private void closeConfirmationModel() {
        Platform.runLater(confirmationModal::close);

        servernameTextField.setDisable(true);
        //notificationsToggleButton.setDisable(true);  use when fixed
        saveButton.setDisable(true);
        cancelButton.setDisable(true);
        deleteButton.setDisable(true);
        spinner.setVisible(true);
    }

    private void onYesLeaveButtonClicked(ActionEvent actionEvent) {
        closeConfirmationModel();
        restClient.leaveServer(model.getId(), this::handleLeaveServerResponse);
    }

    private void handleLeaveServerResponse(HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toPrettyString());
        if (response.isSuccess()) {
            Platform.runLater(this::close);
        } else {
            log.error("leaving server failed!");
            setErrorMessage(Constants.LBL_LEAVE_SERVER_FAILED);

            Platform.runLater(() -> {
                servernameTextField.setDisable(false);
                //notificationsToggleButton.setDisable(false);  use when fixed
                saveButton.setDisable(false);
                cancelButton.setDisable(false);
                deleteButton.setDisable(false);
                spinner.setVisible(false);
            });
        }
    }

    /**
     * when successful: View is closed, server is deleted when WebSocketMessage is received
     * When unsuccessful: shows error message, hides spinner and enables control elements
     * @param response
     */
    private void handleDeleteServerResponse(HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toPrettyString());

        if (response.isSuccess()) {
            Platform.runLater(this::close);
        } else {
            log.error("delete server failed!");
            setErrorMessage(Constants.LBL_DELETE_SERVER_FAILED);

            Platform.runLater(() -> {
                servernameTextField.setDisable(false);
                //notificationsToggleButton.setDisable(false);  use when fixed
                saveButton.setDisable(false);
                cancelButton.setDisable(false);
                deleteButton.setDisable(false);
                spinner.setVisible(false);
            });
        }
    }

    /**
     * When successful: server model is renamed and View is closed
     * When unsuccessful: shows error message, hides spinner and enables control elements
     * @param response
     */
    private void handleRenameServerResponse(HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toPrettyString());

        if (response.isSuccess()) {
            JSONObject jsonObject = response.getBody().getObject().getJSONObject("data");
            String name = jsonObject.getString("name");
            model.setName(name);

            Platform.runLater(this::close);
        } else {
            log.error("rename server failed!");
            setErrorMessage(Constants.LBL_RENAME_SERVER_FAILED);

            Platform.runLater(() -> {
                servernameTextField.setDisable(false);
                //notificationsToggleButton.setDisable(false);  use when fixed
                saveButton.setDisable(false);
                cancelButton.setDisable(false);
                deleteButton.setDisable(false);
                spinner.setVisible(false);
            });
        }
    }

    /**
     * Changes ErrorLabel to display given error.
     *
     * @param label Constant of resource label
     */
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
    public interface ServerSettingsModalFactory {
        ServerSettingsModal create(Parent view, Server server, boolean owner);
    }
}
