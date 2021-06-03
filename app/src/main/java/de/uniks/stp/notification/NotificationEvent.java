package de.uniks.stp.notification;

public class NotificationEvent {
    private int notifications = 0;
    private final Object source;

    public NotificationEvent(Object source) {
        this.source = source;
    }

    public int increaseNotificationsAndGet() {
        return ++notifications;
    }

    public void setNotifications(int notifications) {
        this.notifications = notifications;
    }

    public int getNotifications() {
        return notifications;
    }

    public void reset() {
        notifications = 0;
    }

    public Object getSource() {
        return this.source;
    }
}
