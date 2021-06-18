package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.component.NavBarList;
import de.uniks.stp.component.NavBarServerElement;
import de.uniks.stp.component.NavBarUserElement;
import de.uniks.stp.event.NavBarHomeElementActiveEvent;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.network.ServerInformationHandler;
import de.uniks.stp.network.WebSocketService;
import de.uniks.stp.notification.NotificationEvent;
import de.uniks.stp.notification.NotificationService;
import de.uniks.stp.notification.SubscriberInterface;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import de.uniks.stp.router.Router;
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
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class NavBarListController implements ControllerInterface, SubscriberInterface {

    private final static Logger log = LoggerFactory.getLogger(NavBarListController.class);
    private final Parent view;
    private final Editor editor;
    private final NavBarList navBarList;
    private final String NAV_BAR_CONTAINER_ID = "#nav-bar";

    private AnchorPane anchorPane;
    private RestClient restClient;
    private ServerInformationHandler serverInformationHandler;

    // needed for property change listener clean up
    private final ConcurrentHashMap<Server, NavBarServerElement> navBarServerElementHashMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<User, NavBarUserElement> navBarUserElementHashMap = new ConcurrentHashMap<>();
    PropertyChangeListener availableServersPropertyChangeListener = this::onAvailableServersPropertyChange;

    public NavBarListController(Parent view, Editor editor) {
        this.view = view;
        this.editor = editor;
        this.navBarList = new NavBarList(editor);
        this.restClient = NetworkClientInjector.getRestClient();
        this.serverInformationHandler = new ServerInformationHandler(editor);
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

        //TODO: show spinner
        restClient.getServers(this::callback);
        NotificationService.registerChannelSubscriber(this);
        NotificationService.registerUserSubscriber(this);
    }

    private void serverAdded(final Server server) {
        if (Objects.nonNull(server) && !navBarServerElementHashMap.containsKey(server)) {
            WebSocketService.addServerWebSocket(server.getId());  // enables sending & receiving messages
            final NavBarServerElement navBarElement = new NavBarServerElement(server);
            navBarElement.setNotificationCount(NotificationService.getServerNotificationCount(server));
            navBarServerElementHashMap.put(server, navBarElement);
            Platform.runLater(() -> navBarList.addServerElement(navBarElement));
        }
    }

    private void serverRemoved(final Server server) {
        // in case the deleted server is currently shown: show home screen
        HashMap<String, String> currentArgs = Router.getCurrentArgs();
        if(currentArgs.containsKey(":id")){
            if(currentArgs.get(":id").equals(server.getId())){
                Platform.runLater(()-> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_ONLINE));
                navBarList.fireEvent(new NavBarHomeElementActiveEvent());
            }
        }

        if (Objects.nonNull(server) && navBarServerElementHashMap.containsKey(server)) {
            final NavBarServerElement navBarElement = navBarServerElementHashMap.remove(server);
            Platform.runLater(() -> navBarList.removeElement(navBarElement));
            for (Channel channel : server.getChannels()) {
                // NotificationService.removePublisher(channel);
            }
        }
    }

    @Override
    public void route(RouteInfo routeInfo, RouteArgs args) {
        //no subroutes
    }

    protected void callback(HttpResponse<JsonNode> response) {
        if (response.isSuccess()) {
            JSONArray jsonArray = response.getBody().getObject().getJSONArray("data");
            for (Object element : jsonArray) {
                JSONObject jsonObject = (JSONObject) element;
                final String name = jsonObject.getString("name");
                final String serverId = jsonObject.getString("id");

                final Server server = editor.getOrCreateServer(serverId, name);
                serverAdded(server);
                restClient.getServerInformation(serverId, serverInformationHandler::handleServerInformationRequest);
                restClient.getCategories(server.getId(), (msg) -> serverInformationHandler.handleCategories(msg, server));
            }
            if (Router.getCurrentArgs().containsKey(":id") && Router.getCurrentArgs().containsKey(":channelId")) {
                String activeServerId = Router.getCurrentArgs().get(":id");
                for (Server server : editor.getOrCreateAccord().getCurrentUser().getAvailableServers()) {
                    if (server.getId().equals(activeServerId) && navBarServerElementHashMap.containsKey(server)) {
                        navBarList.setActiveElement(navBarServerElementHashMap.get(server));
                    }
                }
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

    @Override
    public void stop() {
        editor.getOrCreateAccord()
            .getCurrentUser()
            .listeners()
            .removePropertyChangeListener(availableServersPropertyChangeListener);

        navBarUserElementHashMap.clear();
        navBarServerElementHashMap.clear();

        NotificationService.removeChannelSubscriber(this);
        NotificationService.removeUserSubscriber(this);
    }

    @Override
    public void onChannelNotificationEvent(NotificationEvent event) {
        Channel channel = (Channel) event.getSource();
        if (Objects.nonNull(channel)) {
            Server server;
            if (Objects.isNull(channel.getCategory())) {
                server = channel.getServer();
            } else {
                server = channel.getCategory().getServer();
            }
            if (Objects.nonNull(server) && navBarServerElementHashMap.containsKey(server)) {
                navBarServerElementHashMap.get(server).setNotificationCount(NotificationService.getServerNotificationCount(server));
            }
        }
    }

    @Override
    public void onUserNotificationEvent(NotificationEvent event) {
        User user = (User) event.getSource();
        if (Objects.nonNull(user)) {
            if (!navBarUserElementHashMap.containsKey(user)) {
                NavBarUserElement navBarUserElement = new NavBarUserElement(user);
                navBarUserElementHashMap.put(user, navBarUserElement);
                Platform.runLater(() -> {
                    navBarList.addUserElement(navBarUserElement);
                });
            }
            NavBarUserElement userElement = navBarUserElementHashMap.get(user);
            userElement.setNotificationCount(event.getNotifications());
            if (event.getNotifications() == 0) {
                navBarUserElementHashMap.remove(user);
                Platform.runLater(() -> {
                    navBarList.removeElement(userElement);
                });
            }
        }
    }
}
