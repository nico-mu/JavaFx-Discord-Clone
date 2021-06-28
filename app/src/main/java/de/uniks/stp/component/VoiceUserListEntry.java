package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class VoiceUserListEntry extends HBox {

    @FXML
    private Label voiceUserNameLabel;

    public VoiceUserListEntry(final User user) {
        final FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.VOICE_USER_LIST_ENTRY);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        this.setId(user.getId() + "-UserListEntry");

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        setUserName(user.getName());
    }

    public void setUserName(final String userName) {
        voiceUserNameLabel.setText(userName);
    }
}
