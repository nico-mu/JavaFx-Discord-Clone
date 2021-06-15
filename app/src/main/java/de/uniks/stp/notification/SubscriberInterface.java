package de.uniks.stp.notification;

public interface SubscriberInterface {
    /**
     * Method triggered by the NotificationEvent in the onChannelMessage method.
     * The event contains the channel which got a notification as source and the notification count of the channel.
     * @param event NotificationEvent with source type Channel.
     */
    void onChannelNotificationEvent(NotificationEvent event);

    /**
     * Method triggered by the NotificationEvent in the onUserMessage method.
     * The event contains as source the user which got a notification and the notification count of the user.
     * @param event NotificationEvent with source type User.
     */
    void onUserNotificationEvent(NotificationEvent event);
}
