package de.uniks.stp.component;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.ViewLoader;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.util.Pair;

import java.io.IOException;

public class JoinServerButton extends HBox {
    @FXML
    private JFXButton joinServerButton;
    private Pair<String, String> inviteIds;
    private EventHandler<ActionEvent> handleButtonPressed;

    public JoinServerButton(Pair<String, String> inviteIds, EventHandler<ActionEvent> handleButtonPressed) {
        this.inviteIds = inviteIds;
        this.handleButtonPressed = handleButtonPressed;
        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.JOIN_SERVER_BUTTON);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        joinServerButton.setOnAction(this::onButtonPressed);
    }

    private void onButtonPressed(ActionEvent actionEvent) {
        ActionEvent ae = new ActionEvent(inviteIds, null);
        handleButtonPressed.handle(ae);
    }
}
