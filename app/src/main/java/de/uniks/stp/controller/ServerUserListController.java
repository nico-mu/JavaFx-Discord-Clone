package de.uniks.stp.controller;

import de.uniks.stp.Editor;
import de.uniks.stp.component.ServerUserListEntry;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class ServerUserListController implements ControllerInterface {
    private static String ONLINE_USER_LIST_ID = "#online-user-list";
    private static String OFFLINE_USER_LIST_ID = "#offline-user-list";

    private Parent view;
    private Editor editor;
    private Server model;
    private VBox onlineUserList;
    private VBox offlineUserList;

    public ServerUserListController(Parent view, Editor editor, Server model) {
        this.view = view;
        this.editor = editor;
        this.model = model;
    }

    public void init() {
        onlineUserList = (VBox) view.lookup(ONLINE_USER_LIST_ID);
        offlineUserList = (VBox) view.lookup(OFFLINE_USER_LIST_ID);

        for (User user : model.getUsers()) {
            if (user.isStatus()) {
                onlineUserList.getChildren().add(new ServerUserListEntry(user));
                return;
            }
            offlineUserList.getChildren().add(new ServerUserListEntry(user));
        }

    }

    public void stop() {

    }
}
