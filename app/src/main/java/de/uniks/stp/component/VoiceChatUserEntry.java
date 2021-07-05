package de.uniks.stp.component;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class VoiceChatUserEntry extends VBox {

    private static final Image initAudioInputImg = ViewLoader.loadImage("microphone.png");
    private static final Image otherAudioInputImg = ViewLoader.loadImage("microphone-mute.png");
    private final User user;
    @FXML
    private Label userNameLabel;
    @FXML
    private JFXButton muteAudioButton;
    private final ImageView userMuteImgView;
    private boolean userMute = false;

    public VoiceChatUserEntry(final User user) {
        final FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.VOICE_CHAT_USER);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        this.setId(user.getId() + "-VoiceChatUser");

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        userMuteImgView = (ImageView) muteAudioButton.getGraphic();
        this.user = user;
        setUserName(user.getName());
        setMute(user.isMute());
        muteAudioButton.setOnMouseClicked(this::handleMuteAudioButtonClick);
    }

    private void handleMuteAudioButtonClick(MouseEvent mouseEvent) {
        final boolean isMute = user.isMute();
        user.setMute(!isMute);
    }

    public void setUserName(final String userName) {
        userNameLabel.setText(userName);
    }

    public void setMute(final boolean userMute) {
        if (this.userMute != userMute) {
            this.userMute = userMute;
            Image nextImg;
            if (userMute) {
                nextImg = otherAudioInputImg;
            } else {
                nextImg = initAudioInputImg;
            }
            userMuteImgView.setImage(nextImg);
        }
    }
}
