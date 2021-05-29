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
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;

import java.io.IOException;

public class ServerChannelElement extends HBox implements Notifications {

    @FXML
    Label channelLabel;

    @FXML
    Pane channelElementMarker;

    @FXML
    Circle circle;

    @FXML
    Label notificationLabel;

    Channel model;

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
        channelLabel.setText(model.getName());
        channelLabel.setOnMouseClicked(this::onMouseClicked);

        setNotificationVisibility(false);
    }

    public void setActive(boolean active) {
        channelElementMarker.setVisible(active);
    }

    @Override
    public void setNotificationCount(int notifications) {
        if (0 < notifications && notifications < 10) {
            setNotificationsLabel(Integer.toString(notifications));
            setNotificationVisibility(true);
        } else if (10 <= notifications) {
            setNotificationsLabel("9+");
            setNotificationVisibility(true);
        } else {
            setNotificationsLabel("0");
            setNotificationVisibility(false);
        }
    }

    @Override
    public void setNotificationVisibility(boolean mode) {
        notificationLabel.setVisible(mode);
        circle.setVisible(mode);
    }

    private void onMouseClicked(MouseEvent mouseEvent) {
        this.fireEvent(new ChannelChangeEvent(this));
        setNotificationCount(0);
        RouteArgs args = new RouteArgs();
        args.addArgument(":id", model.getCategory().getServer().getId());
        args.addArgument(":categoryId", model.getCategory().getId());
        args.addArgument(":channelId", model.getId());
        Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER + Constants.ROUTE_CHANNEL, args);
    }


    private void setNotificationsLabel(String value) {
        Platform.runLater(() -> {
            notificationLabel.setText(value);
        });
    }
}
