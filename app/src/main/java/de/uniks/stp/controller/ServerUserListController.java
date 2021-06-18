package de.uniks.stp.controller;

import de.uniks.stp.Editor;
import de.uniks.stp.component.ServerUserListEntry;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

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
    private VBox onlineUserList;
    private VBox offlineUserList;

    public ServerUserListController(Parent view, Editor editor, Server model) {
        this.view = view;
        this.editor = editor;
        this.model = model;
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

        //RestClient restClient = NetworkClientInjector.getRestClient();
        //restClient.getServerInformation(model.getId(), this::handleServerInformationRequest);
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

    private void handleServerInformationRequest(HttpResponse<JsonNode> response) {
        if (response.isSuccess()) {
            final JSONObject data = response.getBody().getObject().getJSONObject("data");
            final JSONArray member = data.getJSONArray("members");
            final String serverId = data.getString("id");
            final String serverName = data.getString("name");
            final String serverOwner = data.getString("owner");

            // add server to model -> to NavBar List
            if (serverOwner.equals(editor.getOrCreateAccord().getCurrentUser().getId())) {
                editor.getOrCreateServer(serverId, serverName).setOwner(editor.getOrCreateAccord().getCurrentUser());
            } else {
                editor.getOrCreateServer(serverId, serverName);
            }

            member.forEach(o -> {
                JSONObject jsonUser = (JSONObject) o;
                String userId = jsonUser.getString("id");
                String name = jsonUser.getString("name");
                boolean status = Boolean.parseBoolean(jsonUser.getString("online"));

                User serverMember = editor.getOrCreateServerMember(userId, name, model);
                serverMember.setStatus(status);
                addUser(serverMember);
                addStatusPropertyChangeListener(serverMember);
            });
        }
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
        ServerUserListEntry serverUserListEntry = new ServerUserListEntry(user);
        serverUserListEntryHashMap.put(user, serverUserListEntry);
        Platform.runLater(() -> offlineUserList.getChildren().add(serverUserListEntry));
    }

    private void onlineUser(User user) {
        removeUser(user);
        ServerUserListEntry serverUserListEntry = new ServerUserListEntry(user);
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
}
