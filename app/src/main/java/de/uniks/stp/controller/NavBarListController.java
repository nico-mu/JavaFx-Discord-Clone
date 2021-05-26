package de.uniks.stp.controller;

import de.uniks.stp.Editor;
import de.uniks.stp.component.NavBarList;
import de.uniks.stp.component.NavBarServerElement;
import de.uniks.stp.component.NavBarUserElement;
import de.uniks.stp.model.*;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.network.WebSocketService;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class NavBarListController implements ControllerInterface {

    private final static Logger log = LoggerFactory.getLogger(NavBarListController.class);
    private final Parent view;
    private final Editor editor;
    private final NavBarList navBarList;
    private final String NAV_BAR_CONTAINER_ID = "#nav-bar";

    private AnchorPane anchorPane;
    private RestClient restClient;

    private final ConcurrentHashMap<Server, NavBarServerElement> navBarServerElementHashMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UserNotification, NavBarUserElement> navBarUserElementHashMap = new ConcurrentHashMap<>();
    PropertyChangeListener availableServersPropertyChangeListener = this::onAvailableServersPropertyChange;
    // handles incoming messages and adds new users to Navbar
    PropertyChangeListener privateMessagePropertyChangeListener = this::onPrivateMessagePropertyChange;
    // clean up of the notification on edge cases e.g. User clicks on user in online list instead of notification
    PropertyChangeListener chatPartnerPropertyChangeListener = this::chatPartnerPropertyChange;
    // adding and removing notification from sidebar
    PropertyChangeListener userNotificationPropertyChangeListener = this::userNotificationPropertyChange;

    public NavBarListController(Parent view, Editor editor) {
        this.view = view;
        this.editor = editor;
        this.navBarList = new NavBarList(editor);
        this.restClient = NetworkClientInjector.getRestClient();
    }

    @Override
    public void init() {
        anchorPane = (AnchorPane) this.view.lookup(NAV_BAR_CONTAINER_ID);
        anchorPane.getChildren().add(navBarList);
        navBarList.setPrefHeight(anchorPane.getPrefHeight());

        editor.getOrCreateAccord()
            .getCurrentUser()
            .listeners()
            .addPropertyChangeListener(User.PROPERTY_AVAILABLE_SERVERS, availableServersPropertyChangeListener);

        editor.getOrCreateAccord()
            .getCurrentUser()
            .listeners()
            .addPropertyChangeListener(User.PROPERTY_PRIVATE_CHAT_MESSAGES, privateMessagePropertyChangeListener);

        editor.getOrCreateAccord()
            .getCurrentUser()
            .listeners()
            .addPropertyChangeListener(User.PROPERTY_CURRENT_CHAT_PARTNER, chatPartnerPropertyChangeListener);

        editor.getOrCreateAccord()
            .getCurrentUser()
            .listeners()
            .addPropertyChangeListener(User.PROPERTY_RECEIVED_USER_NOTIFICATIONS, userNotificationPropertyChangeListener);

        //TODO: show spinner
        restClient.getServers(this::callback);
    }

    private void serverAdded(final Server server) {
        if (Objects.nonNull(server) && !navBarServerElementHashMap.containsKey(server)) {
            WebSocketService.addServerWebSocket(server.getId());  // enables sending & receiving messages
            final ServerNotification serverNotification = new ServerNotification().setSender(server);
            serverNotification.setNotificationCounter(0);
            final NavBarServerElement navBarElement = new NavBarServerElement(serverNotification);
            navBarServerElementHashMap.put(server, navBarElement);
            Platform.runLater(() -> navBarList.addServerElement(navBarElement));
        }
    }

    private void serverRemoved(final Server server) {
        if (Objects.nonNull(server) && navBarServerElementHashMap.containsKey(server)) {
            final NavBarServerElement navBarElement = navBarServerElementHashMap.remove(server);
            Platform.runLater(() -> navBarList.removeElement(navBarElement));
        }
    }

    @Override
    public void route(RouteInfo routeInfo, RouteArgs args) {
        //no subroutes
    }

    protected void callback(HttpResponse<JsonNode> response) {
        if (response.isSuccess()) {
            //TODO: hide spinner
            JSONArray jsonArray = response.getBody().getObject().getJSONArray("data");
            for (Object element : jsonArray) {
                JSONObject jsonObject = (JSONObject) element;
                final String name = jsonObject.getString("name");
                final String serverId = jsonObject.getString("id");

                final Server server = editor.getOrCreateServer(serverId, name);
                serverAdded(server);
            }
        } else {
            log.error("Response was unsuccessful, error code: " + response.getStatusText());
        }
    }

    private void onAvailableServersPropertyChange(PropertyChangeEvent propertyChangeEvent) {
        final Server oldValue = (Server) propertyChangeEvent.getOldValue();
        final Server newValue = (Server) propertyChangeEvent.getNewValue();

        if (Objects.isNull(oldValue)) {
            // server added
            serverAdded(newValue);
        } else if (Objects.isNull(newValue)) {
            // server removed
            serverRemoved(oldValue);
        }
    }

    private void onPrivateMessagePropertyChange(PropertyChangeEvent propertyChangeEvent) {
        // getOldValue for non null sender
        DirectMessage dm = (DirectMessage) propertyChangeEvent.getOldValue();
        if (Objects.nonNull(dm)) {
            User sender = dm.getSender();
            if (Objects.nonNull(sender)) {
                if (!sender.equals(editor.getOrCreateAccord().getCurrentUser())
                    && !sender.equals(editor.getOrCreateAccord().getCurrentUser().getCurrentChatPartner())) {
                    handlePrivateNotification(sender);
                }
            }
        }
    }

    private void handleServerNotification(Server server) {
        if (!navBarServerElementHashMap.containsKey(server)) {
            ServerNotification serverNotification = new ServerNotification().setSender(server);
            serverNotification.setNotificationCounter(1);
            serverNotification.setSender(server);
            navBarServerElementHashMap.put(server, new NavBarServerElement(serverNotification));
        } else {
            server
                .getServerNotification()
                .setNotificationCounter(server.getServerNotification().getNotificationCounter() + 1);
            log.info(server.getServerNotification().getNotificationCounter() + " unread Notification(s) from " + server);
        }
    }

    private void handlePrivateNotification(User user) {
        if (Objects.nonNull(user)) {
            if (Objects.isNull(user.getSentUserNotification())) {
                if (!navBarUserElementHashMap.containsKey(user)) {
                    UserNotification userNotification =
                        new UserNotification()
                            .setSender(user);
                    userNotification.setNotificationCounter(0);

                    navBarUserElementHashMap.put(userNotification, new NavBarUserElement(userNotification));
                    userNotification.setReceiver(editor.getOrCreateAccord().getCurrentUser());
                    user.setSentUserNotification(userNotification);
                    editor.getOrCreateAccord().getCurrentUser().withReceivedUserNotifications(userNotification);
                }
            }
            Notification userNotification = user.getSentUserNotification();
            userNotification.setNotificationCounter(userNotification.getNotificationCounter() + 1);
            log.info(userNotification.getNotificationCounter() + " unread Notification(s) from " + user);
        }
    }

    private void chatPartnerPropertyChange(PropertyChangeEvent propertyChangeEvent) {
        User chatPartner = (User) propertyChangeEvent.getNewValue();
        if (Objects.nonNull(chatPartner)) {
            if (navBarUserElementHashMap.containsKey(chatPartner)) {
                chatPartner.getSentUserNotification().setNotificationCounter(0);
                editor.getOrCreateAccord().getCurrentUser()
                    .withoutReceivedUserNotifications(chatPartner.getSentUserNotification());
                chatPartner.setSentUserNotification(null);
            }
        }
    }

    private void userNotificationPropertyChange(PropertyChangeEvent propertyChangeEvent) {
        final UserNotification oldValue = (UserNotification) propertyChangeEvent.getOldValue();
        final UserNotification newValue = (UserNotification) propertyChangeEvent.getNewValue();

        if (Objects.isNull(oldValue)) {
            // first Notification received
            if (navBarUserElementHashMap.containsKey(newValue)) {
                Platform.runLater(() -> {
                    navBarList.addUserElement(navBarUserElementHashMap.get(newValue));
                });
            }
        } else if (Objects.isNull(newValue)) {
            // Notifications read
            if (navBarUserElementHashMap.containsKey(oldValue)) {
                Platform.runLater(() -> {
                    navBarList.removeElement(navBarUserElementHashMap.get(oldValue));
                });
            }
        }
    }

    @Override
    public void stop() {
        editor.getOrCreateAccord()
            .getCurrentUser()
            .listeners()
            .removePropertyChangeListener(availableServersPropertyChangeListener);

        editor.getOrCreateAccord()
            .getCurrentUser()
            .listeners()
            .removePropertyChangeListener(privateMessagePropertyChangeListener);

        editor.getOrCreateAccord()
            .getCurrentUser()
            .listeners()
            .removePropertyChangeListener(chatPartnerPropertyChangeListener);

        for (Map.Entry<UserNotification, NavBarUserElement> entry : navBarUserElementHashMap.entrySet()) {
            entry.getValue().stop();
        }

        for (Map.Entry<Server, NavBarServerElement> entry : navBarServerElementHashMap.entrySet()) {
            entry.getValue().stop();
        }

        navBarUserElementHashMap.clear();
        navBarServerElementHashMap.clear();
    }
}
