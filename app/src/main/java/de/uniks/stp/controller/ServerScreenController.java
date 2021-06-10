package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.emote.EmoteRenderer;
import de.uniks.stp.modal.CreateCategoryModal;
import de.uniks.stp.modal.InvitesModal;
import de.uniks.stp.modal.ServerSettingsModal;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.notification.NotificationService;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import de.uniks.stp.router.Router;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.Objects;

import static de.uniks.stp.view.Views.SERVER_SCREEN;

@Route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER)
public class ServerScreenController implements ControllerInterface {

    private static final String SERVER_NAME_ID = "#server-name";
    private static final String SERVER_CHANNEL_OVERVIEW = "#server-channel-overview";
    private static final String SERVER_CHAT_CONTAINER = "#server-chat-container";
    private static final String SERVER_USER_LIST_CONTAINER = "#server-user-list-container";
    private static final String SETTINGS_LABEL = "#settings-label";
    private AnchorPane view;
    private FlowPane serverScreenView;
    private final Editor editor;
    private final Server model;
    private TextFlow serverName;
    private VBox serverChannelOverview;
    private ServerCategoryListController categoryListController;
    private FlowPane serverChatContainer;
    private ServerChatController serverChatController;
    private ServerUserListController serverUserListController;
    private FlowPane serverUserListContainer;
    private Label settingsGearLabel;
    private final EmoteRenderer renderer = new EmoteRenderer();

    private ContextMenu settingsContextMenu;
    PropertyChangeListener serverNamePropertyChangeListener = this::onServerNamePropertyChange;

    public ServerScreenController(Parent view, Editor editor, Server model) {
        this.view = (AnchorPane) view;
        this.editor = editor;
        this.model = model;
    }

    @Override
    public void init() {
        serverScreenView = (FlowPane) ViewLoader.loadView(SERVER_SCREEN);
        serverChannelOverview = (VBox) serverScreenView.lookup(SERVER_CHANNEL_OVERVIEW);
        serverChatContainer = (FlowPane) serverScreenView.lookup(SERVER_CHAT_CONTAINER);
        serverUserListContainer = (FlowPane) serverScreenView.lookup(SERVER_USER_LIST_CONTAINER);
        settingsGearLabel = (Label) serverScreenView.lookup(SETTINGS_LABEL);
        settingsContextMenu = settingsGearLabel.getContextMenu();

        view.getChildren().add(serverScreenView);
        serverName = (TextFlow) view.lookup(SERVER_NAME_ID);
        renderer.setSize(20).setScalingFactor(2);
        renderer.setEmoteRenderStrategy(renderer::imageEmoteRenderStrategy);
        serverName.getChildren().clear();
        renderer.renderInto(model.getName(), serverName);

        ObservableList<MenuItem> items = settingsContextMenu.getItems();
        items.get(0).setOnAction(this::onInviteUserClicked);
        items.get(1).setOnAction(this::onEditServerClicked);
        items.get(2).setOnAction(this::onCreateCategoryClicked);

        settingsGearLabel.setOnMouseClicked(e -> settingsContextMenu.show(settingsGearLabel, Side.BOTTOM, 0, 0));

        categoryListController = new ServerCategoryListController(serverChannelOverview, editor, model);
        categoryListController.init();

        serverUserListController = new ServerUserListController(serverUserListContainer, editor, model);
        serverUserListController.init();

        model.listeners().addPropertyChangeListener(Server.PROPERTY_NAME, serverNamePropertyChangeListener);
    }

    @Override
    public void route(RouteInfo routeInfo, RouteArgs args) {
        if (routeInfo.getSubControllerRoute().equals(Constants.ROUTE_CHANNEL)) {
            final String serverId = args.getArguments().get(":id");
            final String categoryId = args.getArguments().get(":categoryId");
            final String channelId = args.getArguments().get(":channelId");
            final Channel channel = getChannel(serverId, categoryId, channelId);
            NotificationService.consume(channel);
            serverChatController = new ServerChatController(serverChatContainer, editor, channel);
            serverChatController.init();
            Router.addToControllerCache(routeInfo.getFullRoute(), serverChatController);
        }
    }

    private Channel getChannel(final String serverId, final String categoryId, final String channelId) {
        Server server = editor.getServer(serverId);
        Category category = editor.getCategory(categoryId, server);
        Channel channel = editor.getChannel(channelId, category);
        if (Objects.isNull(channel)) {
            channel = editor.getChannel(channelId, server);
        }
        return channel;
    }

    private void onServerNamePropertyChange(PropertyChangeEvent propertyChangeEvent) {
        Platform.runLater(()-> {
            serverName.getChildren().clear();
            renderer.renderInto(model.getName(), serverName);
        });
    }

    private void onInviteUserClicked(ActionEvent actionEvent) {
        Parent invitesModalView = ViewLoader.loadView(Views.INVITES_MODAL);
        InvitesModal invitesModal = new InvitesModal(invitesModalView, model, editor);
        invitesModal.show();
    }

    private void onEditServerClicked(ActionEvent actionEvent) {
        Parent serverSettingsModalView = ViewLoader.loadView(Views.SERVER_SETTINGS_MODAL);
        ServerSettingsModal serverSettingsModal = new ServerSettingsModal(serverSettingsModalView, model);
        serverSettingsModal.show();
    }

    private void onCreateCategoryClicked(ActionEvent actionEvent) {
        Parent createCategoryModalView = ViewLoader.loadView(Views.CREATE_CATEGORY_MODAL);
        CreateCategoryModal createCategoryModal = new CreateCategoryModal(createCategoryModalView, model, editor);
        createCategoryModal.show();
    }

    @Override
    public void stop() {
        if (Objects.nonNull(categoryListController)) {
            categoryListController.stop();
        }
        if (Objects.nonNull(serverChatController)) {
            serverChatController.stop();
        }
        if (Objects.nonNull(serverUserListController)) {
            serverUserListController.stop();
        }
        settingsGearLabel.setOnMouseClicked(null);
        model.listeners().removePropertyChangeListener(Server.PROPERTY_NAME, serverNamePropertyChangeListener);

        for(MenuItem item: settingsContextMenu.getItems()){
            item.setOnAction(null);
        }
    }
}
