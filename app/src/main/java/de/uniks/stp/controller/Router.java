package de.uniks.stp.controller;

import de.uniks.stp.StageManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class Router {

    private static final HashMap<String, Class<?>> routeMap;
    private static HashMap<String, ControllerInterface> controllerCache = new HashMap<>();
    private static String currentRoute;


    static {
        routeMap = new RouteMapping().getRoutes();
    }

    public static String compareRoutes(String newRoute, String oldRoute) {
        StringBuilder intersection = new StringBuilder();
        int compareLength = Math.min(oldRoute.length(), newRoute.length());

        for (int i = 0; i < compareLength; i++) {
            if(newRoute.charAt(i) == oldRoute.charAt(i)) {
                intersection.append(newRoute.charAt(i));
            }
            else {
                break;
            }
        }
        return intersection.toString();
    }

    public static void shutdownControllers(String neededRoute) {
        List<String> keysToRemove = new ArrayList<>();
        for (String routeName : controllerCache.keySet()) {
            if(routeName.contains(neededRoute) && routeName.length() > neededRoute.length()) {
                keysToRemove.add(routeName);
            }
        }

        if(keysToRemove.size() > 0) {
            String shortestRouteName = keysToRemove.get(0);
            ControllerInterface shortestRouteController = controllerCache.get(shortestRouteName);

            for(String keyToRemove : keysToRemove) {
                if(keyToRemove.length() < shortestRouteName.length()) {
                    shortestRouteName = keyToRemove;
                    shortestRouteController = controllerCache.get(shortestRouteName);
                }
                controllerCache.remove(keyToRemove);
            }

            shortestRouteController.stop();
        }
    }

    public static void route(String route) {
        if(!routeMap.containsKey(route)) {
            throw new RuntimeException("Unknown route " + route);
        }

        if(controllerCache.containsKey(route)) {
            return;
        }

        if(currentRoute != null) {
            String intersection = compareRoutes(route, currentRoute);
            shutdownControllers(intersection);
        }

        Stack<RouteInfo> requirements = new Stack<>();
        String remainingRoute = route;
        String relativeRoutePart;
        StringBuilder currentRelativeRoute = new StringBuilder();
        RouteInfo info = new RouteInfo();

        while(remainingRoute.length() != 0) {

            int index = remainingRoute.lastIndexOf('/');
            if(index == -1) {
                index = 0;
            }
            relativeRoutePart = remainingRoute.substring(index);
            remainingRoute = remainingRoute.substring(0, index);
            currentRelativeRoute.insert(0, relativeRoutePart);

            if(!routeMap.containsKey(remainingRoute) && !remainingRoute.isEmpty()) {
                continue;
            }
            else {
                //add new Route Info Object
                info.setSubroute(currentRelativeRoute.toString());
                currentRelativeRoute.setLength(0);
            }

            if(controllerCache.containsKey(remainingRoute)) {
                break;
            }

            requirements.push(info.setFullRoute(remainingRoute));
            info = new RouteInfo();
        }

        int requirementCount = requirements.size();

        for(int i = 0; i < requirementCount; i++) {
            RouteInfo nextRoute = requirements.pop();

            if(isBaseRoute(nextRoute.getFullRoute()) && !controllerCache.containsKey(nextRoute.getSubroute())) {
                StageManager.route(nextRoute);
                if(!controllerCache.containsKey(nextRoute.getSubroute())) {
                    throw new RuntimeException("Controller for route " + nextRoute.getSubroute() + " was not added to cache by StageManager");
                }
            }
            else {
                //get controller from cache and use its route method
                ControllerInterface currentController = controllerCache.get(nextRoute.getFullRoute());
                currentController.route(nextRoute);

                if(!controllerCache.containsKey(nextRoute.getFullRoute())) {
                    throw new RuntimeException("Controller for route " + nextRoute.getFullRoute() + "was not added to cache by " + currentController.getClass().getName());
                }
            }
        }
        currentRoute = route;
    }

    /**
     * by definition base routes can't handle parameters
     * -> '/main' as base route is allowed, '/main/:id' is not allowed
     *
     * @param route the given route string as full route
     * @return a boolean representing whether a route is a route that must be handled by the stageManager or not
     */
    public static boolean isBaseRoute(String route) {
        return route.isEmpty();
    }

    public static void addToControllerCache(String routeName, ControllerInterface controller) {
        controllerCache.put(routeName, controller);
    }
}
