package de.uniks.stp.notification;

public interface SubscriberInterface {
    void onChannelNotificationEvent(NotificationEvent event);

    void onUserNotificationEvent(NotificationEvent event);
}
