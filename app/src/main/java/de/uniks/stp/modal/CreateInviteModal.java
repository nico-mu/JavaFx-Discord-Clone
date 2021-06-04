package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.component.InviteList;
import de.uniks.stp.component.InviteListEntry;
import de.uniks.stp.component.UserCheckList;
import de.uniks.stp.model.Server;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class CreateInviteModal extends AbstractModal {
    private static final Logger log = LoggerFactory.getLogger(AddChannelModal.class);


    public static final String TIME_CHECKBOX = "#create-invite-time";
    public static final String MAX_CHECKBOX = "#create-invide-max";
    public static final String MAX_TEXTFIELD = "#create-invite-max-textfield";
    public static final String CREATE_BUTTON = "#create-invite-create";
    public static final String CANCEL_BUTTON = "#create-invite-cancel";
    public static final String ERROR_LABEL = "#create-invite-error";

    private JFXCheckBox timeCheckBox;
    private JFXCheckBox maxCheckBox;
    private JFXTextField maxTextField;
    private JFXButton createButton;
    private JFXButton cancelButton;
    private Label errorLabel;

    private RestClient restClient;
    private Server server;

    public CreateInviteModal(Parent root, Server server) {
        super(root);
        this.restClient = NetworkClientInjector.getRestClient();
        this.server = server;

        setTitle(ViewLoader.loadLabel(Constants.LBL_CREATE_INVITATION));
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
        maxTextField.textProperty().addListener(((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                maxTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        }));
    }

    private void onMaxCheckBoxClicked(ActionEvent actionEvent) {
        timeCheckBox.setSelected(!maxCheckBox.isSelected());
    }

    private void onTimeCheckBoxClicked(ActionEvent actionEvent) {
        maxCheckBox.setSelected(!timeCheckBox.isSelected());
    }

    private void onCreatButtonClicked(ActionEvent actionEvent) {
        setErrorMessage(null);
        String type = timeCheckBox.isSelected() ? "temporal" : "count";
        restClient.createServerInvitation(server.getId(), type, Integer.parseInt(maxTextField.getText()), this::handleCreateInvitationResponse);
    }

    private void handleCreateInvitationResponse(HttpResponse<JsonNode> jsonNodeHttpResponse) {
        log.debug("Received create Invite response: " + jsonNodeHttpResponse.getBody().toPrettyString());

        if (jsonNodeHttpResponse.isSuccess()) {
            Platform.runLater(this::close);
            //TODO Add Invite to model
        } else {
            log.error("Create server failed: " + jsonNodeHttpResponse.getBody().toPrettyString());
            setErrorMessage("Can not create invitation");
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

    private void onCancelButtonClicked(ActionEvent actionEvent) {
        this.close();
    }

    @Override
    public void close() {
        createButton.setOnAction(null);
        cancelButton.setOnAction(null);
        super.close();
    }
}
