package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Server;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class NavBarServerElement extends NavBarNotificationElement {

    Server model;
    PropertyChangeListener serverNamePropertyChangeListener = this::onServerNamePropertyChange;

    public NavBarServerElement(Server model) {
        this.model = model;
        this.setId(model.getId() + "-button");
        Tooltip.install(navBarElement, new Tooltip(model.getName()));
        imageView.setImage(ViewLoader.loadImage("server.png"));
        notificationLabel.setVisible(false);
        circle.setVisible(false);

        model.listeners().addPropertyChangeListener(Server.PROPERTY_NAME, serverNamePropertyChangeListener);
    }

    public Server getModel() {
        return model;
    }

    @Override
    protected void onMouseClicked(MouseEvent mouseEvent) {
        super.onMouseClicked(mouseEvent);
        resetNotifications();
        RouteArgs args = new RouteArgs().addArgument(":id", model.getId());
        Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args);
    }

    private void onServerNamePropertyChange(PropertyChangeEvent propertyChangeEvent) {
        Tooltip.install(navBarElement, new Tooltip(model.getName()));
    }
}
