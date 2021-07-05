package de.uniks.stp.controller;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.component.ServerUserListEntry;
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

    private final HashMap<User, ServerUserListEntry> serverUserListEntryHashMap = new HashMap<>();
    private final Parent view;
    private final Editor editor;
    private final Server model;
    private final ViewLoader viewLoader;
    private final ServerUserListEntry.ServerUserListEntryFactory serverUserListEntryFactory;
    private VBox onlineUserList;
    private VBox offlineUserList;

    @AssistedInject
    public ServerUserListController(Editor editor,
                                    ViewLoader viewLoader,
                                    ServerUserListEntry.ServerUserListEntryFactory serverUserListEntryFactory,
                                    @Assisted Parent view,
                                    @Assisted Server model) {
        this.view = view;
        this.viewLoader = viewLoader;
        this.editor = editor;
        this.model = model;
        this.serverUserListEntryFactory = serverUserListEntryFactory;
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
            addStatusPropertyChangeListener(user);
        }
    }

    private void addUser(User user) {
        if (user.isStatus()) {
            onlineUser(user);
        } else {
            offlineUser(user);
        }
    }

    private void addStatusPropertyChangeListener(User user) {
        if (user.listeners().getPropertyChangeListeners(User.PROPERTY_STATUS).length == 0) {
            user.listeners().addPropertyChangeListener(User.PROPERTY_STATUS, userStatusPropertyChangeListener);
        }
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
        addStatusPropertyChangeListener(user);
    }

    private void onUserStatusPropertyChange(PropertyChangeEvent propertyChangeEvent) {
        User user = (User) propertyChangeEvent.getSource();
        addUser(user);
    }

    private void removeUser(User user) {
        if (serverUserListEntryHashMap.containsKey(user)) {
            ServerUserListEntry serverUserListEntry = serverUserListEntryHashMap.get(user);
            Platform.runLater(() -> {
                offlineUserList.getChildren().remove(serverUserListEntry);
                onlineUserList.getChildren().remove(serverUserListEntry);
            });
        }
    }

    private void offlineUser(User user) {
        removeUser(user);
        ServerUserListEntry serverUserListEntry = serverUserListEntryFactory.create(user);
        serverUserListEntryHashMap.put(user, serverUserListEntry);
        Platform.runLater(() -> offlineUserList.getChildren().add(serverUserListEntry));
    }

    private void onlineUser(User user) {
        removeUser(user);
        ServerUserListEntry serverUserListEntry = serverUserListEntryFactory.create(user);
        serverUserListEntryHashMap.put(user, serverUserListEntry);
        Platform.runLater(() -> onlineUserList.getChildren().add(serverUserListEntry));
    }

    public void stop() {
        model.listeners().removePropertyChangeListener(Server.PROPERTY_USERS, availableUsersPropertyChangeListener);
        for (User user : model.getUsers()) {
            user.listeners().removePropertyChangeListener(userStatusPropertyChangeListener);
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
