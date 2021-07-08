package de.uniks.stp.notification;

import de.uniks.stp.AudioService;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.router.Router;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationService {

    private final ConcurrentHashMap<NotificationEvent, List<SubscriberInterface>> channelNotifications;
    private final ConcurrentHashMap<NotificationEvent, List<SubscriberInterface>> userNotifications;
    private final List<SubscriberInterface> userSubscriber;
    private final List<SubscriberInterface> channelSubscriber;
    private final Router router;
    private final SessionDatabaseService databaseService;
    private final AudioService audioService;

    @Inject
    public NotificationService(Router router,
                               SessionDatabaseService databaseService,
                               AudioService audioService) {
        this.router = router;
        this.databaseService = databaseService;
        this.audioService = audioService;
        channelNotifications = new ConcurrentHashMap<>();
        userNotifications = new ConcurrentHashMap<>();
        userSubscriber = new CopyOnWriteArrayList<>();
        channelSubscriber = new CopyOnWriteArrayList<>();
    }

    /**
     * Add a user that publishes messages.
     * @param publisher user that will publish messages.
     */
    public void register(User publisher) {
        if (Objects.isNull(publisher)) {
            return;
        }
        NotificationEvent event = getNotificationEvent(publisher);
        if (Objects.isNull(event)) {
            userNotifications.put(new NotificationEvent(publisher), userSubscriber);
        }
    }

    /**
     * Add a channel that publishes messages.
     * @param publisher channel that will publish messages.
     */
    public void register(Channel publisher) {
        if (Objects.isNull(publisher)) {
            return;
        }
        NotificationEvent event = getNotificationEvent(publisher);
        if (Objects.isNull(event)) {
            channelNotifications.put(new NotificationEvent(publisher), channelSubscriber);
        }
    }

    /**
     * Add a subscriber that wants to receive notifications from registered users.
     * @param subscriber Object that implements SubscriberInterface
     */
    public void registerUserSubscriber(SubscriberInterface subscriber) {
        if (!userSubscriber.contains(subscriber)) {
            userSubscriber.add(subscriber);
        }
    }

    /**
     * Add a subscriber that wants to receive notifications from registered channels.
     * @param subscriber Object that implements SubscriberInterface
     */
    public void registerChannelSubscriber(SubscriberInterface subscriber) {
        if (!channelSubscriber.contains(subscriber)) {
            channelSubscriber.add(subscriber);
        }
    }

    /**
     * Remove a subscriber that wants to receive notifications from registered users.
     * @param subscriber Object that implements SubscriberInterface
     */
    public void removeUserSubscriber(SubscriberInterface subscriber) {
        userSubscriber.remove(subscriber);
    }

    /**
     * Remove a subscriber that wants to receive notifications from registered channel.
     * @param subscriber Object that implements SubscriberInterface
     */
    public void removeChannelSubscriber(SubscriberInterface subscriber) {
        if (Objects.isNull(subscriber)) {
            return;
        }
        channelSubscriber.remove(subscriber);
    }

    /**
     * Unregister a user.
     * @param publisher user
     */
    public void removePublisher(User publisher) {
        NotificationEvent event = handleNotificationEvent(publisher);
        if (Objects.isNull(event)) {
            return;
        }
        userNotifications.remove(event);
    }

    /**
     * Unregister a channel.
     * @param publisher channel
     */
    public void removePublisher(Channel publisher) {
        NotificationEvent event = handleNotificationEvent(publisher);
        if (Objects.isNull(event)) {
            return;
        }
        channelNotifications.remove(event);
    }

    /**
     * Triggers a notification for all Objects subscribed to user notifications.
     * This will cause every subscriber to enter the onUserNotificationEvent method.
     * @param publisher user that sent a private message
     */
    public void onPrivateMessage(User publisher) {
        HashMap<String, String> routeArgs = router.getCurrentArgs();
        if (routeArgs.containsKey(":userId") && routeArgs.get(":userId").equals(publisher.getId())) {
            return;
        }
        NotificationEvent event = handleNotificationEvent(publisher);
        if (Objects.isNull(event)) {
            return;
        }
        event.increaseNotificationsAndGet();
        notifyUser(event);
        audioService.playNotificationSound();
    }

    /**
     * Triggers a notification for all Objects subscribed to channel notifications.
     * This will cause every subscriber to enter the onChannelNotificationEvent method.
     * @param publisher channel that sent a channel message
     */
    public void onChannelMessage(Channel publisher) {
        if(databaseService.isChannelMuted(publisher.getId()) ||
            databaseService.isServerMuted(publisher.getServer().getId()) ||
            databaseService.isCategoryMuted(publisher.getCategory().getId())) {
            return;
        }
        HashMap<String, String> routeArgs = router.getCurrentArgs();
        if (routeArgs.containsKey(":channelId") && routeArgs.get(":channelId").equals(publisher.getId())) {
            return;
        }
        NotificationEvent event = handleNotificationEvent(publisher);
        if (Objects.isNull(event)) {
            return;
        }
        event.increaseNotificationsAndGet();
        notifyChannel(event);
        audioService.playNotificationSound();
    }

    /**
     * Resets the notification count of the publisher to zero.
     * Afterwards it triggers a notification for all Objects subscribed to user notifications.
     * This will cause every subscriber to enter the onUserNotificationEvent method.
     * @param publisher user which notifications need to be reset
     * @return NotificationEvent with zero notifications with source type user
     */
    public NotificationEvent consume(User publisher) {
        NotificationEvent event = handleNotificationEvent(publisher);
        if (Objects.isNull(event)) {
            return null;
        }
        NotificationEvent eventCopy = new NotificationEvent(publisher);
        eventCopy.setNotifications(event.getNotifications());
        event.reset();
        notifyUser(event);
        return eventCopy;
    }

    /**
     * Resets the notification count of the publisher to zero.
     * Afterwards it triggers a notification for all Objects subscribed to channel notifications.
     * This will cause every subscriber to enter the onChannelNotificationEvent method.
     * @param publisher channel which notifications need to be reset
     * @return NotificationEvent with zero notifications with source type channel
     */
    public NotificationEvent consume(Channel publisher) {
        NotificationEvent event = handleNotificationEvent(publisher);
        if (Objects.isNull(event)) {
            return null;
        }
        NotificationEvent eventCopy = new NotificationEvent(publisher);
        eventCopy.setNotifications(event.getNotifications());
        event.reset();
        notifyChannel(event);
        return eventCopy;
    }

    /**
     * Get the notification count of a given server.
     * @param server server
     * @return number of notifications
     */
    public int getServerNotificationCount(Server server) {
        int notificationCount = 0;
        for (NotificationEvent event : channelNotifications.keySet()) {
            Channel channel = (Channel) event.getSource();
            Category category = channel.getCategory();
            Server channelServer = channel.getServer();
            if ((Objects.nonNull(channelServer) && channelServer.equals(server)) || (Objects.nonNull(category) && category.getServer().equals(server))) {
                notificationCount += event.getNotifications();
            }
        }
        return notificationCount;
    }

    /**
     * Get the notification count of a given channel
     * @param channel channel
     * @return number of notifications
     */
    public int getPublisherNotificationCount(Channel channel) {
        NotificationEvent event = getNotificationEvent(channel);
        if (Objects.nonNull(event)) {
            return event.getNotifications();
        }
        return 0;
    }

    /**
     * Get the notification count of a given user
     * @param user user
     * @return number of notifications
     */
    public int getPublisherNotificationCount(User user) {
        NotificationEvent event = getNotificationEvent(user);
        if (Objects.nonNull(event)) {
            return event.getNotifications();
        }
        return 0;
    }

    /**
     * Invokes user notifications of all subscribers
     */
    public void invokeUserNotifications() {
        userNotifications.forEach((key, value) -> {
            notifyUser(key);
        });
    }

    /**
     * resets the NotificationService and clears all its data.
     */
    public void reset() {
        channelSubscriber.clear();
        userSubscriber.clear();
        channelNotifications.clear();
        userNotifications.clear();
    }

    private NotificationEvent handleNotificationEvent(Object source) {
        if (Objects.isNull(source)) {
            return null;
        }
        NotificationEvent event = getNotificationEvent(source);
        if (Objects.isNull(event)) {
            return null;
        }
        return event;
    }

    private void notifyUser(NotificationEvent event) {
        for (SubscriberInterface subscriber : userNotifications.get(event)) {
            subscriber.onUserNotificationEvent(event);
        }
    }

    private void notifyChannel(NotificationEvent event) {
        for (SubscriberInterface subscriber : channelNotifications.get(event)) {
            subscriber.onChannelNotificationEvent(event);
        }
    }

    private NotificationEvent getNotificationEvent(Object source) {
        if (source instanceof User) {
            User user = (User) source;
            for (NotificationEvent event : userNotifications.keySet()) {
                if (user.getId().equals(((User) event.getSource()).getId())) {
                    return event;
                }
            }
        }
        if (source instanceof Channel) {
            Channel channel = (Channel) source;
            for (NotificationEvent event : channelNotifications.keySet()) {
                if (channel.getId().equals(((Channel) event.getSource()).getId())) {
                    return event;
                }
            }
        }
        return null;
    }
}
