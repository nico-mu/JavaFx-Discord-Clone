package de.uniks.stp.component;

import com.jfoenix.controls.JFXButton;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
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

    private final Image initAudioInputImg;
    private final Image otherAudioInputImg;
    private final User user;
    @FXML
    private Label userNameLabel;
    @FXML
    private JFXButton muteAudioButton;
    private final ImageView userMuteImgView;
    private boolean userMute = false;

    @AssistedInject
    public VoiceChatUserEntry(@Assisted final User user, ViewLoader viewLoader) {
        initAudioInputImg = viewLoader.loadImage("microphone.png");
        otherAudioInputImg = viewLoader.loadImage("microphone-mute.png");

        final FXMLLoader fxmlLoader = viewLoader.getFXMLComponentLoader(Components.VOICE_CHAT_USER);
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
        muteAudioButton.setId(user.getId() + "-MuteVoiceChatUserBtn");
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

    @AssistedFactory
    public interface VoiceChatUserEntryFactory {
        VoiceChatUserEntry create(User user);
    }
}
