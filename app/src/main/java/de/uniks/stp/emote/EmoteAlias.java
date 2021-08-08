package de.uniks.stp.emote;

public enum EmoteAlias {
    SMILING_FACE_WITH_OPEN_MOUTH(":D");

    private final String alias;
    EmoteAlias(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }
}
