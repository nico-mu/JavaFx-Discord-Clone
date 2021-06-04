package de.uniks.stp.component;

import com.jfoenix.controls.JFXCheckBox;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class InviteListEntry extends HBox {

    @FXML
    private Label invite;
    @FXML
    private Label until;
    @FXML
    private Label currentMax;

    public InviteListEntry() {
        final FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.INVITE_LIST_ENTRY);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        invite.setText("test");
    }

}
