package de.uniks.stp.network.integration;

public enum Integrations {

    SPOTIFY("spotify"),
    STEAM("steam"),
    GITHUB("github"),
    CLUB_PENGUIN("club_penguin");

    public final String key;

    Integrations(final String key) {
        this.key = key;
    }
}
