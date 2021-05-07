package de.uniks.stp.controller;

import java.util.HashMap;
import de.uniks.stp.controller.MainScreenController;
import de.uniks.stp.controller.LoginScreenController;
import de.uniks.stp.controller.HomeScreenController;

public class RouteMapping {
    public HashMap<String, Class<?>> getRoutes() {
        HashMap<String, Class<?>> routes = new HashMap<>();
        routes.put("/main", MainScreenController.class);
        routes.put("/login", LoginScreenController.class);
        routes.put("/main/home", HomeScreenController.class);
        return routes;
    }
}