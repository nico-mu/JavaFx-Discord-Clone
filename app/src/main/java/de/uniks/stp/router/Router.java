package de.uniks.stp.router;

import de.uniks.stp.controller.AppController;
import de.uniks.stp.controller.ControllerInterface;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Router {

    public static final String FORCE_RELOAD = "force-reload";
    private final HashMap<String, Class<?>> routeMap;
    private final ConcurrentHashMap<String, ControllerInterface> controllerCache;
    private final AppController appController;
    private String currentRoute;
    private RouteArgs currentArgs;

    public Router(AppController appController) {
        this.appController = appController;
        routeMap = new RouteMap().getRoutes();
        currentRoute = null;
        currentArgs = new RouteArgs();
        controllerCache = new ConcurrentHashMap<>();
    }

    public String getCurrentRoute() {
        return currentRoute;
    }

    public HashMap<String, String> getCurrentArgs() {
        return currentArgs.getArguments();
    }

    private String compareRoutes(String newRoute, String oldRoute) {
        StringBuilder intersection = new StringBuilder();
        int compareLength = Math.min(oldRoute.length(), newRoute.length());

        for (int i = 0; i < compareLength; i++) {
            if (newRoute.charAt(i) == oldRoute.charAt(i)) {
                intersection.append(newRoute.charAt(i));
            } else {
                break;
            }
        }
        return intersection.toString();
    }

    private String getRouteStringWithArgs(String routeString, RouteArgs args) {
        HashMap<String, String> argMap = args.getArguments();
        String result = routeString;
        for (String argName : argMap.keySet()) {
            result = result.replace(argName, argMap.get(argName));
        }
        return result;
    }

    private boolean compareRoutesWithArgs(String newRoute, String oldRoute, RouteArgs newArgs, RouteArgs oldArgs) {
        return getRouteStringWithArgs(newRoute, newArgs).equals(getRouteStringWithArgs(oldRoute, oldArgs));
    }

    private void shutdownControllers(String neededRoute, RouteArgs args) {
        List<String> keysToRemove = new ArrayList<>();
        for (String routeName : controllerCache.keySet()) {
            if (routeName.contains(neededRoute) && routeName.length() > neededRoute.length()
                || routeName.equals(neededRoute) && routeContainsArgs(neededRoute)
                && !compareRoutesWithArgs(neededRoute, routeName, args, currentArgs)) {
                keysToRemove.add(routeName);
            }
        }

        if (keysToRemove.size() > 0) {
            String shortestRouteName = keysToRemove.get(0);
            ControllerInterface shortestRouteController = controllerCache.get(shortestRouteName);

            for (String keyToRemove : keysToRemove) {
                if (keyToRemove.length() < shortestRouteName.length()) {
                    shortestRouteName = keyToRemove;
                    shortestRouteController = controllerCache.get(shortestRouteName);
                }
                controllerCache.remove(keyToRemove);
            }

            shortestRouteController.stop();
        }
    }

    public void forceReload() {
        shutdownControllers("", new RouteArgs());
        route(currentRoute, currentArgs.addArgument(FORCE_RELOAD, FORCE_RELOAD));
    }


    private Stack<RouteInfo> getRequirements(String route) {
        Stack<RouteInfo> requirements = new Stack<>();
        String remainingRoute = route;
        String relativeRoutePart;
        StringBuilder currentRelativeRoute = new StringBuilder();
        RouteInfo info = new RouteInfo();

        while (remainingRoute.length() != 0 && !controllerCache.containsKey(remainingRoute)) {

            int index = remainingRoute.lastIndexOf('/');
            if (index == -1) {
                index = 0;
            }
            relativeRoutePart = remainingRoute.substring(index);
            remainingRoute = remainingRoute.substring(0, index);
            currentRelativeRoute.insert(0, relativeRoutePart);

            if (!routeMap.containsKey(remainingRoute) && !remainingRoute.isEmpty()) {
                continue;
            } else {
                //add new Route Info Object
                info.setSubControllerRoute(currentRelativeRoute.toString());
                currentRelativeRoute.setLength(0);
            }

            requirements.push(info.setCurrentControllerRoute(remainingRoute));
            info = new RouteInfo();
        }
        return requirements;
    }

    public void route(String route) {
        routeWithArgs(route, new RouteArgs());
    }

    public void route(String route, RouteArgs args) {
        routeWithArgs(route, args);
    }

    private void routeWithArgs(String route, RouteArgs args) {
        if (!routeMap.containsKey(route)) {
            throw new RuntimeException("Unknown route " + route);
        }

        //check required args
        if (routeContainsArgs(route) && !checkRequiredArgs(route, args)) {
            throw new RuntimeException("Missing argument for route " + route);
        }

        //shutdown controllers that are not needed for the new route
        if (currentRoute != null) {
            String intersection = compareRoutes(route, currentRoute);
            shutdownControllers(intersection, args);
        }

        currentRoute = route;
        currentArgs = args;
        Stack<RouteInfo> requirements = getRequirements(route);
        int requirementCount = requirements.size();

        for (int i = 0; i < requirementCount; i++) {
            RouteInfo nextRoute = requirements.pop();
            ControllerInterface newController;

            if (isBaseRoute(nextRoute.getCurrentControllerRoute()) && !controllerCache.containsKey(nextRoute.getSubControllerRoute())) {
                newController = appController.route(nextRoute, args);
            } else {
                //get controller from cache and use its route method
                ControllerInterface currentController = controllerCache.get(nextRoute.getCurrentControllerRoute());
                newController = currentController.route(nextRoute, args);
            }

            if(Objects.isNull(newController)) {
                throw new RuntimeException("Route " + nextRoute.getFullRoute() + " does not exist");
            }
            else {
                addToControllerCache(nextRoute.getFullRoute(), newController);
            }
        }
    }

    private boolean checkRequiredArgs(String route, RouteArgs args) {
        //parse args from route
        String[] splitRoute = route.split("/");
        for (String split : splitRoute) {
            if (split.contains(":") && !args.getArguments().containsKey(split)) {
                return false;
            }
        }
        return true;
    }

    private boolean routeContainsArgs(String route) {
        return route.contains(":");
    }

    /**
     * @param route the given route string as full route
     * @return a boolean representing whether a route is a route that must be handled by the stageManager or not
     */
    public static boolean isBaseRoute(String route) {
        return route.isEmpty();
    }

    private void addToControllerCache(String routeName, ControllerInterface controller) {
        controllerCache.putIfAbsent(routeName, controller);
    }
}
