package de.uniks.stp.controller;

import de.uniks.stp.Editor;
import de.uniks.stp.component.*;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.network.RestClient;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Objects;

public class NavBarListController implements ControllerInterface {

    private final Parent view;
    private final Editor editor;
    private final NavBarList navBarList;
    private final String NAV_BAR_CONTAINER_ID = "#nav-bar";

    private AnchorPane anchorPane;

    private final HashMap<Server, NavBarServerElement> navBarServerElementHashMap = new HashMap<>();
    PropertyChangeListener availableServersPropertyChangeListener = this::onAvailableServersPropertyChange;


    public NavBarListController(Parent view, Editor editor) {
        this.view = view;
        this.editor = editor;
        this.navBarList = new NavBarList();
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
            final NavBarServerElement serverElement = new NavBarServerElement(newValue);
            navBarServerElementHashMap.put(newValue, serverElement);
            Platform.runLater(() -> navBarList.addElement(serverElement));
        } else if (Objects.isNull(newValue)) {
            // server removed
            NavBarServerElement serverElement = navBarServerElementHashMap.remove(oldValue);
            Platform.runLater(() -> navBarList.removeElement(serverElement));
        }
    }

    @Override
    public void stop() {
        editor.getOrCreateAccord()
            .getCurrentUser()
            .listeners()
            .removePropertyChangeListener(availableServersPropertyChangeListener);
    }
}
