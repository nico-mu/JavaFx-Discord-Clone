package de.uniks.stp.controller;

public interface ControllerInterface {
    void init();

    void route(RouteInfo routeInfo);

    void stop();
}
