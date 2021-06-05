package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.modal.InvitesModal;
import de.uniks.stp.model.ServerInvitation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class InviteListEntry extends HBox {
    private static final Logger log = LoggerFactory.getLogger(InviteListEntry.class);

    @FXML
    private Label invite;
    @FXML
    private Label until;
    @FXML
    private Label currentMax;
    @FXML
    private ImageView copy;
    @FXML
    private ImageView delete;
    private ServerInvitation model;
    private InvitesModal invitesModal;

    public InviteListEntry(ServerInvitation serverInvitation, InvitesModal invitesModal) {
        this.model = serverInvitation;
        this.invitesModal = invitesModal;
        final FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.INVITE_LIST_ENTRY);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        invite.setText(serverInvitation.getLink().substring(serverInvitation.getLink().length() - 15));
        Boolean temporal = serverInvitation.getType().equals("temporal");
        String type = temporal ? ViewLoader.loadLabel(Constants.LBL_CREATE_INVITATION_TIME) : ViewLoader.loadLabel(Constants.LBL_MAX);
        until.setText(type);
        String currentMaxString = serverInvitation.getType().equals("temporal") ? "-" : serverInvitation.getCurrent() + " / " + serverInvitation.getMax();
        currentMax.setText(currentMaxString);
        copy.setOnMouseClicked(this::onCopyClicked);
        delete.setOnMouseClicked(this::onDeleteClicked);
    }

    private void onDeleteClicked(MouseEvent mouseEvent) {
        invitesModal.setErrorMessage(null);
        invitesModal.onDeleteClicked(model);
    }

    private void onCopyClicked(MouseEvent mouseEvent) {
        invitesModal.setErrorMessage(null);
        String link = model.getLink();
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(link);
        clipboard.setContent(content);
    }

}
