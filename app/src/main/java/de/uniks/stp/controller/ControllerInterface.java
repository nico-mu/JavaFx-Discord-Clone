package de.uniks.stp.controller;

import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.RouteInfo;

public interface ControllerInterface {
    void init();

    default void route(RouteInfo routeInfo, RouteArgs args) {

    }

    void stop();
}
