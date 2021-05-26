package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.event.NavBarHomeElementActiveEvent;
import de.uniks.stp.model.Notification;
import de.uniks.stp.model.User;
import de.uniks.stp.model.UserNotification;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class NavBarUserElement extends NavBarNotificationElement {

    final UserNotification userNotification;
    PropertyChangeListener notificationPropertyChangeListener = this::onNotificationPropertyChange;

    public NavBarUserElement(UserNotification model) {
        userNotification = model;
        this.setId(userNotification.getSender().getId() + "-button");
        Tooltip.install(navBarElement, new Tooltip(userNotification.getSender().getName()));
        imageView.setImage(ViewLoader.loadImage("user.png"));
        userNotification.listeners().addPropertyChangeListener(Notification.PROPERTY_NOTIFICATION_COUNTER, notificationPropertyChangeListener);
    }

    public void stop() {
        userNotification.listeners().removePropertyChangeListener(notificationPropertyChangeListener);
    }

    @Override
    protected void onMouseClicked(MouseEvent mouseEvent) {
        super.onMouseClicked(mouseEvent);
        userNotification.setNotificationCounter(0);
        this.setNotificationCount(userNotification.getNotificationCounter());
        stop();
        RouteArgs args = new RouteArgs().addArgument(Constants.ROUTE_PRIVATE_CHAT_ARGS, userNotification.getSender().getId());
        Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_PRIVATE_CHAT, args);
        this.fireEvent(new NavBarHomeElementActiveEvent());
    }

    private void onNotificationPropertyChange(PropertyChangeEvent propertyChangeEvent) {
        int notificationCount = (int) propertyChangeEvent.getNewValue();
        userNotification.setNotificationCounter(notificationCount);
        if (notificationCount == 1) {
            userNotification.getReceiver().withReceivedUserNotifications(userNotification);
        }
        else if (notificationCount == 0) {
            userNotification.getReceiver().withoutReceivedUserNotifications(userNotification);
        }
        setNotificationCount(notificationCount);
    }
}
