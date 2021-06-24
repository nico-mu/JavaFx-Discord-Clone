package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.component.*;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.notification.NotificationEvent;
import de.uniks.stp.notification.NotificationService;
import de.uniks.stp.notification.SubscriberInterface;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Objects;

import static de.uniks.stp.model.Category.PROPERTY_CHANNELS;
import static de.uniks.stp.model.Server.PROPERTY_CATEGORIES;


public class ServerCategoryListController implements ControllerInterface, SubscriberInterface {

    private Channel defaultChannel;
    private final Parent view;
    private final Editor editor;
    private final ServerCategoryList serverCategoryList;
    private final Server model;
    private VBox vBox;
    private final HashMap<Category, ServerCategoryElement> categoryElementHashMap;
    private final HashMap<Channel, ServerChannelElement> channelElementHashMap;

    PropertyChangeListener categoriesPropertyChangeListener = this::onCategoriesPropertyChanged;
    PropertyChangeListener channelPropertyChangeListener = this::onChannelPropertyChanged;
    PropertyChangeListener categoryNamePropertyChangeListener = this::onCatNamePropertyChanged;
    PropertyChangeListener channelNamePropertyChangeListener = this::onChannelNamePropertyChanged;

    public ServerCategoryListController(Parent view, Editor editor, Server model) {
        this.view = view;
        this.editor = editor;
        this.model = model;
        this.serverCategoryList = new ServerCategoryList();
        categoryElementHashMap = new HashMap<>();
        channelElementHashMap = new HashMap<>();
        NotificationService.registerChannelSubscriber(this);
    }

    @Override
    public void init() {
        vBox = (VBox) view;
        vBox.getChildren().add(serverCategoryList);
        serverCategoryList.setPrefHeight(vBox.getPrefHeight());

        model.listeners().addPropertyChangeListener(PROPERTY_CATEGORIES, categoriesPropertyChangeListener);

        for(Category category : model.getCategories()) {
            categoryAdded(category);
            for(Channel channel : category.getChannels()) {
                channelAdded(category, channel);
            }
        }
        goToDefaultChannel();
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
            for(Channel channel: category.getChannels()){
                NotificationService.removePublisher(channel);
            }

            HashMap<String, String> currentArgs = Router.getCurrentArgs();
            // in case a channel of the deleted category is currently shown: reload server
            if(currentArgs.containsKey(":categoryId") && currentArgs.get(":categoryId").equals(category.getId())){
                RouteArgs args = new RouteArgs().addArgument(":id", model.getId());
                Platform.runLater(()-> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
            }
            //else: remove category element in list
            else{
                category.listeners().removePropertyChangeListener(PROPERTY_CHANNELS, channelPropertyChangeListener);
                final ServerCategoryElement serverCategoryElement = categoryElementHashMap.remove(category);
                Platform.runLater(() -> serverCategoryList.removeElement(serverCategoryElement));
                category.listeners().removePropertyChangeListener(Category.PROPERTY_NAME, categoryNamePropertyChangeListener);
            }
        }
    }

    private void categoryAdded(final Category category) {
        if (Objects.nonNull(category) && !categoryElementHashMap.containsKey(category)) {
            category.listeners().addPropertyChangeListener(PROPERTY_CHANNELS, channelPropertyChangeListener);
            final ServerCategoryElement serverCategoryElement = new ServerCategoryElement(category);
            categoryElementHashMap.put(category, serverCategoryElement);
            Platform.runLater(() -> serverCategoryList.addElement(serverCategoryElement));
            category.listeners().addPropertyChangeListener(Category.PROPERTY_NAME, categoryNamePropertyChangeListener);
        }
    }

    private void goToDefaultChannel() {
        if(channelElementHashMap.containsKey(defaultChannel) && !Router.getCurrentArgs().containsKey(":channelId")) {
            goToChannel(defaultChannel);

            RouteArgs args = new RouteArgs();
            args.addArgument(":id", defaultChannel.getCategory().getServer().getId());
            args.addArgument(":categoryId", defaultChannel.getCategory().getId());
            args.addArgument(":channelId", defaultChannel.getId());
            Platform.runLater(() -> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER + Constants.ROUTE_CHANNEL, args));
        }
    }

    private void goToChannel(Channel channel) {
        if(channelElementHashMap.containsKey(channel)) {
            ServerChannelElement element = channelElementHashMap.get(channel);
            serverCategoryList.setActiveElement(element);
            NotificationService.consume(channel);
        }
    }

    private void onChannelPropertyChanged(PropertyChangeEvent propertyChangeEvent) {
        final Channel oldValue = (Channel) propertyChangeEvent.getOldValue();
        final Channel newValue = (Channel) propertyChangeEvent.getNewValue();
        final Category category = (Category) propertyChangeEvent.getSource();

        if (Objects.isNull(oldValue)) {
            channelAdded(newValue.getCategory(), newValue);
        } else if (Objects.isNull(newValue)) {
            channelRemoved(category, oldValue);
        }
    }

    private void onCatNamePropertyChanged(PropertyChangeEvent propertyChangeEvent) {
        Category category = (Category) propertyChangeEvent.getSource();
        String newName = (String) propertyChangeEvent.getNewValue();
        if (Objects.nonNull(category) && Objects.nonNull(newName) && categoryElementHashMap.containsKey(category)) {
            categoryElementHashMap.get(category).updateText(newName);
        }
    }

    private void onChannelNamePropertyChanged(PropertyChangeEvent propertyChangeEvent) {
        Channel channel = (Channel) propertyChangeEvent.getSource();
        String newName = (String) propertyChangeEvent.getNewValue();
        if (Objects.nonNull(channel) && Objects.nonNull(newName) && channelElementHashMap.containsKey(channel)) {
            channelElementHashMap.get(channel).updateText(newName);
        }
    }

    private void channelRemoved(final Category category, final Channel channel) {
        if (Objects.nonNull(category) && Objects.nonNull(channel) && channelElementHashMap.containsKey(channel)) {
            NotificationService.removePublisher(channel);

            HashMap<String, String> currentArgs = Router.getCurrentArgs();
            // in case the deleted channel is currently shown: reload server
            if(currentArgs.containsKey(":categoryId") && currentArgs.containsKey(":categoryId")
                    && currentArgs.get(":categoryId").equals(category.getId())
                    && currentArgs.get(":channelId").equals(channel.getId())){
                RouteArgs args = new RouteArgs().addArgument(":id", model.getId());
                Platform.runLater(()-> Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
            }
            // else: remove channel element in list
            else{
                final ServerCategoryElement serverCategoryElement = categoryElementHashMap.get(category);
                final ServerChannelElement serverChannelElement = channelElementHashMap.remove(channel);
                Platform.runLater(() -> serverCategoryElement.removeChannelElement(serverChannelElement));
                channel.listeners().removePropertyChangeListener(Channel.PROPERTY_NAME, channelNamePropertyChangeListener);
            }
        }
    }

    private void channelAdded(final Category category, final Channel channel) {
        if (Objects.nonNull(category) && Objects.nonNull(channel) && Objects.nonNull(channel.getName()) &&
            !channelElementHashMap.containsKey(channel) && categoryElementHashMap.containsKey(category)) {
            final ServerCategoryElement serverCategoryElement = categoryElementHashMap.get(category);
            boolean voice = channel.getType().equals("audio");
            final ServerChannelElement serverChannelElement = voice ? new ServerVoiceChannelElement(channel, editor) : new ServerTextChannelElement(channel, editor);
            NotificationService.register(channel);
            channel.listeners().addPropertyChangeListener(Channel.PROPERTY_NAME, channelNamePropertyChangeListener);
            channelElementHashMap.put(channel, serverChannelElement);
            Platform.runLater(() -> {
                serverCategoryElement.addChannelElement(serverChannelElement);
                if (serverChannelElement.getChannelTextId().equals(channel.getId() + "-ChannelElementText")) {
                    serverChannelElement.setNotificationCount(NotificationService.getPublisherNotificationCount(channel));
                }
            });
            HashMap<String, String> currentRouteArgs = Router.getCurrentArgs();
            // show ServerChatView of first loaded channel
            if (Objects.isNull(defaultChannel)) {
                defaultChannel = channel;
                if (!currentRouteArgs.containsKey(":channelId")) {
                    goToDefaultChannel();
                }
            }
            if(currentRouteArgs.containsKey(":channelId") && currentRouteArgs.get(":channelId").equals(channel.getId())) {
                goToChannel(channel);
            }
        }
    }

    @Override
    public void stop() {
        model.listeners().removePropertyChangeListener(PROPERTY_CATEGORIES, categoriesPropertyChangeListener);

        for (Category category : model.getCategories()) {
            category.listeners().removePropertyChangeListener(PROPERTY_CHANNELS, channelPropertyChangeListener);
            category.listeners().removePropertyChangeListener(Category.PROPERTY_NAME, categoryNamePropertyChangeListener);
        }
        for (Channel channel : model.getChannels()) {
            channel.listeners().removePropertyChangeListener(Channel.PROPERTY_NAME, channelNamePropertyChangeListener);
        }
        channelElementHashMap.clear();
        categoryElementHashMap.clear();
        NotificationService.removeChannelSubscriber(this);
    }

    @Override
    public void onChannelNotificationEvent(NotificationEvent event) {
        Channel channel = (Channel) event.getSource();
        if (Objects.nonNull(channel) && channelElementHashMap.containsKey(channel)) {
            channelElementHashMap.get(channel).setNotificationCount(event.getNotifications());
        }
    }

    @Override
    public void onUserNotificationEvent(NotificationEvent event) {

    }
}
