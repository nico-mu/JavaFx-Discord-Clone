package de.uniks.stp.modal;

import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.ServerMessage;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteMessageModal {

    private final Logger log = LoggerFactory.getLogger(EditMessageModal.class);
    private final ConfirmationModal confirmationModal;
    private final ServerMessage message;
    private final String userKey;

    public DeleteMessageModal(ServerMessage message, String userKey) {
        this.message = message;
        this.userKey = userKey;
        Parent confirmationModalView = ViewLoader.loadView(Views.CONFIRMATION_MODAL);
        confirmationModal = new ConfirmationModal(confirmationModalView,
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
        RestClient restClient = NetworkClientInjector.getRestClient();
        String serverId = this.message.getChannel().getServer().getId();
        String categoryId = this.message.getChannel().getCategory().getId();
        String channelId = this.message.getChannel().getId();
        String messageId = this.message.getId();
        restClient.deleteMessage(serverId, categoryId, channelId, messageId, userKey, this::handleDeleteMessageResponse);
    }

    private void handleDeleteMessageResponse(HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toPrettyString());
        Platform.runLater(confirmationModal::close);
    }
}
