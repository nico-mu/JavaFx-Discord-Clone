package de.uniks.stp.controller;

public class RouteInfo {

    private String fullRoute;
    private String subroute;

    public String getFullRoute() {
        return fullRoute;
    }

    public RouteInfo setFullRoute(String fullRoute) {
        this.fullRoute = fullRoute;
        return this;
    }

    public String getSubroute() {
        return subroute;
    }

    public RouteInfo setSubroute(String subroute) {
        this.subroute = subroute;
        return this;
    }
}
