package de.uniks.stp.notification;

import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationService {

    private static final ConcurrentHashMap<NotificationEvent, List<NotificationInterface>> channelNotifications = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<NotificationEvent, List<NotificationInterface>> userNotifications = new ConcurrentHashMap<>();
    private static final List<NotificationInterface> userSubscriber = new CopyOnWriteArrayList<>();
    private static final List<NotificationInterface> channelSubscriber = new CopyOnWriteArrayList<>();
    private static Object activeObject;

    public static void register(User publisher) {
        if (Objects.isNull(publisher)) {
            return;
        }
        NotificationEvent event = getNotificationEvent(publisher);
        if (Objects.isNull(event)) {
            userNotifications.put(new NotificationEvent(publisher), userSubscriber);
        }
    }

    public static void register(Channel publisher) {
        if (Objects.isNull(publisher)) {
            return;
        }
        NotificationEvent event = getNotificationEvent(publisher);
        if (Objects.isNull(event)) {
            channelNotifications.put(new NotificationEvent(publisher), channelSubscriber);
        }
    }

    public static void registerUserSubscriber(NotificationInterface subscriber) {
        if (!userSubscriber.contains(subscriber)) {
            userSubscriber.add(subscriber);
        }
    }

    public static void registerChannelSubscriber(NotificationInterface subscriber) {
        if (!channelSubscriber.contains(subscriber)) {
            channelSubscriber.add(subscriber);
        }
    }

    public static void removeUserSubscriber(NotificationInterface subscriber) {
        userSubscriber.remove(subscriber);
    }

    public static void removeChannelSubscriber(NotificationInterface subscriber) {
        if (Objects.isNull(subscriber)) {
            return;
        }
        channelSubscriber.remove(subscriber);
    }

    public static void removePublisher(User publisher) {
        NotificationEvent event = handleNotificationEvent(publisher);
        if (Objects.isNull(event)) {
            return;
        }
        userNotifications.remove(event);
    }

    public static void removePublisher(Channel publisher) {
        NotificationEvent event = handleNotificationEvent(publisher);
        if (Objects.isNull(event)) {
            return;
        }
        channelNotifications.remove(event);
    }

    public static void onPrivateMessage(User publisher) {
        if (Objects.nonNull(activeObject) && activeObject.equals(publisher)) {
            return;
        }
        NotificationEvent event = handleNotificationEvent(publisher);
        if (Objects.isNull(event)) {
            return;
        }
        event.increaseNotificationsAndGet();
        notifyUser(event);
    }

    public static void onChannelMessage(Channel publisher) {
        if (Objects.nonNull(activeObject) && activeObject.equals(publisher)) {
            return;
        }
        NotificationEvent event = handleNotificationEvent(publisher);
        if (Objects.isNull(event)) {
            return;
        }
        event.increaseNotificationsAndGet();
        notifyChannel(event);
    }

    public static NotificationEvent consume(User publisher) {
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

    public static NotificationEvent consume(Channel publisher) {
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

    public static int getServerNotificationCount(Server server) {
        int notificationCount = 0;
        for (NotificationEvent event : channelNotifications.keySet()) {
            Channel channel = (Channel) event.getSource();
            if (channel.getCategory().getServer().equals(server)) {
                notificationCount += event.getNotifications();
            }
        }
        return notificationCount;
    }

    public static void setActiveObject(Object o) {
        activeObject = o;
    }

    private static NotificationEvent handleNotificationEvent(Object source) {
        if (Objects.isNull(source)) {
            return null;
        }
        NotificationEvent event = getNotificationEvent(source);
        if (Objects.isNull(event)) {
            throw new RuntimeException("NotificationEvent has not been registered.");
        }
        return event;
    }

    private static void notifyUser(NotificationEvent event) {
        for (NotificationInterface subscriber : userNotifications.get(event)) {
            subscriber.onUserNotificationEvent(event);
        }
    }

    private static void notifyChannel(NotificationEvent event) {
        for (NotificationInterface subscriber : channelNotifications.get(event)) {
            subscriber.onChannelNotificationEvent(event);
        }
    }

    private static NotificationEvent getNotificationEvent(Object source) {
        for (NotificationEvent event : userNotifications.keySet()) {
            if (event.getSource().equals(source)) {
                return event;
            }
        }
        for (NotificationEvent event : channelNotifications.keySet()) {
            if (event.getSource().equals(source)) {
                return event;
            }
        }
        return null;
    }

    private static NotificationEvent getOrCreateNotificationEvent(Object source) {
        NotificationEvent event = getNotificationEvent(source);
        if (Objects.isNull(event)) {
            return new NotificationEvent(source);
        }
        return event;
    }
}
