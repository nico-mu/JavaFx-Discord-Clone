package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Notification;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.ServerNotification;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class NavBarServerElement extends NavBarNotificationElement {

    PropertyChangeListener serverNamePropertyChangeListener = this::onServerNamePropertyChange;
    PropertyChangeListener notificationPropertyChangeListener = this::onNotificationPropertyChange;

    final Server server;

    public NavBarServerElement(ServerNotification model) {
        super(model);
        server = model.getModel();
        this.setId(server.getId() + "-button");
        Tooltip.install(navBarElement, new Tooltip(server.getName()));
        imageView.setImage(ViewLoader.loadImage("server.png"));
        notificationLabel.setVisible(false);
        circle.setVisible(false);

        server.listeners().addPropertyChangeListener(Server.PROPERTY_NAME, serverNamePropertyChangeListener);
        this.model.listeners().addPropertyChangeListener(Notification.PROPERTY_NOTIFICATION_COUNTER, notificationPropertyChangeListener);
    }

    public void stop() {
        server.listeners().removePropertyChangeListener(serverNamePropertyChangeListener);
        model.listeners().removePropertyChangeListener(notificationPropertyChangeListener);
    }

    public Server getServer() {
        return server;
    }

    @Override
    protected void onMouseClicked(MouseEvent mouseEvent) {
        super.onMouseClicked(mouseEvent);
        model.setNotificationCounter(0);
        this.setNotificationCount();
        RouteArgs args = new RouteArgs().addArgument(":id", server.getId());
        Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args);
    }

    private void onServerNamePropertyChange(PropertyChangeEvent propertyChangeEvent) {
        Tooltip.install(navBarElement, new Tooltip(server.getName()));
    }

    private void onNotificationPropertyChange(PropertyChangeEvent propertyChangeEvent) {
        int notificationCount = (int) propertyChangeEvent.getNewValue();
        this.model.setNotificationCounter(notificationCount);
        setNotificationCount();
    }
}
