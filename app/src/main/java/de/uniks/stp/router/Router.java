package de.uniks.stp.router;

import de.uniks.stp.StageManager;
import de.uniks.stp.controller.ControllerInterface;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Router {

    private static final HashMap<String, Class<?>> routeMap;
    private static final ConcurrentHashMap<String, ControllerInterface> controllerCache = new ConcurrentHashMap<>();
    private static String currentRoute;
    private static RouteArgs currentArgs;

    static {
        routeMap = new RouteMap().getRoutes();
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

    private static Stack<RouteInfo> getRequirements(String route) {
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
                info.setSubControllerRoute(currentRelativeRoute.toString());
                currentRelativeRoute.setLength(0);
            }

            requirements.push(info.setCurrentControllerRoute(remainingRoute));
            info = new RouteInfo();

            if(controllerCache.containsKey(remainingRoute)) {
                break;
            }
        }
        return requirements;
    }

    public static void route(String route) {
        routeWithArgs(route, new RouteArgs());
    }

    public static void route(String route, RouteArgs args) {
        routeWithArgs(route, args);
    }

    public static void routeWithArgs(String route, RouteArgs args) {
        if(!routeMap.containsKey(route)) {
            throw new RuntimeException("Unknown route " + route);
        }

        if(controllerCache.containsKey(route)) {
            if(!args.compareTo(currentArgs)) {
                controllerCache.remove(route);
            }
            else {
                return;
            }
        }

        //check required args
        if(routeContainsArgs(route) && !checkRequiredArgs(route, args)) {
            throw new RuntimeException("Missing argument for route " + route);
        }

        //shutdown controllers that are not needed for the new route
        if(currentRoute != null) {
            String intersection = compareRoutes(route, currentRoute);
            shutdownControllers(intersection);
        }

        Stack<RouteInfo> requirements = getRequirements(route);
        int requirementCount = requirements.size();

        for(int i = 0; i < requirementCount; i++) {
            RouteInfo nextRoute = requirements.pop();

            if(isBaseRoute(nextRoute.getCurrentControllerRoute()) && !controllerCache.containsKey(nextRoute.getSubControllerRoute())) {
                StageManager.route(nextRoute);
                if(!controllerCache.containsKey(nextRoute.getSubControllerRoute())) {
                    throw new RuntimeException("Controller for route " + nextRoute.getSubControllerRoute() + " was not added to cache by StageManager");
                }
            }
            else {
                //get controller from cache and use its route method
                ControllerInterface currentController = controllerCache.get(nextRoute.getCurrentControllerRoute());
                currentController.route(nextRoute, args);

                if(!controllerCache.containsKey(nextRoute.getCurrentControllerRoute())) {
                    throw new RuntimeException("Controller for route " + nextRoute.getCurrentControllerRoute() + "was not added to cache by " + currentController.getClass().getName());
                }
            }
        }
        currentRoute = route;
        currentArgs = args;
    }

    public static boolean checkRequiredArgs(String route, RouteArgs args) {
        //parse args from route
        String[] splitRoute = route.split("/");
        for(String split : splitRoute) {
            if(split.contains(":") && !args.getArguments().containsKey(split)) {
                return false;
            }
        }
        return true;
    }

    public static boolean routeContainsArgs(String route) {
        return route.contains(":");
    }

    /**
     *
     * @param route the given route string as full route
     * @return a boolean representing whether a route is a route that must be handled by the stageManager or not
     */
    public static boolean isBaseRoute(String route) {
        return route.isEmpty();
    }

    public static void addToControllerCache(String routeName, ControllerInterface controller) {
        controllerCache.putIfAbsent(routeName, controller);
    }
}
