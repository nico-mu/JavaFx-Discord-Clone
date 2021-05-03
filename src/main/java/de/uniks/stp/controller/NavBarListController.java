package de.uniks.stp.controller;

import de.uniks.stp.Editor;
import de.uniks.stp.component.NavBarElement;
import de.uniks.stp.component.NavBarHomeElement;
import de.uniks.stp.component.NavBarServerElement;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.network.RestClient;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Objects;

public class NavBarListController extends ListViewController {

    private final HashMap<Server, NavBarServerElement> navBarServerElementHashMap = new HashMap<>();
    NavBarElement currentActiveElement;
    PropertyChangeListener availableServersPropertyChangeListener = this::onAvailableServersPropertyChange;

    public NavBarListController(Parent view, Editor editor) {
        super(view, editor);
    }

    @Override
    public void init() {
        super.init();
        container.getStyleClass().add("nav-bar-vbox");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("nav-bar-vbox");

        //add home element
        NavBarHomeElement homeElement = new NavBarHomeElement(this::setActiveElement);
        container.getChildren().add(homeElement.getRootElement());
        currentActiveElement = homeElement;
        currentActiveElement.setActive(true);

        editor.getOrCreateAccord()
            .getCurrentUser()
            .listeners()
            .addPropertyChangeListener(User.PROPERTY_AVAILABLE_SERVERS, availableServersPropertyChangeListener);

        //TODO: show spinner
        RestClient.getServers(this::callback);
    }

    protected void callback(HttpResponse<JsonNode> response) {
        if (response.isSuccess()) {
            //TODO: hide spinner
            JSONArray jsonArray = response.getBody().getObject().getJSONArray("data");
            for (Object element : jsonArray) {
                JSONObject jsonObject = (JSONObject) element;
                String name = jsonObject.getString("name");
                String serverId = jsonObject.getString("id");

                editor.getOrCreateAccord()
                    .getCurrentUser()
                    .withAvailableServers(new Server().setName(name).setId(serverId));
            }
        }
        else {
            //TODO: show error message
        }
    }

    private void onAvailableServersPropertyChange(PropertyChangeEvent propertyChangeEvent) {
        final Server oldValue = (Server) propertyChangeEvent.getOldValue();
        final Server newValue = (Server) propertyChangeEvent.getNewValue();

        if (Objects.isNull(oldValue)) {
            // server added
            final NavBarServerElement serverElement = new NavBarServerElement(newValue, this::setActiveElement);
            navBarServerElementHashMap.put(newValue, serverElement);
            Platform.runLater(() -> container.getChildren().add(serverElement.getRootElement()));
        } else if (Objects.isNull(newValue)) {
            // server removed
            NavBarServerElement serverElement = navBarServerElementHashMap.remove(oldValue);
            Platform.runLater(() -> container.getChildren().remove(serverElement.getRootElement()));
        }
    }

    private void setActiveElement(NavBarElement navBarElement) {
        if (!navBarElement.equals(currentActiveElement)) {
            currentActiveElement.setActive(false);
            navBarElement.setActive(true);
            currentActiveElement = navBarElement;
        }
    }


    @Override
    public void stop() {
        super.stop();

        editor.getOrCreateAccord()
            .getCurrentUser()
            .listeners()
            .removePropertyChangeListener(availableServersPropertyChangeListener);
    }
}
