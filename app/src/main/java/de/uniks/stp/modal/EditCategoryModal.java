package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Category;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
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
    public static final String ERROR_LABEL = "#error-message-label";
    public static final String SPINNER = "#spinner";
    public static final String CANCEL_BUTTON = "#cancel-button";
    public static final String APPLY_BUTTON = "#apply-button";

    private final JFXTextField categoryNameTextField;
    private final Label errorLabel;
    private final JFXSpinner spinner;
    private final JFXButton applyButton;
    private final JFXButton cancelButton;

    final Category model;

    public EditCategoryModal(Parent root, Category model) {
        super(root);
        this.model = model;

        setTitle(ViewLoader.loadLabel(Constants.LBL_EDIT_CATEGORY_TITLE));

        categoryNameTextField = (JFXTextField) view.lookup(NAME_FIELD);
        errorLabel = (Label) view.lookup(ERROR_LABEL);
        spinner = (JFXSpinner) view.lookup(SPINNER);
        applyButton = (JFXButton) view.lookup(APPLY_BUTTON);
        cancelButton = (JFXButton) view.lookup(CANCEL_BUTTON);

        applyButton.setOnAction(this::onApplyButtonClicked);
        applyButton.setDefaultButton(true);  // use Enter in order to press button
        cancelButton.setOnAction(this::onCancelButtonClicked);
        cancelButton.setCancelButton(true);  // use Escape in order to press button
    }

    private void onCancelButtonClicked(ActionEvent actionEvent) {
        this.close();
    }

    private void onApplyButtonClicked(ActionEvent actionEvent) {
        setErrorMessage(null);

        if (!categoryNameTextField.getText().isEmpty()) {
            String name = categoryNameTextField.getText();

            categoryNameTextField.setDisable(true);
            applyButton.setDisable(true);
            cancelButton.setDisable(true);
            spinner.setVisible(true);

            RestClient restClient = NetworkClientInjector.getRestClient();
            restClient.updateCategory(model.getServer().getId(), model.getId(), name, this::handleUpdateCategoryResponse);
        } else {
            setErrorMessage(Constants.LBL_MISSING_NAME);
        }
    }

    private void handleUpdateCategoryResponse(HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toPrettyString());

        if (response.isSuccess()) {
            Platform.runLater(this::close);
        } else {
            log.error("Edit category failed!");
            setErrorMessage(Constants.LBL_EDIT_CATEGORY_FAILED);

            Platform.runLater(() -> {
                categoryNameTextField.setDisable(false);
                applyButton.setDisable(false);
                cancelButton.setDisable(false);
                spinner.setVisible(false);
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
        String message = ViewLoader.loadLabel(label);
        Platform.runLater(() -> {
            errorLabel.setText(message);
        });
    }
}
