package de.uniks.stp.controller;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.component.ChatView;
import de.uniks.stp.model.Message;
import de.uniks.stp.router.Route;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import de.uniks.stp.router.Router;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Date;

@Route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME)
public class HomeScreenController implements ControllerInterface {

    private final AnchorPane view;
    private final Editor editor;
    private ChatView chatView;
    private AnchorPane container;

    HomeScreenController(Parent view, Editor editor) {
        this.view = (AnchorPane) view;
        this.editor = editor;
        this.chatView = new ChatView();
    }

    @Override
    public void init() {
        container = (AnchorPane) view.lookup("#subview-container");
        container.getChildren().add(chatView.getComponent());

        chatView.onMessageSubmit((message) -> {
            chatView.appendMessage(new Message()
                .setMessage(message)
                .setSender(editor.getOrCreateAccord().getCurrentUser())
                .setTimestamp(new Date().getTime()));

            // send message to the server

        });
    }

    @Override
    public void route(RouteInfo routeInfo, RouteArgs args) {
        //no subroutes
    }

    @Override
    public void stop() {

    }
}
