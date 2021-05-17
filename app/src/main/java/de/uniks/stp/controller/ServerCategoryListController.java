package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.component.ServerCategoryElement;
import de.uniks.stp.component.ServerCategoryList;
import de.uniks.stp.component.ServerChannelElement;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Objects;

import static de.uniks.stp.model.Category.PROPERTY_CHANNELS;
import static de.uniks.stp.model.Server.PROPERTY_CATEGORIES;

@Route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER + Constants.ROUTE_CHANNEL)
public class ServerCategoryListController implements ControllerInterface {

    private final Parent view;
    private final Editor editor;
    private final ServerCategoryList serverCategoryList;
    private final RestClient restClient;
    private final Server model;
    private VBox vBox;
    private final HashMap<Category, ServerCategoryElement> categoryElementHashMap;
    private final HashMap<Channel, ServerChannelElement> channelElementHashMap;

    PropertyChangeListener categoriesPropertyChangeListener = this::onCategoriesPropertyChanged;
    PropertyChangeListener channelPropertyChangeListener = this::onChannelPropertyChanged;

    public ServerCategoryListController(Parent view, Editor editor, Server model) {
        this.view = view;
        this.editor = editor;
        this.model = model;
        this.serverCategoryList = new ServerCategoryList();
        this.restClient = NetworkClientInjector.getRestClient();
        categoryElementHashMap = new HashMap<>();
        channelElementHashMap = new HashMap<>();
    }

    @Override
    public void init() {
        vBox = (VBox)view;
        vBox.getChildren().add(serverCategoryList);
        serverCategoryList.setPrefHeight(vBox.getPrefHeight());

        model.listeners().addPropertyChangeListener(PROPERTY_CATEGORIES, categoriesPropertyChangeListener);

        restClient.getCategories(model.getId(), this::handleCategories);
    }

    private void handleCategories(HttpResponse<JsonNode> response) {
        if(response.isSuccess()) {
            JSONArray categoriesJson = response.getBody().getObject().getJSONArray("data");
            for (Object category : categoriesJson) {
                JSONObject categoryJson = (JSONObject) category;
                final String name = categoryJson.getString("name");
                final String categoryId = categoryJson.getString("id");

                Category categoryModel = editor.getOrCreateCategory(categoryId, name, model);
                categoryModel.listeners().addPropertyChangeListener(PROPERTY_CHANNELS, channelPropertyChangeListener);
                categoryAdded(categoryModel);
                restClient.getChannels(model.getId(), categoryId, this::handleChannels);
            }
        }
        else {
            //TODO: show error message
        }
    }

    private void onCategoriesPropertyChanged(PropertyChangeEvent propertyChangeEvent) {
        final Category oldValue = (Category) propertyChangeEvent.getOldValue();
        final Category newValue = (Category) propertyChangeEvent.getNewValue();

        if (Objects.isNull(oldValue)) {
            categoryAdded(newValue);
        } else if (Objects.isNull(newValue)) {
            categoryRemoved(oldValue);
        }
    }

    private void categoryRemoved(final Category category) {
        if (Objects.nonNull(category) && categoryElementHashMap.containsKey(category)) {
            final ServerCategoryElement serverCategoryElement = categoryElementHashMap.remove(category);
            Platform.runLater(() -> serverCategoryList.removeElement(serverCategoryElement));
        }
    }

    private void categoryAdded(final Category category) {
        if (Objects.nonNull(category) && !categoryElementHashMap.containsKey(category)) {
            final ServerCategoryElement serverCategoryElement = new ServerCategoryElement(category);
            categoryElementHashMap.put(category, serverCategoryElement);
            Platform.runLater(() -> serverCategoryList.addElement(serverCategoryElement));
        }
    }

    private void handleChannels(HttpResponse<JsonNode> response) {
        if(response.isSuccess()) {
            JSONArray channelsJson = response.getBody().getObject().getJSONArray("data");
            for (Object channel : channelsJson) {
                JSONObject channelJson = (JSONObject) channel;
                final String name = channelJson.getString("name");
                final String channelId = channelJson.getString("id");
                final String categoryId = channelJson.getString("category");

                Category categoryModel = editor.getCategory(categoryId, model);
                Channel channelModel = editor.getOrCreateChannel(channelId, name, categoryModel);

                channelAdded(categoryModel, channelModel);
            }
        }
        else {
            //TODO: show error message
        }
    }

    private void onChannelPropertyChanged(PropertyChangeEvent propertyChangeEvent) {
        final Channel oldValue = (Channel) propertyChangeEvent.getOldValue();
        final Channel newValue = (Channel) propertyChangeEvent.getNewValue();

        if (Objects.isNull(oldValue)) {
            channelAdded(newValue.getCategory(), newValue);
        } else if (Objects.isNull(newValue)) {
            channelRemoved(oldValue.getCategory(), oldValue);
        }
    }

    private void channelRemoved(final Category category , final Channel channel) {
        if (Objects.nonNull(category) && Objects.nonNull(channel) && channelElementHashMap.containsKey(channel)) {
            final ServerCategoryElement serverCategoryElement = categoryElementHashMap.get(category);
            final ServerChannelElement serverChannelElement = channelElementHashMap.remove(channel);
            Platform.runLater(() -> serverCategoryElement.removeChannelElement(serverChannelElement));
        }
    }

    private void channelAdded(final Category category, final Channel channel) {
        if (Objects.nonNull(category)  && Objects.nonNull(channel) && !channelElementHashMap.containsKey(channel)) {
            final ServerCategoryElement serverCategoryElement = categoryElementHashMap.get(category);
            final ServerChannelElement serverChannelElement = new ServerChannelElement(channel);
            channelElementHashMap.put(channel, serverChannelElement);
            Platform.runLater(() -> serverCategoryElement.addChannelElement(serverChannelElement));
        }
    }

    @Override
    public void stop() {
        model.listeners().removePropertyChangeListener(PROPERTY_CATEGORIES, categoriesPropertyChangeListener);

        for (Category category: model.getCategories()) {
            category.listeners().removePropertyChangeListener(PROPERTY_CHANNELS, channelPropertyChangeListener);
        }
        channelElementHashMap.clear();
        categoryElementHashMap.clear();
    }
}
