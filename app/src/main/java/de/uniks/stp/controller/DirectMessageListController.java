package de.uniks.stp.controller;

import de.uniks.stp.Editor;
import de.uniks.stp.component.DirectMessageEntry;
import de.uniks.stp.component.DirectMessageList;
import de.uniks.stp.model.User;
import de.uniks.stp.notification.NotificationEvent;
import de.uniks.stp.notification.NotificationService;
import de.uniks.stp.notification.SubscriberInterface;
import javafx.application.Platform;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DirectMessageListController implements ControllerInterface, SubscriberInterface {

    private final DirectMessageList directMessageList;
    private final Editor editor;
    private final HashMap<String, DirectMessageEntry> directMessageEntryHashMap;
    private final Map<String, Boolean> knownUsers = new ConcurrentHashMap<>();
    private final PropertyChangeListener chatPartnerChangeListener = this::onChatPartnerChanged;

    public DirectMessageListController(final DirectMessageList directMessageList, final Editor editor) {
        this.directMessageList = directMessageList;
        this.editor = editor;
        directMessageEntryHashMap = new HashMap<>();
    }

    @Override
    public void init() {
        User currentUser = editor.getOrCreateAccord().getCurrentUser();
        currentUser
            .listeners()
            .addPropertyChangeListener(User.PROPERTY_CHAT_PARTNER, chatPartnerChangeListener);
        NotificationService.registerUserSubscriber(this);
    }

    @Override
    public void stop() {
        editor.getOrCreateAccord()
            .getCurrentUser()
            .listeners()
            .removePropertyChangeListener(User.PROPERTY_CHAT_PARTNER, chatPartnerChangeListener);
        NotificationService.removeUserSubscriber(this);
    }

    @Override
    public void onChannelNotificationEvent(NotificationEvent event) {

    }

    @Override
    public void onUserNotificationEvent(NotificationEvent event) {
        User user = (User) event.getSource();
        if (Objects.nonNull(user) && Objects.nonNull(user.getId()) && directMessageEntryHashMap.containsKey(user.getId())) {
            directMessageEntryHashMap.get(user.getId()).setNotificationCount(event.getNotifications());
        }
    }

    public void addUserToSidebar(User otherUser) {
        if (Objects.isNull(otherUser)) {
            return;
        }

        String id = otherUser.getId();

        // Check if user is already in the list
        if (knownUsers.containsKey(id)) {
            return;
        }
        knownUsers.put(id, true);

        if (directMessageEntryHashMap.containsKey(id)) {
            return;
        }

        // Add to known users sidebar
        DirectMessageEntry userListEntry = new DirectMessageEntry(otherUser);
        directMessageEntryHashMap.put(id, userListEntry);
        Platform.runLater(() -> {
            directMessageList.addElement(userListEntry);
            userListEntry.setNotificationCount(NotificationService.getPublisherNotificationCount(otherUser));
        });
    }

    private void onChatPartnerChanged(PropertyChangeEvent propertyChangeEvent) {
        User user = (User) propertyChangeEvent.getNewValue();
        addUserToSidebar(user);
    }
}
