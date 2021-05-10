package de.uniks.stp.controller;


import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.router.Route;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;
import javafx.scene.Parent;

@Route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + "/chat/:userId")
public class PrivateChatController implements ControllerInterface {

    public PrivateChatController(Parent view, Editor editor) {

    }

    @Override
    public void init() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void route(RouteInfo routeInfo, RouteArgs args) {

    }
}
