package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.model.Category;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class EditCategoryModal extends AbstractModal {
    private static final Logger log = LoggerFactory.getLogger(EditCategoryModal.class);

    public static final String NAME_FIELD = "#category-name-text-field";
    public static final String NOTIFICATIONS_TOGGLE_BUTTON = "#notifications-toggle-button";
    public static final String NOTIFICATIONS_ACTIVATED_LABEL = "#notifications-activated-label";
    public static final String ERROR_LABEL = "#error-message-label";
    public static final String SPINNER = "#spinner";
    public static final String CANCEL_BUTTON = "#cancel-button";
    public static final String SAVE_BUTTON = "#save-button";
    public static final String DELETE_BUTTON = "#delete-button";

    private final JFXTextField categoryNameTextField;
    private final JFXToggleButton notificationsToggleButton;
    private final Label notificationsLabel;
    private final Label errorLabel;
    private final JFXSpinner spinner;
    private final JFXButton saveButton;
    private final JFXButton cancelButton;
    private final JFXButton deleteButton;

    final Category model;
    private ConfirmationModal deleteConfirmationModal;

    public EditCategoryModal(Parent root, Category model) {
        super(root);
        this.model = model;

        setTitle(ViewLoader.loadLabel(Constants.LBL_EDIT_CATEGORY_TITLE));

        categoryNameTextField = (JFXTextField) view.lookup(NAME_FIELD);
        notificationsToggleButton = (JFXToggleButton) view.lookup(NOTIFICATIONS_TOGGLE_BUTTON);
        notificationsLabel = (Label) view.lookup(NOTIFICATIONS_ACTIVATED_LABEL);
        errorLabel = (Label) view.lookup(ERROR_LABEL);
        spinner = (JFXSpinner) view.lookup(SPINNER);
        saveButton = (JFXButton) view.lookup(SAVE_BUTTON);
        cancelButton = (JFXButton) view.lookup(CANCEL_BUTTON);
        deleteButton = (JFXButton) view.lookup(DELETE_BUTTON);

        boolean muted = DatabaseService.isCategoryMuted(model.getId());
        notificationsToggleButton.setSelected(!muted);
        notificationsLabel.setText(ViewLoader.loadLabel(muted ? Constants.LBL_OFF : Constants.LBL_ON));
        notificationsToggleButton.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            notificationsLabel.setText(ViewLoader.loadLabel(!notificationsToggleButton.isSelected() ? Constants.LBL_OFF : Constants.LBL_ON));
        }));

        categoryNameTextField.setText(model.getName());

        saveButton.setOnAction(this::onSaveButtonClicked);
        saveButton.setDefaultButton(true);  // use Enter in order to press button
        cancelButton.setOnAction(this::onCancelButtonClicked);
        cancelButton.setCancelButton(true);  // use Escape in order to press button
        deleteButton.setOnAction(this::onDeleteButtonClicked);
    }

    private void onSaveButtonClicked(ActionEvent actionEvent) {
        setErrorMessage(null);

        if (!categoryNameTextField.getText().isEmpty()) {
            String name = categoryNameTextField.getText();

            categoryNameTextField.setDisable(true);
            notificationsToggleButton.setDisable(true);
            saveButton.setDisable(true);
            cancelButton.setDisable(true);
            deleteButton.setDisable(true);
            spinner.setVisible(true);

            boolean muted = !notificationsToggleButton.isSelected();
            if(muted) {
                DatabaseService.addMutedCategoryId(model.getId());
            }else {
                DatabaseService.removeMutedCategoryId(model.getId());
            }

            if(categoryNameTextField.getText().equals(model.getName())) {
                this.close();
                return;
            }

            RestClient restClient = NetworkClientInjector.getRestClient();
            restClient.updateCategory(model.getServer().getId(), model.getId(), name, this::handleRenameCategoryResponse);
        } else {
            setErrorMessage(Constants.LBL_NO_CHANGES);
        }
    }

    private void handleRenameCategoryResponse(HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toPrettyString());

        if (response.isSuccess()) {
            Platform.runLater(this::close);
        } else {
            log.error("Rename category failed!");
            setErrorMessage(Constants.LBL_RENAME_CATEGORY_FAILED);

            Platform.runLater(() -> {
                categoryNameTextField.setDisable(false);
                //notificationsToggleButton.setDisable(false);  use when fixed
                saveButton.setDisable(false);
                cancelButton.setDisable(false);
                deleteButton.setDisable(false);
                spinner.setVisible(false);
            });
        }
    }

    /**
     * Shows ConfirmationModal
     * @param actionEvent
     */
    private void onDeleteButtonClicked(ActionEvent actionEvent) {
        Parent confirmationModalView = ViewLoader.loadView(Views.CONFIRMATION_MODAL);
        deleteConfirmationModal = new ConfirmationModal(confirmationModalView,
            Constants.LBL_DELETE_CATEGORY,
            Constants.LBL_CONFIRM_DELETE_CATEGORY,
            this::onYesButtonClicked,
            this::onNoButtonClicked);
        deleteConfirmationModal.show();

        // disabling buttons improves the view
        saveButton.setDisable(true);
        cancelButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    /**
     * Used as onAction Method of Yes-Button in ConfirmationModal for category deletion
     * @param actionEvent
     */
    private void onYesButtonClicked(ActionEvent actionEvent) {
        Platform.runLater(deleteConfirmationModal::close);

        categoryNameTextField.setDisable(true);
        //notificationsToggleButton.setDisable(true);  use when fixed
        saveButton.setDisable(true);
        cancelButton.setDisable(true);
        deleteButton.setDisable(true);
        spinner.setVisible(true);

        RestClient restClient = NetworkClientInjector.getRestClient();
        restClient.deleteCategory(model.getServer().getId(), model.getId(), this::handleDeleteCategoryResponse);
    }

    /**
     * Used as onAction Method of No-Button in ConfirmationModal for category deletion
     * @param actionEvent
     */
    private void onNoButtonClicked(ActionEvent actionEvent) {
        Platform.runLater(deleteConfirmationModal::close);
        saveButton.setDisable(false);
        cancelButton.setDisable(false);
        deleteButton.setDisable(false);
    }

    /**
     * when successful: View is closed, category is deleted when WebSocketMessage is received
     * When unsuccessful: shows error message, hides spinner and enables control elements
     * @param response
     */
    private void handleDeleteCategoryResponse(HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toPrettyString());

        if (response.isSuccess()) {
            Platform.runLater(this::close);
        } else {
            log.error("Delete category failed!");
            setErrorMessage(Constants.LBL_DELETE_CATEGORY_FAILED);

            Platform.runLater(() -> {
                categoryNameTextField.setDisable(false);
                //notificationsToggleButton.setDisable(false);  use when fixed
                saveButton.setDisable(false);
                cancelButton.setDisable(false);
                deleteButton.setDisable(false);
                spinner.setVisible(false);
            });
        }
    }

    private void onCancelButtonClicked(ActionEvent actionEvent) {
        this.close();
    }

    private void setErrorMessage(String label) {
        if (Objects.isNull(label)) {
            Platform.runLater(() -> {
                errorLabel.setText("");
            });
            return;
        }
        String message = ViewLoader.loadLabel(label);
        Platform.runLater(() -> {
            errorLabel.setText(message);
        });
    }
}
