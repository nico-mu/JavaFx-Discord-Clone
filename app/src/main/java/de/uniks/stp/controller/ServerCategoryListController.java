package de.uniks.stp.controller;

import de.uniks.stp.Editor;
import de.uniks.stp.component.ServerCategoryElement;
import de.uniks.stp.component.ServerCategoryList;
import de.uniks.stp.component.ServerChannelElement;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Channel;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

public class ServerCategoryListController implements ControllerInterface {

    private final Parent view;
    private final Editor editor;
    private final ServerCategoryList serverCategoryList;
    private final RestClient restClient;
    private VBox vBox;

    public ServerCategoryListController(Parent view, Editor editor) {
        this.view = view;
        this.editor = editor;
        this.serverCategoryList = new ServerCategoryList();
        this.restClient = new RestClient();
    }

    @Override
    public void init() {
        vBox = (VBox)view;
        vBox.getChildren().add(serverCategoryList);
        serverCategoryList.setPrefHeight(vBox.getPrefHeight());

        for (int j= 0; j < 10; j++) {
            ServerCategoryElement element = new ServerCategoryElement(new Category().setName("cat1"));
            ServerCategoryElement element2 = new ServerCategoryElement(new Category().setName("cat2"));

            for (int i = 0; i < 5; i++) {
                element.addChannelElement(new ServerChannelElement(new Channel().setName("test " + i)));
                element2.addChannelElement(new ServerChannelElement(new Channel().setName("test " + i)));
            }

            serverCategoryList.addElement(element);
            serverCategoryList.addElement(element2);
        }
    }

    @Override
    public void route(RouteInfo routeInfo, RouteArgs args) {
        //no subroutes
    }

    @Override
    public void stop() {

    }
}
