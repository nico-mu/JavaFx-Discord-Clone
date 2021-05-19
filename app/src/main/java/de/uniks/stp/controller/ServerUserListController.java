package de.uniks.stp.controller;

import de.uniks.stp.Editor;
import de.uniks.stp.component.ServerUserListEntry;
import de.uniks.stp.component.UserListEntry;
import de.uniks.stp.model.Accord;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
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
import java.util.HashSet;
import java.util.Objects;

public class ServerUserListController implements ControllerInterface {
    private final static String ONLINE_USER_LIST_ID = "#online-user-list";
    private final static String OFFLINE_USER_LIST_ID = "#offline-user-list";
    private final static String SERVER_USER_LIST_SCROLL_ID = "#server-user-list-scroll";

    private final PropertyChangeListener availableUsersPropertyChangeListener = this::onAvailableUsersPropertyChange;
    private final HashMap<String, ServerUserListEntry> serverUserListEntryHashMap = new HashMap<>();
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

        final Accord accord = editor.getOrCreateAccord();
        accord.listeners().addPropertyChangeListener(Accord.PROPERTY_OTHER_USERS, availableUsersPropertyChangeListener);

        final RestClient restClient = NetworkClientInjector.getRestClient();
        restClient.getServerInformation(model.getId(), this::handleServerInformationRequest);
    }

    private void handleServerInformationRequest(HttpResponse<JsonNode> response) {
        if (response.isSuccess()) {
            final JSONArray data = response.getBody().getObject().getJSONObject("data").getJSONArray("members");
            data.forEach(o -> {
                JSONObject jsonUser = (JSONObject) o;
                String userId = jsonUser.getString("id");
                String name = jsonUser.getString("name");
                boolean status = Boolean.parseBoolean(jsonUser.getString("online"));

                User user = editor.getOrCreateUserOfServer(model, userId, name, status);

                if (status) {
                    onlineUser(user);
                    return;
                }

                offlineUser(user);
            });
        }
    }

    private void offlineUser(User user) {
        Platform.runLater(() -> {
            ServerUserListEntry serverUserListEntry = new ServerUserListEntry(user);
            serverUserListEntryHashMap.put(user.getId(), serverUserListEntry);
            offlineUserList.getChildren().add(serverUserListEntry);
        });
    }

    private void onlineUser(User user) {
        Platform.runLater(() -> {
            ServerUserListEntry serverUserListEntry = new ServerUserListEntry(user);
            serverUserListEntryHashMap.put(user.getId(), serverUserListEntry);
            onlineUserList.getChildren().add(serverUserListEntry);
        });
    }

    private void onAvailableUsersPropertyChange(PropertyChangeEvent propertyChangeEvent) {
        User oldValue = (User) propertyChangeEvent.getOldValue();
        User newValue = (User) propertyChangeEvent.getNewValue();

        if (Objects.isNull(oldValue) && Objects.nonNull(newValue) && serverUserListEntryHashMap.containsKey(newValue.getId())) {
            // User Joined
            ServerUserListEntry serverUserListEntry = serverUserListEntryHashMap.get(newValue.getId());
            offlineUserList.getChildren().remove(serverUserListEntry);
            onlineUserList.getChildren().add(serverUserListEntry);
        } else if (Objects.isNull(newValue) && Objects.nonNull(oldValue) && serverUserListEntryHashMap.containsKey(oldValue.getId())) {
            // User left
            ServerUserListEntry serverUserListEntry = serverUserListEntryHashMap.get(oldValue.getId());
            onlineUserList.getChildren().remove(serverUserListEntry);
            offlineUserList.getChildren().add(serverUserListEntry);
        }
    }

    public void stop() {
        Accord accord = editor.getOrCreateAccord();
        accord.listeners().removePropertyChangeListener(Accord.PROPERTY_OTHER_USERS, availableUsersPropertyChangeListener);
        serverUserListEntryHashMap.clear();
    }
}
