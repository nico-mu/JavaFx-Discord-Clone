package de.uniks.stp.router;

import java.util.Objects;

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
        if (Objects.nonNull(key) && Objects.nonNull(value)) {
            return key.equals(other.getKey()) && value.equals(other.getValue());
        }
        return Objects.isNull(key) && Objects.isNull(value) && Objects.isNull(other.getKey()) && Objects.isNull(other.getValue());
    }
}
