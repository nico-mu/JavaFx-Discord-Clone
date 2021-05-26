package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Notification;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.ServerNotification;
import de.uniks.stp.model.User;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class NavBarServerElement extends NavBarNotificationElement {

    PropertyChangeListener serverNamePropertyChangeListener = this::onServerNamePropertyChange;
    PropertyChangeListener notificationPropertyChangeListener = this::onNotificationPropertyChange;

    final ServerNotification notification;

    public NavBarServerElement(ServerNotification serverNotification) {
        this.notification = serverNotification;
        this.setId(notification.getSender().getId() + "-button");
        Tooltip.install(navBarElement, new Tooltip(notification.getSender().getName()));
        imageView.setImage(ViewLoader.loadImage("server.png"));
        notificationLabel.setVisible(false);
        circle.setVisible(false);

        notification.getSender().listeners().addPropertyChangeListener(Server.PROPERTY_NAME, serverNamePropertyChangeListener);
        notification.listeners().addPropertyChangeListener(Notification.PROPERTY_NOTIFICATION_COUNTER, notificationPropertyChangeListener);
    }

    public void stop() {
        notification.getSender().listeners().removePropertyChangeListener(serverNamePropertyChangeListener);
        notification.listeners().removePropertyChangeListener(notificationPropertyChangeListener);
    }

    @Override
    protected void onMouseClicked(MouseEvent mouseEvent) {
        super.onMouseClicked(mouseEvent);
        notification.setNotificationCounter(0);
        this.setNotificationCount(notification.getNotificationCounter());
        stop();
        RouteArgs args = new RouteArgs().addArgument(":id", notification.getSender().getId());
        Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args);
    }

    private void onServerNamePropertyChange(PropertyChangeEvent propertyChangeEvent) {
        Tooltip.install(navBarElement, new Tooltip(notification.getSender().getName()));
    }

    private void onNotificationPropertyChange(PropertyChangeEvent propertyChangeEvent) {
        int notificationCount = (int) propertyChangeEvent.getNewValue();
        notification.setNotificationCounter(notificationCount);
        setNotificationCount(notification.getNotificationCounter());
    }
}
