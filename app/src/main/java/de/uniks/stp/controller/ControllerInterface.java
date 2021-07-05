package de.uniks.stp.controller;

import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;

public interface ControllerInterface {
    void init();

    default ControllerInterface route(RouteInfo routeInfo, RouteArgs args) {
        return null;
    }

    void stop();
}
