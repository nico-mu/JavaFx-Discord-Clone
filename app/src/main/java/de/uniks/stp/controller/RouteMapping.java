package de.uniks.stp.controller;

import java.util.HashMap;

public class RouteMapping {
    public HashMap<String, Class<?>> getRoutes() {
        HashMap<String, Class<?>> routes = new HashMap<>();
        routes.put("/main", MainScreenController.class);
        routes.put("/login", LoginScreenController.class);
        routes.put("/main/home", HomeScreenController.class);
        return routes;
    }
}
