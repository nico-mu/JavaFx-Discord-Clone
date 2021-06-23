package de.uniks.stp.controller;

import de.uniks.stp.Editor;
import de.uniks.stp.component.DirectMessageEntry;
import de.uniks.stp.component.ListComponent;
import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.model.User;
import de.uniks.stp.notification.NotificationEvent;
import de.uniks.stp.notification.NotificationService;
import de.uniks.stp.notification.SubscriberInterface;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

public class DirectMessageListController implements ControllerInterface, SubscriberInterface {

    private final Editor editor;
    private final PropertyChangeListener chatPartnerChangeListener = this::onChatPartnerChanged;
    private final VBox directMessagesContainer;
    private final ListComponent<User, DirectMessageEntry> directMessagePartnerList;

    public DirectMessageListController(Parent view, final Editor editor) {
        this.editor = editor;
        directMessagesContainer = (VBox) view;
        directMessagePartnerList = new ListComponent<>();
        directMessagesContainer.getChildren().add(directMessagePartnerList);
    }

    @Override
    public void init() {
        User currentUser = editor.getOrCreateAccord().getCurrentUser();
        currentUser
            .listeners()
            .addPropertyChangeListener(User.PROPERTY_CHAT_PARTNER, chatPartnerChangeListener);
        NotificationService.registerUserSubscriber(this);

        for (Pair<String, String> chatPartner : DatabaseService.getAllConversationPartnerOf(currentUser.getName())) {
            String chatPartnerId = chatPartner.getKey();
            String chatPartnerName = chatPartner.getValue();
            User user = editor.getChatPartnerOfCurrentUserById(chatPartnerId);
            if(Objects.nonNull(user)) {
                addUserToSidebar(user);
            }
            else {
                editor.getOrCreateChatPartnerOfCurrentUser(chatPartnerId, chatPartnerName);
            }
        }
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

        if (Objects.nonNull(user) && Objects.nonNull(user.getId())) {
            DirectMessageEntry entry = directMessagePartnerList.getElement(user);
            if(Objects.nonNull(entry)) {
                entry.setNotificationCount(event.getNotifications());
            }
        }
    }

    public void addUserToSidebar(User otherUser) {
        if (Objects.isNull(otherUser)) {
            return;
        }

        if(!directMessagePartnerList.contains(otherUser)) {
            DirectMessageEntry directMessagePartnerEntry = new DirectMessageEntry(otherUser);

            Platform.runLater(() -> {
                directMessagePartnerList.addElement(otherUser, directMessagePartnerEntry);
                directMessagePartnerEntry.setNotificationCount(NotificationService.getPublisherNotificationCount(otherUser));
            });
        }
    }

    private void onChatPartnerChanged(PropertyChangeEvent propertyChangeEvent) {
        User user = (User) propertyChangeEvent.getNewValue();
        addUserToSidebar(user);
    }
}
