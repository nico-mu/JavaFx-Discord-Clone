package de.uniks.stp.router;

public class RouteArgs {

    private String key;
    private String value;

    public String getKey() {
        return key;
    }

    public RouteArgs setKey(String key) {
        this.key = key;
        return this;
    }

    public String getValue() {
        return value;
    }

    public RouteArgs setValue(String value) {
        this.value = value;
        return this;
    }

    public boolean compareTo(RouteArgs other) {
        return key.equals(other.getKey()) && value.equals(other.getValue());
    }
}
