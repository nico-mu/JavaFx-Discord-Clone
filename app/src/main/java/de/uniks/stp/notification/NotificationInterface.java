package de.uniks.stp.notification;

public interface NotificationInterface {
    void onChannelNotificationEvent(NotificationEvent event);

    void onUserNotificationEvent(NotificationEvent event);
}
