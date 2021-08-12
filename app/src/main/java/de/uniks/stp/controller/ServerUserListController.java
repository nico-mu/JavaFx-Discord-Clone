package de.uniks.stp.controller;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.component.PrivateChatNavUserListEntry;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Objects;

public class ServerUserListController implements ControllerInterface {
    private final static String ONLINE_USER_LIST_ID = "#online-user-list";
    private final static String OFFLINE_USER_LIST_ID = "#offline-user-list";
    private final static String SERVER_USER_LIST_SCROLL_ID = "#server-user-list-scroll";

    private final PropertyChangeListener availableUsersPropertyChangeListener = this::onAvailableUsersPropertyChange;
    private final PropertyChangeListener userStatusPropertyChangeListener = this::onUserStatusPropertyChange;
    private final PropertyChangeListener userDescriptionPropertyChangeListener = this::onUserDescriptionPropertyChange;

    private final HashMap<User, PrivateChatNavUserListEntry> serverUserListEntryHashMap = new HashMap<>();
    private final Parent view;
    private final PrivateChatNavUserListEntry.PrivateChatNavUserListEntryFactory privateChatNavUserListEntryFactory;
    private final Server model;
    private VBox onlineUserList;
    private VBox offlineUserList;

    @AssistedInject
    public ServerUserListController(PrivateChatNavUserListEntry.PrivateChatNavUserListEntryFactory privateChatNavUserListEntryFactory,
                                    @Assisted Parent view,
                                    @Assisted Server model) {
        this.view = view;
        this.model = model;
        this.privateChatNavUserListEntryFactory = privateChatNavUserListEntryFactory;
    }

    public void init() {
        ScrollPane scrollPane = (ScrollPane) view.lookup(SERVER_USER_LIST_SCROLL_ID);
        onlineUserList = (VBox) scrollPane.getContent().lookup(ONLINE_USER_LIST_ID);
        offlineUserList = (VBox) scrollPane.getContent().lookup(OFFLINE_USER_LIST_ID);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        model.listeners().addPropertyChangeListener(Server.PROPERTY_USERS, availableUsersPropertyChangeListener);

        for(User user : model.getUsers()) {
            addUser(user);
            addUserPropertyChangeListeners(user);
        }
    }

    private void addUser(User user) {
        if (user.isStatus()) {
            onlineUser(user);
        } else {
            offlineUser(user);
            if(serverUserListEntryHashMap.containsKey(user)) {
                Platform.runLater(() -> serverUserListEntryHashMap.get(user).setDescription(""));
            }
        }
    }

    private void addUserPropertyChangeListeners(User user) {
        if (user.listeners().getPropertyChangeListeners(User.PROPERTY_STATUS).length == 0) {
            user.listeners().addPropertyChangeListener(User.PROPERTY_STATUS, userStatusPropertyChangeListener);
        }
        user.listeners().addPropertyChangeListener(User.PROPERTY_DESCRIPTION, userDescriptionPropertyChangeListener);
    }

    private void onAvailableUsersPropertyChange(PropertyChangeEvent propertyChangeEvent) {
        User user = (User) propertyChangeEvent.getNewValue();
        User oldUser = (User) propertyChangeEvent.getOldValue();

        if (Objects.isNull(user)) {
            // in case the server was deleted
            removeUser(oldUser);
            oldUser.listeners().removePropertyChangeListener(User.PROPERTY_STATUS, userStatusPropertyChangeListener);
            return;
        }
        addUser(user);
        addUserPropertyChangeListeners(user);
    }

    private void onUserStatusPropertyChange(PropertyChangeEvent propertyChangeEvent) {
        User user = (User) propertyChangeEvent.getSource();
        addUser(user);
    }

    private void onUserDescriptionPropertyChange(PropertyChangeEvent propertyChangeEvent) {
        User user = (User) propertyChangeEvent.getSource();
        String newDescription = (String) propertyChangeEvent.getNewValue();
        if(serverUserListEntryHashMap.containsKey(user)) {
            Platform.runLater(() -> serverUserListEntryHashMap.get(user).setDescription(newDescription));
        }
    }

    private void removeUser(User user) {
        if (serverUserListEntryHashMap.containsKey(user)) {
            PrivateChatNavUserListEntry privateChatNavUserListEntry = serverUserListEntryHashMap.get(user);
            Platform.runLater(() -> {
                offlineUserList.getChildren().remove(privateChatNavUserListEntry);
                onlineUserList.getChildren().remove(privateChatNavUserListEntry);
            });
        }
    }

    private void offlineUser(User user) {
        removeUser(user);
        PrivateChatNavUserListEntry privateChatNavUserListEntry = privateChatNavUserListEntryFactory.create(user);
        serverUserListEntryHashMap.put(user, privateChatNavUserListEntry);
        Platform.runLater(() -> offlineUserList.getChildren().add(privateChatNavUserListEntry));
    }

    private void onlineUser(User user) {
        removeUser(user);
        PrivateChatNavUserListEntry privateChatNavUserListEntry = privateChatNavUserListEntryFactory.create(user);
        serverUserListEntryHashMap.put(user, privateChatNavUserListEntry);
        Platform.runLater(() -> onlineUserList.getChildren().add(privateChatNavUserListEntry));
    }

    public void stop() {
        model.listeners().removePropertyChangeListener(Server.PROPERTY_USERS, availableUsersPropertyChangeListener);
        for (User user : model.getUsers()) {
            user.listeners().removePropertyChangeListener(User.PROPERTY_STATUS, userStatusPropertyChangeListener);
            user.listeners().removePropertyChangeListener(User.PROPERTY_DESCRIPTION, userDescriptionPropertyChangeListener);
        }
        serverUserListEntryHashMap.clear();
        onlineUserList.getChildren().clear();
        offlineUserList.getChildren().clear();
    }

    @AssistedFactory
    public interface ServerUserListControllerFactory {
        ServerUserListController create(Parent view, Server server);
    }
}
