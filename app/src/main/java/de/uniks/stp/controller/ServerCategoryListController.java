package de.uniks.stp.controller;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.Constants;
import de.uniks.stp.component.*;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.notification.NotificationEvent;
import de.uniks.stp.notification.NotificationService;
import de.uniks.stp.notification.SubscriberInterface;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Objects;

import static de.uniks.stp.model.Category.PROPERTY_CHANNELS;
import static de.uniks.stp.model.Server.PROPERTY_CATEGORIES;


public class ServerCategoryListController implements ControllerInterface, SubscriberInterface {
    private static final Logger log = LoggerFactory.getLogger(ServerCategoryListController.class);

    private Channel defaultChannel;
    private final Parent view;
    private final ServerCategoryList serverCategoryList;
    private final Server model;
    private VBox vBox;
    private final HashMap<Category, ServerCategoryElement> categoryElementHashMap;
    private final HashMap<Channel, ServerChannelElement> channelElementHashMap;
    private final ServerCategoryElement.ServerCategoryElementFactory serverCategoryElementFactory;
    private final ServerVoiceChannelElement.ServerVoiceChannelElementFactory serverVoiceChannelElementFactory;
    private final ServerTextChannelElement.ServerTextChannelElementFactory serverTextChannelElementFactory;


    private final NotificationService notificationService;
    private final Router router;

    PropertyChangeListener categoriesPropertyChangeListener = this::onCategoriesPropertyChanged;
    PropertyChangeListener channelPropertyChangeListener = this::onChannelPropertyChanged;
    PropertyChangeListener categoryNamePropertyChangeListener = this::onCatNamePropertyChanged;
    PropertyChangeListener channelNamePropertyChangeListener = this::onChannelNamePropertyChanged;
    private final PropertyChangeListener audioMembersPropertyChangeListener = this::onAudioMembersPropertyChange;

    @AssistedInject
    public ServerCategoryListController(Router router,
                                        NotificationService notificationService,
                                        ServerCategoryList serverCategoryList,
                                        ServerCategoryElement.ServerCategoryElementFactory serverCategoryElementFactory,
                                        ServerVoiceChannelElement.ServerVoiceChannelElementFactory serverVoiceChannelElementFactory,
                                        ServerTextChannelElement.ServerTextChannelElementFactory serverTextChannelElementFactory,
                                        @Assisted Parent view,
                                        @Assisted Server model) {
        this.view = view;
        this.model = model;
        this.serverCategoryList = serverCategoryList;
        categoryElementHashMap = new HashMap<>();
        channelElementHashMap = new HashMap<>();
        this.notificationService = notificationService;
        this.router = router;
        this.serverCategoryElementFactory = serverCategoryElementFactory;
        this.serverVoiceChannelElementFactory = serverVoiceChannelElementFactory;
        this.serverTextChannelElementFactory = serverTextChannelElementFactory;
        notificationService.registerChannelSubscriber(this);
    }

    @Override
    public void init() {
        vBox = (VBox) view;
        vBox.getChildren().add(serverCategoryList);
        serverCategoryList.setPrefHeight(vBox.getPrefHeight());

        model.listeners().addPropertyChangeListener(PROPERTY_CATEGORIES, categoriesPropertyChangeListener);

        for (Category category : model.getCategories()) {
            categoryAdded(category);
            for (Channel channel : category.getChannels()) {
                channelAdded(category, channel);
            }
        }
    }

    public void setNoElementActive() {
        this.serverCategoryList.setNoElementActive();
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
            for (Channel channel : category.getChannels()) {
                notificationService.removePublisher(channel);
            }

            HashMap<String, String> currentArgs = router.getCurrentArgs();
            // in case a channel of the deleted category is currently shown: reload server
            if (currentArgs.containsKey(":categoryId") && currentArgs.get(":categoryId").equals(category.getId())) {
                RouteArgs args = new RouteArgs().addArgument(":id", model.getId());
                Platform.runLater(() -> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
            }
            //else: remove category element in list
            else {
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
            final ServerCategoryElement serverCategoryElement = serverCategoryElementFactory.create(category);
            categoryElementHashMap.put(category, serverCategoryElement);
            Platform.runLater(() -> serverCategoryList.addElement(serverCategoryElement));
            category.listeners().addPropertyChangeListener(Category.PROPERTY_NAME, categoryNamePropertyChangeListener);
        }
    }

    public void goToChannel(Channel channel) {
        if (channelElementHashMap.containsKey(channel)) {
            ServerChannelElement element = channelElementHashMap.get(channel);
            serverCategoryList.setActiveElement(element);
            notificationService.consume(channel);
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
            notificationService.removePublisher(channel);

            HashMap<String, String> currentArgs = router.getCurrentArgs();
            // in case the deleted channel is currently shown: reload server
            if (currentArgs.containsKey(":categoryId") && currentArgs.containsKey(":categoryId")
                && currentArgs.get(":categoryId").equals(category.getId())
                && currentArgs.get(":channelId").equals(channel.getId())) {
                RouteArgs args = new RouteArgs().addArgument(":id", model.getId());
                Platform.runLater(() -> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args));
            }
            // else: remove channel element in list
            else {
                final ServerCategoryElement serverCategoryElement = categoryElementHashMap.get(category);
                final ServerChannelElement serverChannelElement = channelElementHashMap.remove(channel);
                Platform.runLater(() -> serverCategoryElement.removeChannelElement(serverChannelElement));
                channel.listeners().removePropertyChangeListener(Channel.PROPERTY_NAME, channelNamePropertyChangeListener);
                removePropertyChangeListenerIfAudio(channel);
            }
        }
    }

    private void removePropertyChangeListenerIfAudio(Channel channel) {
        if ("audio".equals(channel.getType())) {
            channel.listeners().removePropertyChangeListener(Channel.PROPERTY_AUDIO_MEMBERS, audioMembersPropertyChangeListener);
        }
    }

    private void channelAdded(final Category category, final Channel channel) {
        if (Objects.nonNull(category) && Objects.nonNull(channel) && Objects.nonNull(channel.getName()) &&
            !channelElementHashMap.containsKey(channel) && categoryElementHashMap.containsKey(category)) {
            PropertyChangeSupport channelPropertyChangeListeners = channel.listeners();
            final String channelType = channel.getType();
            ServerChannelElement serverChannelElement;
            switch (channelType) {
                case "text":
                    serverChannelElement = serverTextChannelElementFactory.create(channel);
                    break;
                case "audio":
                    serverChannelElement = serverVoiceChannelElementFactory.create(channel);
                    channelPropertyChangeListeners.addPropertyChangeListener(Channel.PROPERTY_AUDIO_MEMBERS, audioMembersPropertyChangeListener);
                    break;
                default:
                    log.error("Could not create a channel of the type: {}", channelType);
                    return;
            }
            notificationService.register(channel);
            channelPropertyChangeListeners.addPropertyChangeListener(Channel.PROPERTY_NAME, channelNamePropertyChangeListener);
            channelElementHashMap.put(channel, serverChannelElement);
            final ServerCategoryElement serverCategoryElement = categoryElementHashMap.get(category);
            Platform.runLater(() -> {
                serverCategoryElement.addChannelElement(serverChannelElement);
                if ("text".equals(channelType)) {
                    serverChannelElement.setNotificationCount(notificationService.getPublisherNotificationCount(channel));
                }
            });
            HashMap<String, String> currentRouteArgs = router.getCurrentArgs();
            // show ServerChatView of first loaded channel
            if (Objects.isNull(defaultChannel)) {
                defaultChannel = channel;
            }
            if (currentRouteArgs.containsKey(":channelId") && currentRouteArgs.get(":channelId").equals(channel.getId())) {
                goToChannel(channel);
            }
        }
    }

    private void onAudioMembersPropertyChange(PropertyChangeEvent propertyChangeEvent) {
        final User oldValue = (User) propertyChangeEvent.getOldValue();
        final User newValue = (User) propertyChangeEvent.getNewValue();
        final Channel source = (Channel) propertyChangeEvent.getSource();
        final ServerVoiceChannelElement serverChannelElement = (ServerVoiceChannelElement) channelElementHashMap.get(source);

        if (Objects.isNull(oldValue)) {
            serverChannelElement.addAudioUser(newValue);
        } else if (Objects.isNull(newValue)) {
            serverChannelElement.removeAudioUser(oldValue);
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
            removePropertyChangeListenerIfAudio(channel);
        }
        channelElementHashMap.clear();
        categoryElementHashMap.clear();
        notificationService.removeChannelSubscriber(this);
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

    @AssistedFactory
    public interface ServerCategoryListControllerFactory {
        ServerCategoryListController create(Parent view, Server server);
    }
}
