package de.uniks.stp.component;

import java.beans.PropertyChangeSupport;

public class NavBarNotificationElement extends NavBarElement {

    public static final String PROPERTY_NOTIFICATIONS = "notifications";

    protected PropertyChangeSupport listeners;

    private int notifications = 0;

    /**
     * increases the notification count and fires a property change for PROPERTY_NOTIFICATIONS
     */
    public void increaseNotifications() {
        ++notifications;
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
}
