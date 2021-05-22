package de.uniks.stp.view;

import java.util.Objects;

public enum Languages {
    GERMAN("de"), ENGLISH("en");

    public final String key;

    Languages(final String key) {
        this.key = key;
    }

    public static Languages getDefault() {
        return GERMAN;
    }

    public static Languages fromKeyOrDefault(String key) {
        if (Objects.nonNull(key)) {
            for (final Languages languages : values()) {
                if (key.equals(languages.key)) {
                    return languages;
                }
            }
        }
        return getDefault();
    }
}
