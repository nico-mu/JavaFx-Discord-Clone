package de.uniks.stp.controller;

import de.uniks.stp.Editor;
import de.uniks.stp.component.NavBarList;
import de.uniks.stp.component.NavBarNotificationElement;
import de.uniks.stp.component.NavBarServerElement;
import de.uniks.stp.component.NavBarUserElement;
import de.uniks.stp.model.DirectMessage;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.network.WebSocketClient;
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
    private final ConcurrentHashMap<User, NavBarUserElement> navBarUserElementHashMap = new ConcurrentHashMap<>();
    PropertyChangeListener availableServersPropertyChangeListener = this::onAvailableServersPropertyChange;
    // handles incoming messages and adds new users to Navbar
    PropertyChangeListener messagePropertyChangeListener = this::onMessagePropertyChange;
    // clean up of the notification when its set to zero
    PropertyChangeListener notificationPropertyChangeListener = this::notificationPropertyChange;
    // clean up of the notification on edge cases e.g. User clicks on user in online list instead of notification
    PropertyChangeListener chatPartnerPropertyChangeListener = this::chatPartnerPropertyChange;

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
            .addPropertyChangeListener(User.PROPERTY_PRIVATE_CHAT_MESSAGES, messagePropertyChangeListener);

        editor.getOrCreateAccord()
            .getCurrentUser()
            .listeners()
            .addPropertyChangeListener(User.PROPERTY_CURRENT_CHAT_PARTNER, chatPartnerPropertyChangeListener);


        //TODO: show spinner
        restClient.getServers(this::callback);
    }

    private void serverAdded(final Server server) {
        if (Objects.nonNull(server) && !navBarServerElementHashMap.containsKey(server)) {
            WebSocketService.addServerWebSocket(server.getId());  // enables sending & receiving messages
            final NavBarServerElement navBarElement = new NavBarServerElement(server);
            navBarServerElementHashMap.put(server, navBarElement);
            Platform.runLater(() -> navBarList.addServerElement(navBarElement));
        }
    }

    private void serverRemoved(final Server server) {
        if (Objects.nonNull(server) && navBarServerElementHashMap.containsKey(server)) {
            final NavBarServerElement navBarElement = navBarServerElementHashMap.remove(server);
            Platform.runLater(() -> navBarList.removeServerElement(navBarElement));
        }
    }

    private void notificationReceived(final Object object) {
        if (Objects.nonNull(object)) {
            if (object instanceof User) {
                User user = (User) object;
                navBarUserElementHashMap.computeIfAbsent(user, notification -> {
                    NavBarUserElement navBarUserElement = new NavBarUserElement(user);
                    navBarUserElement.listeners().addPropertyChangeListener(NavBarNotificationElement.PROPERTY_NOTIFICATIONS, this::notificationPropertyChange);
                    return navBarUserElement;
                });
                navBarUserElementHashMap.get(user).increaseNotifications();
                log.info(navBarUserElementHashMap.get(user).getNotifications() + " unread Notification(s) from " + object);
            }
            else if (object instanceof Server) {
                Server server = (Server) object;
                navBarServerElementHashMap.computeIfAbsent(server, notification -> {
                    NavBarServerElement navBarServerElement = new NavBarServerElement(server);
                    navBarServerElement.listeners().addPropertyChangeListener(NavBarNotificationElement.PROPERTY_NOTIFICATIONS, this::notificationPropertyChange);
                    return navBarServerElement;
                });
                navBarServerElementHashMap.get(server).increaseNotifications();
                log.info(navBarServerElementHashMap.get(server).getNotifications() + " unread Notification(s) in " + object);
            }
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
            //TODO: show error message
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

    private void onMessagePropertyChange(PropertyChangeEvent propertyChangeEvent) {
        // getOldValue for non null sender
        DirectMessage dm = (DirectMessage) propertyChangeEvent.getOldValue();
        if (Objects.nonNull(dm)) {
            User sender = dm.getSender();
            if (Objects.isNull(sender)) {
                return;
            }
            if (!sender.equals(editor.getOrCreateAccord().getCurrentUser())
                && !sender.equals(editor.getOrCreateAccord().getCurrentUser().getCurrentChatPartner())) {
                notificationReceived(sender);
            }
        }
    }

    private void notificationPropertyChange(PropertyChangeEvent propertyChangeEvent) {
        // gets mainly triggered by onMessagePropertyChange
        NavBarNotificationElement navBarElement = (NavBarNotificationElement) propertyChangeEvent.getNewValue();
        if (Objects.nonNull(navBarElement)) {
            if (navBarElement.getNotifications() == 1) {
                if (navBarElement instanceof NavBarUserElement) {
                    Platform.runLater(() -> navBarList.addUserElement(navBarElement));
                }
                else if (navBarElement instanceof NavBarServerElement) {
                    // TODO: add server notification functionality
                }
            } else if (navBarElement.getNotifications() == 0){
                navBarElementCleanUp(navBarElement);
            }
        }
    }

    private void chatPartnerPropertyChange(PropertyChangeEvent propertyChangeEvent) {
        User chatPartner = (User) propertyChangeEvent.getNewValue();
        if (Objects.nonNull(chatPartner))
        {
            if (navBarUserElementHashMap.containsKey(chatPartner)) {
                navBarUserElementHashMap.get(chatPartner).resetNotifications();
            }
        }
    }

    private void navBarElementCleanUp(NavBarNotificationElement navBarElement) {
        if (navBarElement instanceof NavBarServerElement) {
            NavBarServerElement serverElement = (NavBarServerElement) navBarElement;
            // TODO: add server notification functionality
        } else if (navBarElement instanceof NavBarUserElement) {
            NavBarUserElement userElement = (NavBarUserElement) navBarElement;
            userElement.listeners().removePropertyChangeListener(notificationPropertyChangeListener);
            navBarUserElementHashMap.remove(userElement.getModel());
            Platform.runLater(() -> navBarList.removeElement(userElement));
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
            .removePropertyChangeListener(messagePropertyChangeListener);

        editor.getOrCreateAccord()
            .getCurrentUser()
            .listeners()
            .removePropertyChangeListener(chatPartnerPropertyChangeListener);

        for (Map.Entry<User, NavBarUserElement> entry : navBarUserElementHashMap.entrySet()) {
            entry.getValue().listeners().removePropertyChangeListener(notificationPropertyChangeListener);
        }

        for (Map.Entry<Server, NavBarServerElement> entry : navBarServerElementHashMap.entrySet()) {
            entry.getValue().listeners().removePropertyChangeListener(notificationPropertyChangeListener);
        }

        navBarUserElementHashMap.clear();
        navBarServerElementHashMap.clear();
    }
}
