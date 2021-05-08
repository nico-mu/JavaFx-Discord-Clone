package de.uniks.stp.router;

public class RouteInfo {

    private String currentControllerRoute;
    private String subControllerRoute;

    public String getCurrentControllerRoute() {
        return currentControllerRoute;
    }

    public RouteInfo setCurrentControllerRoute(String currentControllerRoute) {
        this.currentControllerRoute = currentControllerRoute;
        return this;
    }

    public String getSubControllerRoute() {
        return subControllerRoute;
    }

    public RouteInfo setSubControllerRoute(String subControllerRoute) {
        this.subControllerRoute = subControllerRoute;
        return this;
    }

    public String getFullRoute() {
        return currentControllerRoute + subControllerRoute;
    }
}
