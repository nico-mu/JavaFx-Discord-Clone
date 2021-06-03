package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.event.ChannelChangeEvent;
import de.uniks.stp.model.Channel;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.IOException;

public class ServerChannelElement extends HBox implements NotificationComponentInterface {

    @FXML
    Text channelText;

    @FXML
    Pane channelElementMarker;

    @FXML
    VBox channelVBox;

    Channel model;

    private final Font font;
    private final Font boldFont;

    public ServerChannelElement(Channel model) {
        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.SERVER_CHANNEL_ELEMENT);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.model = model;
        channelText.setText(model.getName());
        channelVBox.setOnMouseClicked(this::onMouseClicked);

        font = Font.font(channelText.getFont().getFamily(), FontWeight.NORMAL, channelText.getFont().getSize());
        boldFont = Font.font(channelText.getFont().getFamily(), FontWeight.BOLD, channelText.getFont().getSize());

        setNotificationVisibility(false);
    }

    public void setActive(boolean active) {
        channelElementMarker.setVisible(active);
    }

    @Override
    public void setNotificationCount(int notifications) {
        setNotificationVisibility(0 < notifications);
    }

    @Override
    public void setNotificationVisibility(boolean mode) {
        Platform.runLater(() -> {
            if (mode) {
                channelText.setFont(boldFont);
                channelText.setStyle("-fx-text-fill: red");
            } else {
                channelText.setFont(font);
                channelText.setStyle("-fx-text-fill: white");
            }
        });
    }

    private void onMouseClicked(MouseEvent mouseEvent) {
        this.fireEvent(new ChannelChangeEvent(this));
        RouteArgs args = new RouteArgs();
        args.addArgument(":id", model.getCategory().getServer().getId());
        args.addArgument(":categoryId", model.getCategory().getId());
        args.addArgument(":channelId", model.getId());
        Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER + Constants.ROUTE_CHANNEL, args);
    }
}
