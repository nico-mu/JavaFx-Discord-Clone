package de.uniks.stp.router;

import java.util.HashMap;
import java.util.Objects;

public class RouteArgs {

    private final HashMap<String, String> argMap = new HashMap<>();

    public RouteArgs addArgument(String key, String value) {
        argMap.put(key, value);
        return this;
    }

    public RouteArgs removeArgument(String key) {
        argMap.remove(key);
        return this;
    }

    public HashMap<String, String> getArguments() {
        return argMap;
    }

    public boolean compareTo(RouteArgs other) {
        Objects.requireNonNull(other);
        HashMap<String, String> otherMap = other.getArguments();
        for(String argName : argMap.keySet()) {
            if (!otherMap.containsKey(argName) || !otherMap.get(argName).equals(argMap.get(argName))) {
                return false;
            }
        }
        return true;
    }
}
