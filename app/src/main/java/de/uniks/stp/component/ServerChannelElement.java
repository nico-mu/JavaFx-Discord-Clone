package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Channel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class ServerChannelElement extends HBox {

    @FXML
    Label channelLabel;

    public ServerChannelElement(Channel model) {
        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.SERVER_CHANNEL_ELEMENT);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        channelLabel.setText(model.getName());
    }
}
