package de.uniks.stp.modal;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.ServerMessage;
import de.uniks.stp.network.rest.SessionRestClient;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class DeleteMessageModal {

    private final Logger log = LoggerFactory.getLogger(EditMessageModal.class);
    private final ConfirmationModal confirmationModal;
    private final ServerMessage message;
    private final SessionRestClient restClient;

    @Inject
    ConfirmationModal.ConfirmationModalFactory confirmationModalFactory;

    @AssistedInject
    public DeleteMessageModal(ViewLoader viewLoader,
                              SessionRestClient restClient,
                              @Assisted ServerMessage message) {

        this.message = message;
        this.restClient = restClient;

        Parent confirmationModalView = viewLoader.loadView(Views.CONFIRMATION_MODAL);
        confirmationModal = confirmationModalFactory.create(confirmationModalView,
            Constants.LBL_DELETE_MESSAGE_TITLE,
            Constants.LBL_CONFIRM_DELETE_MESSAGE,
            this::onYesDeleteButtonClicked,
            this::onNoButtonClicked);
        confirmationModal.show();
    }

    private void onNoButtonClicked(ActionEvent actionEvent) {
        Platform.runLater(confirmationModal::close);
    }

    private void onYesDeleteButtonClicked(ActionEvent actionEvent) {
        String serverId = this.message.getChannel().getServer().getId();
        String categoryId = this.message.getChannel().getCategory().getId();
        String channelId = this.message.getChannel().getId();
        String messageId = this.message.getId();
        restClient.deleteMessage(serverId, categoryId, channelId, messageId, this::handleDeleteMessageResponse);
    }

    private void handleDeleteMessageResponse(HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toPrettyString());
        Platform.runLater(confirmationModal::close);
    }

    @AssistedFactory
    public interface DeleteMessageModalFactory {
        DeleteMessageModal create(ServerMessage message);
    }
}
