package de.uniks.stp.emote;

public enum EmoteMapping {
    grinning_face("\uD83D\uDE00"),
    grinning_face_with_big_eyes("\uD83D\uDE03");

    public final String unicode;

    EmoteMapping(String unicode) {
        this.unicode = unicode;
    }
}
