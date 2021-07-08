package de.uniks.stp.component;

import com.jfoenix.controls.JFXButton;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.util.InviteInfo;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class JoinServerButton extends HBox {
    @FXML
    private JFXButton joinServerButton;

    @AssistedInject
    public JoinServerButton(ViewLoader viewLoader,
                            @Assisted InviteInfo inviteInfo,
                            @Assisted EventHandler<ActionEvent> handleButtonPressed) {
        FXMLLoader fxmlLoader = viewLoader.getFXMLComponentLoader(Components.JOIN_SERVER_BUTTON);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        joinServerButton.setId(inviteInfo.getServerId() + "-" + inviteInfo.getInviteId());
        joinServerButton.setUserData(inviteInfo);
        joinServerButton.setOnAction(handleButtonPressed);
    }

    @AssistedFactory
    public interface JoinServerButtonFactory {
        JoinServerButton create(InviteInfo inviteInfo, EventHandler<ActionEvent> handleButtonPressed);
    }
}
