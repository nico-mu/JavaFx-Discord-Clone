package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.component.InviteList;
import de.uniks.stp.component.InviteListEntry;
import de.uniks.stp.component.UserCheckList;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class InvitesModal extends AbstractModal {
    private static final Logger log = LoggerFactory.getLogger(AddChannelModal.class);

    public static final String INVITE_LIST_CONTAINER = "#invites-list-container";
    public static final String CREATE_BUTTON = "#invites-create";
    public static final String CANCEL_BUTTON = "#invites-cancel";
    public static final String ERROR_LABEL = "#invites-error";

    private VBox inviteListContainer;
    private JFXButton createButton;
    private JFXButton cancelButton;
    private Label errorLabel;
    private InviteList inviteList;

    private RestClient restClient;
    private Server server;

    public InvitesModal(Parent root, Server server) {
        super(root);
        this.restClient = NetworkClientInjector.getRestClient();
        this.server = server;

        setTitle(ViewLoader.loadLabel(Constants.LBL_INVITATIONS));
        inviteListContainer = (VBox) view.lookup(INVITE_LIST_CONTAINER);
        createButton = (JFXButton) view.lookup(CREATE_BUTTON);
        cancelButton = (JFXButton) view.lookup(CANCEL_BUTTON);
        errorLabel = (Label) view.lookup(ERROR_LABEL);

        inviteList = new InviteList();
        inviteList.setMaxHeight(inviteListContainer.getMaxHeight());
        InviteListEntry inviteListEntry = new InviteListEntry();
        inviteList.addInviteListEntry(inviteListEntry);
        inviteListEntry = new InviteListEntry();
        inviteList.addInviteListEntry(inviteListEntry);
        inviteListContainer.getChildren().add(inviteList);

        createButton.setOnAction(this::onCreatButtonClicked);
        cancelButton.setOnAction(this::onCancelButtonClicked);
        cancelButton.setCancelButton(true);  // use Escape in order to press button

        restClient.getServerInvitations(server.getId(), this::handleGetServerInvitationsResponse);
    }

    private void handleGetServerInvitationsResponse(HttpResponse<JsonNode> jsonNodeHttpResponse) {
        log.debug("Received get server invites response: " + jsonNodeHttpResponse.getBody().toPrettyString());
        if(jsonNodeHttpResponse.isSuccess()) {
            JSONArray channelsJson = jsonNodeHttpResponse.getBody().getObject().getJSONArray("data");
            for (Object channel : channelsJson) {
                JSONObject channelJson = (JSONObject) channel;
                final String inviteId = channelJson.getString("id");
                final String inviteLink = channelJson.getString("link");
                final String type = channelJson.getString("type");
                final int max = channelJson.getInt("max");
                final int current = channelJson.getInt("current");
                final String serverId = channelJson.getString("server");
                //TODO add invite
            }
        }
    }

    private void onCreatButtonClicked(ActionEvent actionEvent) {
        setErrorMessage(null);
        Parent createInviteModalView = ViewLoader.loadView(Views.CREATE_INVITE_MODAL);
        CreateInviteModal createInviteModal = new CreateInviteModal(createInviteModalView, server);
        createInviteModal.show();
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
