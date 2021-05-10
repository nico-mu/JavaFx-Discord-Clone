package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.component.UserList;
import de.uniks.stp.component.UserListEntry;
import de.uniks.stp.model.Accord;
import de.uniks.stp.model.User;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.router.Route;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import javafx.application.Platform;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Consumer;


public class UserListController implements ControllerInterface {
    private final HashMap<User, UserListEntry> userUserListEntryHashMap = new HashMap<>();
    private final UserList userList;
    private final Editor editor;


    private final PropertyChangeListener availableUsersPropertyChangeListener = this::onAvailableUsersPropertyChange;
    private RestClient restClient;

    public UserListController(final UserList userList, final Editor editor) {
        this.userList = userList;
        this.editor = editor;
    }

    private void onAvailableUsersPropertyChange(final PropertyChangeEvent propertyChangeEvent) {
        final User oldValue = (User) propertyChangeEvent.getOldValue();
        final User newValue = (User) propertyChangeEvent.getNewValue();

        if (Objects.isNull(oldValue)) {
            // user joined
            final UserListEntry userListEntry = new UserListEntry(newValue);
            userUserListEntryHashMap.put(newValue, userListEntry);
            Platform.runLater(() -> userList.addUserListEntry(userListEntry));
        } else if (Objects.isNull(newValue)) {
            // user left
            final UserListEntry userListEntry = userUserListEntryHashMap.remove(oldValue);
            Platform.runLater(() -> userList.removeUserListEntry(userListEntry));
        }
    }

    @Override
    public void init() {
        editor.getOrCreateAccord().listeners().addPropertyChangeListener(Accord.PROPERTY_OTHER_USERS, availableUsersPropertyChangeListener);

        // Add users in current model
        for (User user : editor.getOtherUsers()) {
            final UserListEntry userListEntry = new UserListEntry(user);
            userUserListEntryHashMap.put(user, userListEntry);
            Platform.runLater(() -> userList.addUserListEntry(userListEntry));
        }

        restClient = new RestClient();
        restClient.requestOnlineUsers(this::handleUserOnlineRequest);
    }

    @Override
    public void route(RouteInfo routeInfo, RouteArgs args) {
        // no subroutes
    }

    private void handleUserOnlineRequest(final HttpResponse<JsonNode> response) {
        if (response.isSuccess()) {
            final JSONArray data = response.getBody().getObject().getJSONArray("data");
            data.forEach(o -> {
                final JSONObject user = (JSONObject) o;
                final String userId = user.getString("id");
                final String name = user.getString("name");

                editor.getOrCreateOtherUser(userId, name);
            });
        }
    }

    @Override
    public void stop() {
        editor.getOrCreateAccord().listeners().removePropertyChangeListener(availableUsersPropertyChangeListener);
    }

    public void onUserSelected(Consumer<String> callback) {
        userUserListEntryHashMap.forEach((s, entry) -> {
            entry.onClick(callback::accept);
        });
    }
}
