package de.uniks.stp.controller;


import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.component.UserList;
import de.uniks.stp.component.UserListEntry;
import de.uniks.stp.model.Accord;
import de.uniks.stp.model.User;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import javafx.application.Platform;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Objects;

@Route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_ONLINE)
public class UserListController implements ControllerInterface {
    private final HashMap<User, UserListEntry> userUserListEntryHashMap;
    private final UserList userList;
    private final Editor editor;

    private final PropertyChangeListener availableUsersPropertyChangeListener = this::onAvailableUsersPropertyChange;

    public UserListController(final UserList userList, final Editor editor) {
        this.userList = userList;
        this.editor = editor;

        userUserListEntryHashMap = new HashMap<>();
    }

    private void onAvailableUsersPropertyChange(final PropertyChangeEvent propertyChangeEvent) {
        final User oldValue = (User) propertyChangeEvent.getOldValue();
        final User newValue = (User) propertyChangeEvent.getNewValue();

        if (Objects.isNull(oldValue)) {
            userJoined(newValue);
        } else if (Objects.isNull(newValue)) {
            userLeft(oldValue);
        }
    }

    private void userLeft(final User user) {
        if (Objects.nonNull(user) && userUserListEntryHashMap.containsKey(user)) {
            final UserListEntry userListEntry = userUserListEntryHashMap.remove(user);
            Platform.runLater(() -> userList.removeUserListEntry(userListEntry));
        }
    }

    private void userJoined(final User user) {
        if (Objects.nonNull(user) && !userUserListEntryHashMap.containsKey(user)) {
            final UserListEntry userListEntry = new UserListEntry(user);
            userUserListEntryHashMap.put(user, userListEntry);
            Platform.runLater(() -> userList.addUserListEntry(userListEntry));
        }
    }

    @Override
    public void init() {
        final Accord accord = editor.getOrCreateAccord();
        accord.listeners().addPropertyChangeListener(Accord.PROPERTY_OTHER_USERS, availableUsersPropertyChangeListener);

        final RestClient restClient = NetworkClientInjector.getRestClient();
        restClient.requestOnlineUsers(this::handleUserOnlineRequest);
    }

    private void handleUserOnlineRequest(final HttpResponse<JsonNode> response) {
        if (response.isSuccess()) {
            final JSONArray data = response.getBody().getObject().getJSONArray("data");
            data.forEach(o -> {
                final JSONObject jsonUser = (JSONObject) o;
                final String userId = jsonUser.getString("id");
                final String name = jsonUser.getString("name");

                final User user = editor.getOrCreateOtherUser(userId, name).setStatus(true);
                userJoined(user);
            });
        }
    }

    @Override
    public void stop() {
        final Accord accord = editor.getOrCreateAccord();
        accord.listeners().removePropertyChangeListener(availableUsersPropertyChangeListener);
        userUserListEntryHashMap.clear();
    }

}
