package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import de.uniks.stp.router.Router;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.Objects;

import static de.uniks.stp.view.Views.SERVER_SCREEN;

@Route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER)
public class ServerScreenController implements ControllerInterface {

    private static final String SERVER_NAME_LABEL_ID = "#server-name-label";
    private static final String SERVER_CHANNEL_OVERVIEW = "#server-channel-overview";
    private static final String SERVER_CHAT_CONTAINER = "#server-chat-container";
    private static final String SERVER_USER_LIST_CONTAINER = "#server-user-list-container";
    private AnchorPane view;
    private FlowPane serverScreenView;
    private final Editor editor;
    private final Server model;
    private Label serverNameLabel;
    private VBox serverChannelOverview;
    private ServerCategoryListController categoryListController;
    private FlowPane serverChatContainer;
    private ServerChatController serverChatController;
    private ServerUserListController serverUserListController;
    private FlowPane serverUserListContainer;

    public ServerScreenController(Parent view, Editor editor, Server model) {
        this.view = (AnchorPane)view;
        this.editor = editor;
        this.model = model;
    }

    @Override
    public void init() {
       serverScreenView = (FlowPane) ViewLoader.loadView(SERVER_SCREEN);
       serverChannelOverview = (VBox) serverScreenView.lookup(SERVER_CHANNEL_OVERVIEW);
       serverChatContainer = (FlowPane) serverScreenView.lookup(SERVER_CHAT_CONTAINER);
       serverUserListContainer = (FlowPane) serverScreenView.lookup(SERVER_USER_LIST_CONTAINER);
       view.getChildren().add(serverScreenView);
       serverNameLabel = (Label)view.lookup(SERVER_NAME_LABEL_ID);
       serverNameLabel.setText(model.getName());

       categoryListController = new ServerCategoryListController(serverChannelOverview, editor, model);
       categoryListController.init();

       serverUserListController = new ServerUserListController(serverUserListContainer, editor, model);
       serverUserListController.init();
    }

    @Override
    public void route(RouteInfo routeInfo, RouteArgs args) {
        if(routeInfo.getSubControllerRoute().equals(Constants.ROUTE_CHANNEL)) {
            final String serverId = args.getArguments().get(":id");
            final String categoryId = args.getArguments().get(":categoryId");
            final String channelId = args.getArguments().get(":channelId");
            final Channel channel = getChannel(serverId, categoryId, channelId);
            serverChatController = new ServerChatController(serverChatContainer, editor, channel);
            serverChatController.init();
            Router.addToControllerCache(routeInfo.getFullRoute(), serverChatController);
        }
    }

    private Channel getChannel(final String serverId, final String categoryId, final String channelId) {
        Server server = editor.getServer(serverId);
        Category category = editor.getCategory(categoryId, server);
        return editor.getChannel(channelId, category);
    }

    @Override
    public void stop() {
        if(Objects.nonNull(categoryListController)) {
            categoryListController.stop();
        }
        if(Objects.nonNull(serverChatController)) {
            serverChatController.stop();
        }
        if (Objects.nonNull(serverUserListController)) {
            serverUserListController.stop();
        }
    }
}
