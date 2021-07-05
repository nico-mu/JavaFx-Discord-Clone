package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.ServerInvitation;
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

import java.util.Objects;

public class CreateInviteModal extends AbstractModal {
    private static final Logger log = LoggerFactory.getLogger(CreateInviteModal.class);


    public static final String TIME_CHECKBOX = "#create-invite-time";
    public static final String MAX_CHECKBOX = "#create-invide-max";
    public static final String MAX_TEXTFIELD = "#create-invite-max-textfield";
    public static final String CREATE_BUTTON = "#create-invite-create";
    public static final String CANCEL_BUTTON = "#create-invite-cancel";
    public static final String ERROR_LABEL = "#create-invite-error";

    private final JFXCheckBox timeCheckBox;
    private final JFXCheckBox maxCheckBox;
    private final JFXTextField maxTextField;
    private final JFXButton createButton;
    private final JFXButton cancelButton;
    private final Label errorLabel;

    private final SessionRestClient restClient;
    private final Editor editor;
    private final Server server;
    private final ViewLoader viewLoader;

    @AssistedInject
    public CreateInviteModal(Editor editor,
                             Stage primaryStage,
                             SessionRestClient restClient,
                             ViewLoader viewLoader,
                             @Assisted Parent root,
                             @Assisted Server server) {
        super(root, primaryStage);
        this.restClient = restClient;
        this.viewLoader = viewLoader;
        this.editor = editor;
        this.server = server;

        setTitle(viewLoader.loadLabel(Constants.LBL_CREATE_INVITATION));
        timeCheckBox = (JFXCheckBox) view.lookup(TIME_CHECKBOX);
        maxCheckBox = (JFXCheckBox) view.lookup(MAX_CHECKBOX);
        maxTextField = (JFXTextField) view.lookup(MAX_TEXTFIELD);
        createButton = (JFXButton) view.lookup(CREATE_BUTTON);
        cancelButton = (JFXButton) view.lookup(CANCEL_BUTTON);
        errorLabel = (Label) view.lookup(ERROR_LABEL);

        createButton.setOnAction(this::onCreatButtonClicked);
        cancelButton.setOnAction(this::onCancelButtonClicked);
        cancelButton.setCancelButton(true);  // use Escape in order to press button
        timeCheckBox.setOnAction(this::onTimeCheckBoxClicked);
        maxCheckBox.setOnAction(this::onMaxCheckBoxClicked);
        timeCheckBox.setSelected(true);
        maxTextField.setDisable(true);
        maxTextField.textProperty().addListener(((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                maxTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        }));
    }

    private void onMaxCheckBoxClicked(ActionEvent actionEvent) {
        timeCheckBox.setSelected(!maxCheckBox.isSelected());
        maxTextField.setDisable(false);
    }

    private void onTimeCheckBoxClicked(ActionEvent actionEvent) {
        maxCheckBox.setSelected(!timeCheckBox.isSelected());
        maxTextField.setDisable(true);
    }

    private void onCreatButtonClicked(ActionEvent actionEvent) {
        setErrorMessage(null);
        String type = timeCheckBox.isSelected() ? "temporal" : "count";
        String maxString = type.equals("temporal") ? "-1" : maxTextField.getText();
        if (type.equals("count") && maxString.equals("")) {
            setErrorMessage(Constants.LBL_MISSING_MAX_VALUE);
            return;
        }
        restClient.createServerInvitation(server.getId(), type, Integer.parseInt(maxString), this::handleCreateInvitationResponse);
    }

    private void handleCreateInvitationResponse(HttpResponse<JsonNode> jsonNodeHttpResponse) {
        log.debug("Received create Invite response: " + jsonNodeHttpResponse.getBody().toPrettyString());
        if (jsonNodeHttpResponse.isSuccess()) {
            JSONObject data = jsonNodeHttpResponse.getBody().getObject().getJSONObject("data");
            String invId = data.getString("id");
            String link = data.getString("link");
            String type = data.getString("type");
            int max = data.getInt("max");
            int current = data.getInt("current");
            String serverId = data.getString("server");

            ServerInvitation serverInvitation = new ServerInvitation().setId(invId).setLink(link).setType(type).setMax(max).setCurrent(current);
            Server server = editor.getServer(serverId);
            server.withInvitations(serverInvitation);

            Platform.runLater(this::close);
        } else {
            log.error("Create server failed: " + jsonNodeHttpResponse.getBody().toPrettyString());
            setErrorMessage(Constants.LBL_NOT_SERVER_OWNER);
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

    private void onCancelButtonClicked(ActionEvent actionEvent) {
        this.close();
    }

    @Override
    public void close() {
        createButton.setOnAction(null);
        cancelButton.setOnAction(null);
        super.close();
    }

    @AssistedFactory
    public interface CreateInviteModalFactory {
        CreateInviteModal create(Parent view, Server server);
    }
}
