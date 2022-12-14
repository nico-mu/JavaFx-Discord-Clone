package de.uniks.stp.controller;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.component.TextWithEmoteSupport;
import de.uniks.stp.modal.CreateCategoryModal;
import de.uniks.stp.modal.InvitesModal;
import de.uniks.stp.modal.ServerSettingsModal;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import de.uniks.stp.util.AnimationUtil;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Objects;

import static de.uniks.stp.view.Views.SERVER_SCREEN;

@Route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER)
public class ServerScreenController implements ControllerInterface {
    private static final Logger log = LoggerFactory.getLogger(ServerScreenController.class);

    private static final String SERVER_NAME_ID = "#server-name";
    private static final String SERVER_CHANNEL_OVERVIEW = "#server-channel-overview";
    private static final String SERVER_CHANNEL_CONTAINER = "#server-channel-container";
    private static final String SERVER_USER_LIST_CONTAINER = "#server-user-list-container";
    private static final String SETTINGS_LABEL = "#settings-label";
    private static final String CHANNEL_NAME_LABEL = "#channel-name-label";

    private final Editor editor;
    private final Server model;
    private final ViewLoader viewLoader;

    private final VBox view;
    private HBox serverScreenView;
    private Channel selectedChannel;
    private TextWithEmoteSupport serverName;

    private VBox serverChannelOverview;
    private ServerCategoryListController categoryListController;
    private VBox serverChannelContainer;
    private BaseController serverChannelController;
    private ServerUserListController serverUserListController;
    private FlowPane serverUserListContainer;
    private Label settingsGearLabel;
    private ImageView settingsGear;
    private ContextMenu settingsContextMenu;
    private TextWithEmoteSupport channelNameLabel;
    private ChangeListener<Number> viewHeightChangedListener = this::onViewHeightChanged;


    private final ServerCategoryListController.ServerCategoryListControllerFactory serverCategoryListControllerFactory;
    private final ServerChatController.ServerChatControllerFactory serverChatControllerFactory;
    private final ServerVoiceChatController.ServerVoiceChatControllerFactory serverVoiceChatControllerFactory;
    private final ServerUserListController.ServerUserListControllerFactory serverUserListControllerFactory;
    private final InvitesModal.InvitesModalFactory invitesModalFactory;
    private final ServerSettingsModal.ServerSettingsModalFactory serverSettingsModalFactory;
    private final CreateCategoryModal.CreateCategoryModalFactory categoryModalFactory;

    private final PropertyChangeListener channelNameListener = this::onChannelNamePropertyChange;
    PropertyChangeListener serverNamePropertyChangeListener = this::onServerNamePropertyChange;

    @AssistedInject
    public ServerScreenController(ViewLoader viewLoader,
                                  Editor editor,
                                  ServerCategoryListController.ServerCategoryListControllerFactory serverCategoryListControllerFactory,
                                  ServerChatController.ServerChatControllerFactory serverChatControllerFactory,
                                  ServerVoiceChatController.ServerVoiceChatControllerFactory serverVoiceChatControllerFactory,
                                  ServerUserListController.ServerUserListControllerFactory serverUserListControllerFactory,
                                  InvitesModal.InvitesModalFactory invitesModalFactory,
                                  ServerSettingsModal.ServerSettingsModalFactory serverSettingsModalFactory,
                                  CreateCategoryModal.CreateCategoryModalFactory categoryModalFactory,
                                  @Assisted Parent view,
                                  @Assisted Server model) {
        this.view = (VBox) view;
        this.editor = editor;
        this.model = model;
        this.viewLoader = viewLoader;
        this.serverCategoryListControllerFactory = serverCategoryListControllerFactory;
        this.serverChatControllerFactory = serverChatControllerFactory;
        this.serverVoiceChatControllerFactory = serverVoiceChatControllerFactory;
        this.serverUserListControllerFactory = serverUserListControllerFactory;
        this.invitesModalFactory = invitesModalFactory;
        this.serverSettingsModalFactory = serverSettingsModalFactory;
        this.categoryModalFactory = categoryModalFactory;
    }

    @Override
    public void init() {
        serverScreenView = (HBox) viewLoader.loadView(SERVER_SCREEN);
        serverChannelOverview = (VBox) serverScreenView.lookup(SERVER_CHANNEL_OVERVIEW);
        serverChannelContainer = (VBox) serverScreenView.lookup(SERVER_CHANNEL_CONTAINER);
        serverUserListContainer = (FlowPane) serverScreenView.lookup(SERVER_USER_LIST_CONTAINER);
        settingsGearLabel = (Label) serverScreenView.lookup(SETTINGS_LABEL);
        settingsGear = (ImageView) settingsGearLabel.getGraphic();
        settingsContextMenu = settingsGearLabel.getContextMenu();

        channelNameLabel = (TextWithEmoteSupport) serverScreenView.lookup(CHANNEL_NAME_LABEL);

        view.getChildren().add(serverScreenView);
        serverName = (TextWithEmoteSupport) view.lookup(SERVER_NAME_ID);
        serverName.getRenderer().setSize(20).setScalingFactor(2);
        serverName.setText(model.getName());

        ObservableList<MenuItem> items = settingsContextMenu.getItems();
        items.get(0).setOnAction(this::onInviteUserClicked);
        items.get(1).setOnAction(this::onEditServerClicked);
        items.get(2).setOnAction(this::onCreateCategoryClicked);

        serverScreenView.setPrefHeight(view.getHeight());
        view.heightProperty().addListener(viewHeightChangedListener);

        AnimationUtil animationUtil = new AnimationUtil();
        settingsGearLabel.setOnMouseEntered(event ->  animationUtil.iconEntered(settingsGear));
        settingsGearLabel.setOnMouseExited(event ->  animationUtil.iconExited(settingsGear));
        settingsGearLabel.setOnMouseClicked(e -> settingsContextMenu.show(settingsGearLabel, Side.BOTTOM, 0, 0));

        categoryListController = serverCategoryListControllerFactory.create(serverChannelOverview, model);
        categoryListController.init();

        serverUserListController = serverUserListControllerFactory.create(serverUserListContainer, model);
        serverUserListController.init();

        model.listeners().addPropertyChangeListener(Server.PROPERTY_NAME, serverNamePropertyChangeListener);
    }

    @Override
    public ControllerInterface route(RouteInfo routeInfo, RouteArgs args) {
        subviewCleanup();
        if (routeInfo.getSubControllerRoute().equals(Constants.ROUTE_CHANNEL)) {
            final Channel channel = selectAndGetChannel(args.getArguments());
            final String channelType = channel.getType();
            switch (channelType) {
                case "text":
                    serverChannelController = serverChatControllerFactory.create(serverChannelContainer, channel);
                    break;
                case "audio":
                    serverChannelController = serverVoiceChatControllerFactory.create(serverChannelContainer, channel);
                    break;
                default:
                    log.error("Could not create a Controller for channelType: {}", channelType);
                    return null;
            }
            serverChannelController.init();
            serverChannelController.setOnStop(this::onChannelControllerStopped);
            categoryListController.goToChannel(channel);
            return serverChannelController;
        }
        return null;
    }

    private void onChannelControllerStopped() {
        categoryListController.setNoElementActive();
        subviewCleanup();
        serverChannelController = null;
    }

    private void subviewCleanup() {
        serverChannelContainer.getChildren().clear();
        channelNameLabel.setText("");
    }


    private Channel selectAndGetChannel(final HashMap<String, String> args) {
        final String serverId = args.get(":id");
        final String categoryId = args.get(":categoryId");
        final String channelId = args.get(":channelId");
        return selectAndGetChannel(serverId, categoryId, channelId);
    }

    private Channel selectAndGetChannel(final String serverId, final String categoryId, final String channelId) {
        Server server = editor.getServer(serverId);
        Category category = editor.getCategory(categoryId, server);
        Channel channel = editor.getChannel(channelId, category);
        if (Objects.isNull(channel)) {
            channel = editor.getChannel(channelId, server);
        }
        if (Objects.nonNull(selectedChannel)) {
            selectedChannel.listeners().removePropertyChangeListener(channelNameListener);
        }
        selectedChannel = channel;
        channelNameLabel.setText(selectedChannel.getName());
        channel.listeners().addPropertyChangeListener(Channel.PROPERTY_NAME, channelNameListener);

        return channel;
    }

    private void onServerNamePropertyChange(PropertyChangeEvent propertyChangeEvent) {
        final String newName = (String) propertyChangeEvent.getNewValue();
        Platform.runLater(() -> {
            serverName.setText(newName);
        });
    }

    private void onChannelNamePropertyChange(PropertyChangeEvent propertyChangeEvent) {
        final String newName = (String) propertyChangeEvent.getNewValue();
        Platform.runLater(() -> {
            channelNameLabel.setText(newName);
        });
    }

    private void onInviteUserClicked(ActionEvent actionEvent) {
        Parent invitesModalView = viewLoader.loadView(Views.INVITES_MODAL);
        InvitesModal invitesModal = invitesModalFactory.create(invitesModalView, model);
        invitesModal.show();
    }

    private void onEditServerClicked(ActionEvent actionEvent) {
        Parent serverSettingsModalView = viewLoader.loadView(Views.SERVER_SETTINGS_MODAL);
        boolean owner = false;
        if (Objects.nonNull(model.getOwner())) {
            owner = editor.getOrCreateAccord().getCurrentUser().getId().equals(model.getOwner().getId());
        }
        ServerSettingsModal serverSettingsModal = serverSettingsModalFactory.create(serverSettingsModalView, model, owner);
        serverSettingsModal.show();
    }

    private void onCreateCategoryClicked(ActionEvent actionEvent) {
        Parent createCategoryModalView = viewLoader.loadView(Views.CREATE_CATEGORY_MODAL);
        CreateCategoryModal createCategoryModal = categoryModalFactory.create(createCategoryModalView, model);
        createCategoryModal.show();
    }

    @Override
    public void stop() {
        if (Objects.nonNull(categoryListController)) {
            categoryListController.stop();
        }
        if (Objects.nonNull(serverChannelController)) {
            serverChannelController.stop();
        }
        if (Objects.nonNull(serverUserListController)) {
            serverUserListController.stop();
        }
        if (Objects.nonNull(selectedChannel)) {
            selectedChannel.listeners().removePropertyChangeListener(Channel.PROPERTY_NAME, channelNameListener);
        }

        settingsGearLabel.setOnMouseClicked(null);
        model.listeners().removePropertyChangeListener(Server.PROPERTY_NAME, serverNamePropertyChangeListener);

        for (MenuItem item : settingsContextMenu.getItems()) {
            item.setOnAction(null);
        }
        view.heightProperty().removeListener(viewHeightChangedListener);
        settingsGear.setOnMouseEntered(null);
        settingsGear.setOnMouseExited(null);
    }

    private void onViewHeightChanged(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
        serverScreenView.setPrefHeight(newValue.doubleValue());
    }

    @AssistedFactory
    public interface ServerScreenControllerFactory {
        ServerScreenController create(Parent view, Server server);
    }
}
