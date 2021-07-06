package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.component.InviteListEntry;
import de.uniks.stp.component.ListComponent;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.ServerInvitation;
import de.uniks.stp.network.rest.SessionRestClient;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

public class InvitesModal extends AbstractModal {
    private static final Logger log = LoggerFactory.getLogger(InvitesModal.class);

    public static final String INVITE_LIST_CONTAINER = "#invites-list-container";
    public static final String CREATE_BUTTON = "#invites-create";
    public static final String CANCEL_BUTTON = "#invites-cancel";
    public static final String ERROR_LABEL = "#invites-error";

    private final VBox inviteListContainer;
    private final JFXButton createButton;
    private final JFXButton cancelButton;
    private final Label errorLabel;
    private final ListComponent<ServerInvitation, InviteListEntry> inviteList;
    private final ViewLoader viewLoader;
    private final InviteListEntry.InviteListEntryFactory inviteListEntryFactory;

    private SessionRestClient restClient;
    private final Editor editor;
    private final Server server;

    @Inject
    CreateInviteModal.CreateInviteModalFactory createInviteModalFactory;

    PropertyChangeListener serverInvitationListener = this::onServerInvitationsChanged;

    @AssistedInject
    public InvitesModal(Editor editor,
                        SessionRestClient restClient,
                        ViewLoader viewLoader,
                        @Named("primaryStage") Stage primaryStage,
                        InviteListEntry.InviteListEntryFactory inviteListEntryFactory,
                        @Assisted Parent root,
                        @Assisted Server server) {
        super(root, primaryStage);
        this.editor = editor;
        this.server = server;
        this.restClient = restClient;
        this.viewLoader = viewLoader;
        this.inviteListEntryFactory = inviteListEntryFactory;

        setTitle(viewLoader.loadLabel(Constants.LBL_INVITATIONS));
        inviteListContainer = (VBox) view.lookup(INVITE_LIST_CONTAINER);
        createButton = (JFXButton) view.lookup(CREATE_BUTTON);
        cancelButton = (JFXButton) view.lookup(CANCEL_BUTTON);
        errorLabel = (Label) view.lookup(ERROR_LABEL);

        inviteList = new ListComponent<>(viewLoader);
        inviteList.setMaxHeight(inviteListContainer.getMaxHeight());
        inviteListContainer.getChildren().add(inviteList);

        createButton.setOnAction(this::onCreateButtonClicked);
        cancelButton.setOnAction(this::onCancelButtonClicked);
        cancelButton.setCancelButton(true);  // use Escape in order to press button

        server.listeners().addPropertyChangeListener(Server.PROPERTY_INVITATIONS, serverInvitationListener);
        for (ServerInvitation serverInvitation : server.getInvitations()) {
            invitationAdded(serverInvitation);
        }
        restClient.getServerInvitations(server.getId(), this::handleGetServerInvitationsResponse);
    }

    private void handleGetServerInvitationsResponse(HttpResponse<JsonNode> jsonNodeHttpResponse) {
        log.debug("Received get server invites response: " + jsonNodeHttpResponse.getBody().toPrettyString());
        if (jsonNodeHttpResponse.isSuccess()) {
            JSONArray channelsJson = jsonNodeHttpResponse.getBody().getObject().getJSONArray("data");
            for (Object channel : channelsJson) {
                JSONObject channelJson = (JSONObject) channel;
                final String inviteId = channelJson.getString("id");
                final String inviteLink = channelJson.getString("link");
                final String type = channelJson.getString("type");
                final int max = channelJson.getInt("max");
                final int current = channelJson.getInt("current");
                final String serverId = channelJson.getString("server");
                editor.getOrCreateServerInvitation(inviteId, inviteLink, type, max, current, serverId);
            }
        } else {
            setErrorMessage(Constants.LBL_NOT_SERVER_OWNER);
        }
    }

    private void onServerInvitationsChanged(PropertyChangeEvent propertyChangeEvent) {
        final ServerInvitation newValue = (ServerInvitation) propertyChangeEvent.getNewValue();
        final ServerInvitation oldValue = (ServerInvitation) propertyChangeEvent.getOldValue();

        if (Objects.isNull(oldValue)) {
            invitationAdded(newValue);
        } else if (Objects.isNull(newValue)) {
            invitationRemoved(oldValue);
        }
    }

    private void invitationRemoved(ServerInvitation oldValue) {
        if (Objects.nonNull(oldValue)) {
            Platform.runLater(() -> inviteList.removeElement(oldValue));
        }
    }

    private void invitationAdded(ServerInvitation newValue) {
        if (Objects.nonNull(newValue)) {
            InviteListEntry inviteListEntry = inviteListEntryFactory.create(newValue, this);
            Platform.runLater(() -> inviteList.addElement(newValue, inviteListEntry));
        }
    }

    private void onCreateButtonClicked(ActionEvent actionEvent) {
        setErrorMessage(null);
        Parent createInviteModalView = viewLoader.loadView(Views.CREATE_INVITE_MODAL);
        CreateInviteModal createInviteModal = createInviteModalFactory.create(createInviteModalView, server);
        createInviteModal.show();
    }

    public void setErrorMessage(String label) {
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

    public void onDeleteClicked(ServerInvitation invite) {
        restClient.deleteServerInvitation(invite.getServer().getId(), invite.getId(), this::handleDeleteServerResponse);
    }

    private void handleDeleteServerResponse(HttpResponse<JsonNode> jsonNodeHttpResponse) {
        log.debug("Received delete server invite response: " + jsonNodeHttpResponse.getBody().toPrettyString());
        if (jsonNodeHttpResponse.isSuccess()) {
            JSONObject data = jsonNodeHttpResponse.getBody().getObject().getJSONObject("data");
            String serverId = data.getString("server");
            String invId = data.getString("id");
            Server server = editor.getServer(serverId);
            for (ServerInvitation serverInvitation : server.getInvitations()) {
                if (serverInvitation.getId().equals(invId)) {
                    server.withoutInvitations(serverInvitation);
                    break;
                }
            }
        } else {
            log.error("Received delete server invite response: " + jsonNodeHttpResponse.getBody().toPrettyString());
            setErrorMessage(Constants.LBL_CANT_DELETE_INVITATION);
        }
    }

    @Override
    public void close() {
        createButton.setOnAction(null);
        cancelButton.setOnAction(null);
        server.listeners().removePropertyChangeListener(Server.PROPERTY_INVITATIONS, serverInvitationListener);
        super.close();
    }

    @AssistedFactory
    public interface InvitesModalFactory {
        InvitesModal create(Parent view, Server server);
    }
}
