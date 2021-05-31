package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Server;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class CreateCategoryModal extends AbstractModal {
    public static final String CATEGORY_NAME_TEXT_FIELD = "#category-name-text-field";
    public static final String SPINNER = "#spinner";
    public static final String ERROR_MESSAGE_LABEL = "#error-message-label";
    public static final String CREATE_BUTTON = "#create-button";
    public static final String CANCEL_BUTTON = "#cancel-button";

    private final JFXTextField categoryNameTextField;
    private final Label errorLabel;
    private final JFXSpinner spinner;
    private final JFXButton createButton;
    private final JFXButton cancelButton;

    private final Server model;
    private static final Logger log = LoggerFactory.getLogger(CreateCategoryModal.class);

    public CreateCategoryModal(Parent root, Server model) {
        super(root);
        this.model = model;

        setTitle(ViewLoader.loadLabel(Constants.LBL_CREATE_CATEGORY_TITLE));

        categoryNameTextField = (JFXTextField) view.lookup(CATEGORY_NAME_TEXT_FIELD);
        errorLabel = (Label) view.lookup(ERROR_MESSAGE_LABEL);
        spinner = (JFXSpinner) view.lookup(SPINNER);
        createButton = (JFXButton) view.lookup(CREATE_BUTTON);
        cancelButton = (JFXButton) view.lookup(CANCEL_BUTTON);

        createButton.setOnAction(this::onCreateButtonClicked);
        createButton.setDefaultButton(true);  // use Enter in order to press button
        cancelButton.setOnAction(this::onCancelButtonClicked);
        cancelButton.setCancelButton(true);  // use Escape in order to press button
    }

    @Override
    public void close() {
        createButton.setOnAction(null);
        cancelButton.setOnAction(null);
        super.close();
    }

    /**
     * Removes any error message, checks whether anything was typed/changed and then applies this change
     * - when something is written in categoryNameTextField: disables control elements, shows spinner and sends request
     * @param actionEvent
     */
    private void onCreateButtonClicked(ActionEvent actionEvent) {
        setErrorMessage(null);

        //ToDo: Notifications

        if (! categoryNameTextField.getText().isEmpty()){
            String name = categoryNameTextField.getText();

            categoryNameTextField.setDisable(true);
            createButton.setDisable(true);
            cancelButton.setDisable(true);
            spinner.setVisible(true);

            RestClient restClient = NetworkClientInjector.getRestClient();
            restClient.createCategory(model.getId(), name, this::handleCreateCategoryResponse);
        }
        else{
            setErrorMessage(Constants.LBL_MISSING_NAME);
        }
    }

    private void onCancelButtonClicked(ActionEvent actionEvent) {
        this.close();
    }

    private void onDeleteButtonClicked(ActionEvent actionEvent) {
        // ToDo
    }

    /**
     * When successful: new category is created, inserted in model and View is closed
     * When unsuccessful: shows error message, hides spinner and enables control elements
     * @param response
     */
    private void handleCreateCategoryResponse(HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toPrettyString());

        if (response.isSuccess()) {
            JSONObject jsonObject = response.getBody().getObject().getJSONObject("data");
            String categoryId = jsonObject.getString("id");
            String name = jsonObject.getString("name");
            String serverId = jsonObject.getString("server");
            JSONArray channelList = jsonObject.getJSONArray("channels");  // don't know what might be contained in Array

            if(! serverId.equals(model.getId())){
                log.error("Wrong serverId in response!");
                return;
            }
            if(! channelList.isEmpty()){
                log.error("New category already contains channel(s)!");
                return;
            }

            Category newCategory = new Category().setId(categoryId).setName(name);
            newCategory.setServer(model);

            Platform.runLater(this::close);
        } else {
            log.error("Create category failed!");
            setErrorMessage(Constants.LBL_CREATE_CATEGORY_FAILED);

            Platform.runLater(() -> {
                categoryNameTextField.setDisable(false);
                createButton.setDisable(false);
                cancelButton.setDisable(false);
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
        String message = ViewLoader.loadLabel(label);
        Platform.runLater(() -> {
            errorLabel.setText(message);
        });
    }
}
