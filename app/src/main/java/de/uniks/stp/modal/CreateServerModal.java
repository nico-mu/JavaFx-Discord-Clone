package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Server;
import de.uniks.stp.network.rest.ServerInformationHandler;
import de.uniks.stp.network.rest.SessionRestClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.Objects;

public class CreateServerModal extends AbstractModal {
    public static final String ADD_SERVER_CREATE_BUTTON = "#add-server-create-button";
    public static final String ADD_SERVER_CANCEL_BUTTON = "#add-server-cancel-button";
    public static final String ADD_SERVER_TEXT_FIELD_SERVERNAME = "#servername-text-field";
    public static final String ADD_SERVER_ERROR_LABEL = "#error-message-label";
    public static final String ADD_SERVER_SPINNER = "#spinner";

    private final Editor editor;
    private final JFXButton createButton;
    private final JFXButton cancelButton;
    private final JFXTextField servernameTextField;
    private final Label errorLabel;
    private final JFXSpinner spinner;
    private final SessionRestClient restClient;
    private final ServerInformationHandler serverInformationHandler;

    private static final Logger log = LoggerFactory.getLogger(CreateServerModal.class);
    private final ViewLoader viewLoader;

    @AssistedInject
    public CreateServerModal(Editor editor,
                             SessionRestClient restClient,
                             @Named("primaryStage") Stage primaryStage,
                             ServerInformationHandler informationHandler,
                             ViewLoader viewLoader,
                             @Assisted Parent root) {
        super(root, primaryStage);
        this.editor = editor;
        this.restClient = restClient;
        this.viewLoader = viewLoader;
        this.serverInformationHandler = informationHandler;

        setTitle(viewLoader.loadLabel(Constants.LBL_ADD_SERVER_TITLE));

        createButton = (JFXButton) view.lookup(ADD_SERVER_CREATE_BUTTON);
        cancelButton = (JFXButton) view.lookup(ADD_SERVER_CANCEL_BUTTON);
        servernameTextField = (JFXTextField) view.lookup(ADD_SERVER_TEXT_FIELD_SERVERNAME);
        errorLabel = (Label) view.lookup(ADD_SERVER_ERROR_LABEL);
        spinner = (JFXSpinner) view.lookup(ADD_SERVER_SPINNER);

        createButton.setOnAction(this::onApplyButtonClicked);
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

    private void onCancelButtonClicked(ActionEvent actionEvent) {
        this.close();
    }

    /**
     * If TextField is not empty, disables TextField and Button, shows Spinner and sends rest call to create server
     * If TextField is empty, it shows an error message
     * @param actionEvent
     */
    private void onApplyButtonClicked(ActionEvent actionEvent) {
        String name = servernameTextField.getText();
        if (! name.isEmpty()){
            setErrorMessage(null);
            servernameTextField.setDisable(true);
            createButton.setDisable(true);
            cancelButton.setDisable(true);
            spinner.setVisible(true);

            restClient.createServer(name, this::handleCreateServerResponse);
        }
        else{
            setErrorMessage(Constants.LBL_MISSING_NAME);
        }
    }

    /**
     * If successful, it creates new Server Object, inserts it in model and closes this Modal Window.
     * If not successful, it shows an error message, hides the spinner and enables the TextField and Buttons.
     * @param response
     */
    private void handleCreateServerResponse(HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toPrettyString());

        if (response.isSuccess()) {
            JSONObject jsonObject = response.getBody().getObject().getJSONObject("data");
            String name = jsonObject.getString("name");
            String serverId = jsonObject.getString("id");

            Server server = new Server().setName(name).setId(serverId);
            editor.getOrCreateAccord()
                .getCurrentUser()
                .withAvailableServers(server);
            Platform.runLater(this::close);
            restClient.getServerInformation(serverId, serverInformationHandler::handleServerInformationRequest);
            restClient.getCategories(server.getId(), (msg) -> serverInformationHandler.handleCategories(msg, server));
        } else {
            log.error("create server failed!");
            setErrorMessage(Constants.LBL_CREATE_SERVER_FAILED);

            Platform.runLater(() -> {
                servernameTextField.setDisable(false);
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
        String message = viewLoader.loadLabel(label);
        Platform.runLater(() -> {
            errorLabel.setText(message);
        });
    }

    @AssistedFactory
    public interface CreateServerModalFactory {
        CreateServerModal create(Parent view);
    }
}
