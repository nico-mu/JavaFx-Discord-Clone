package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.event.NavBarHomeElementActiveEvent;
import de.uniks.stp.model.Notification;
import de.uniks.stp.model.User;
import de.uniks.stp.model.UserNotification;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class NavBarUserElement extends NavBarNotificationElement {

    final User user;
    final NavBarList parent;
    PropertyChangeListener notificationPropertyChangeListener = this::onNotificationPropertyChange;

    public NavBarUserElement(UserNotification model, NavBarList parent) {
        super(model);
        this.parent = parent;
        user = model.getModel();
        this.setId(user.getId() + "-button");
        Tooltip.install(navBarElement, new Tooltip(user.getName()));
        imageView.setImage(ViewLoader.loadImage("user.png"));
        this.model.listeners().addPropertyChangeListener(Notification.PROPERTY_NOTIFICATION_COUNTER, notificationPropertyChangeListener);
    }

    public void stop() {
        model.listeners().removePropertyChangeListener(notificationPropertyChangeListener);
    }

    public User getUser() {
        return user;
    }

    @Override
    protected void onMouseClicked(MouseEvent mouseEvent) {
        super.onMouseClicked(mouseEvent);
        model.setNotificationCounter(0);
        this.setNotificationCount();
        RouteArgs args = new RouteArgs().addArgument(Constants.ROUTE_PRIVATE_CHAT_ARGS, user.getId());
        Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_PRIVATE_CHAT, args);
        this.fireEvent(new NavBarHomeElementActiveEvent());
    }

    private void onNotificationPropertyChange(PropertyChangeEvent propertyChangeEvent) {
        int notificationCount = (int) propertyChangeEvent.getNewValue();
        this.model.setNotificationCounter(notificationCount);
        if (notificationCount == 1) {
            Platform.runLater(() -> {
                parent.addUserElement(this);
            });
        }
        else if (notificationCount == 0) {
            Platform.runLater(() -> {
                parent.removeElement(this);
            });
        }
        setNotificationCount();
    }
}
