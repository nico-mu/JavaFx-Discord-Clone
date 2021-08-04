package de.uniks.stp.controller;


import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.component.ListComponent;
import de.uniks.stp.component.PrivateChatNavUserListEntry;
import de.uniks.stp.component.UserListEntry;
import de.uniks.stp.model.Accord;
import de.uniks.stp.model.User;
import de.uniks.stp.network.rest.SessionRestClient;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

@Route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_ONLINE)
public class UserListController implements ControllerInterface {

    private final Editor editor;
    private final ListComponent<User, UserListEntry> onlineUserList;
    private final SessionRestClient restClient;
    private final PropertyChangeListener availableUsersPropertyChangeListener = this::onAvailableUsersPropertyChange;
    private final PropertyChangeListener userDescriptionChangeListener = this::onUserDescriptionChanged;

    private final PrivateChatNavUserListEntry.PrivateChatNavUserListEntryFactory privateChatNavUserListEntryFactory;

    @AssistedInject
    public UserListController(Editor editor,
                              SessionRestClient restClient,
                              ViewLoader viewLoader,
                              PrivateChatNavUserListEntry.PrivateChatNavUserListEntryFactory privateChatNavUserListEntryFactory,
                              @Assisted Parent view) {
        this.editor = editor;
        VBox onlineUsersContainer = (VBox) view;
        onlineUserList = new ListComponent<>(viewLoader);
        onlineUsersContainer.getChildren().add(onlineUserList);
        this.restClient = restClient;
        this.privateChatNavUserListEntryFactory = privateChatNavUserListEntryFactory;
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
        if (Objects.nonNull(user)) {
            Platform.runLater(() -> onlineUserList.removeElement(user));
            user.listeners().removePropertyChangeListener(User.PROPERTY_DESCRIPTION, userDescriptionChangeListener);
        }
    }

    private void userJoined(final User user) {
        if (Objects.nonNull(user)) {
            Platform.runLater(() -> onlineUserList.addElement(user, privateChatNavUserListEntryFactory.create(user)));
            user.listeners().addPropertyChangeListener(User.PROPERTY_DESCRIPTION, userDescriptionChangeListener);
        }
    }

    @Override
    public void init() {
        final Accord accord = editor.getOrCreateAccord();
        accord.listeners().addPropertyChangeListener(Accord.PROPERTY_OTHER_USERS, availableUsersPropertyChangeListener);
        restClient.requestOnlineUsers(this::handleUserOnlineRequest);
    }

    private void handleUserOnlineRequest(final HttpResponse<JsonNode> response) {
        if (response.isSuccess()) {
            final JSONArray data = response.getBody().getObject().getJSONArray("data");
            final User currentUser = editor.getOrCreateAccord().getCurrentUser();
            data.forEach(o -> {
                final JSONObject jsonUser = (JSONObject) o;
                final String userId = jsonUser.getString("id");
                final String name = jsonUser.getString("name");
                final String description = jsonUser.getString("description");

                if(!name.equals(currentUser.getName())) {
                    User otherUser = editor.getOrCreateOtherUser(userId, name);

                    if (Objects.nonNull(otherUser)) {
                        final User user = otherUser.setStatus(true).setDescription(description);
                        userJoined(user);
                    }
                }
            });
        }
    }

    @Override
    public void stop() {
        final Accord accord = editor.getOrCreateAccord();
        accord.listeners().removePropertyChangeListener(Accord.PROPERTY_OTHER_USERS, availableUsersPropertyChangeListener);

        for (User userModel : onlineUserList.getModels()) {
            userModel.listeners().removePropertyChangeListener(User.PROPERTY_DESCRIPTION, userDescriptionChangeListener);
        }
    }

    private void onUserDescriptionChanged(PropertyChangeEvent propertyChangeEvent) {
        User user = (User) propertyChangeEvent.getSource();
        String description = (String) propertyChangeEvent.getNewValue();
        UserListEntry userListEntry = onlineUserList.getElement(user);

        if(Objects.nonNull(userListEntry)) {
            Platform.runLater(() -> {
                userListEntry.setDescription(description);
            });
        }
    }

    @AssistedFactory
    public interface UserListControllerFactory {
        UserListController create(Parent view);
    }
}
