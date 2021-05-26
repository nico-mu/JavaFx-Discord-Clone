package de.uniks.stp.component;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.beans.PropertyChangeSupport;

public class NavBarNotificationElement extends NavBarElement {

    public static final String PROPERTY_NOTIFICATIONS = "notifications";

    protected PropertyChangeSupport listeners;

    private int notifications = 0;

    /**
     * sets the notification label to given number and handles the visibility of the label and circle
     * @param count number of notifications
     */
    public void setNotificationCount(int count) {
        if (0 < count && count < 10)
        {
            setNotificationsLabel(Integer.toString(count));
            setNotificationVisibility(true);
        }
        else if (10 <= count)
        {
            setNotificationsLabel("9+");
            setNotificationVisibility(true);
        }
        else
        {
            setNotificationsLabel("0");
            setNotificationVisibility(false);
        }
    }

    /**
     * increases the notification count and fires a property change for PROPERTY_NOTIFICATIONS
     */
    public void increaseNotifications() {
        ++notifications;
        setNotificationCount(notifications);
        this.firePropertyChange(PROPERTY_NOTIFICATIONS, null, this);
    }

    public int getNotifications() {
        return notifications;
    }

    /**
     * sets the notification count to zero and fires a property change for PROPERTY_NOTIFICATIONS
     */
    public void resetNotifications() {
        notifications = 0;
        setNotificationCount(notifications);
        this.firePropertyChange(PROPERTY_NOTIFICATIONS, null, this);
    }

    public boolean firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (this.listeners != null) {
            this.listeners.firePropertyChange(propertyName, oldValue, newValue);
            return true;
        }
        return false;
    }

    public PropertyChangeSupport listeners() {
        if (this.listeners == null) {
            this.listeners = new PropertyChangeSupport(this);
        }
        return this.listeners;
    }

    private void setNotificationVisibility(boolean mode) {
        notificationLabel.setVisible(mode);
        circle.setVisible(mode);
    }

    private void setNotificationsLabel(String value) {
        Platform.runLater(() -> {
            notificationLabel.setText(value);
        });
    }
}
