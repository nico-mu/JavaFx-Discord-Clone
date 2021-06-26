package de.uniks.stp.component;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class VoiceChatUser extends HBox {

    private final User user;
    @FXML
    private Label userNameLabel;
    @FXML
    private JFXButton muteAudioButton;

    public VoiceChatUser(final User user) {
        final FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.VOICE_CHAT_USER);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        this.setId(user.getId() + "-VoiceChatUser");

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.user = user;

        setUserName(user.getName());

        muteAudioButton.setOnMouseClicked(this::handlemuteAudioButtonClick);
    }

    private void handlemuteAudioButtonClick(MouseEvent mouseEvent) {

    }

    public void setUserName(final String userName) {
        userNameLabel.setText(userName);
    }
}
