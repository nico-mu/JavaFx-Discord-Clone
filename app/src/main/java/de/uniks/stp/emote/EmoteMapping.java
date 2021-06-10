package de.uniks.stp.emote;

// Image emotes from https://openmoji.org/library/ or https://github.com/twitter/twemoji/tree/gh-pages/36x36
// Emotes from https://www.unicode.org/emoji/charts/full-emoji-list.html
public enum EmoteMapping {
    grinning_face("\uD83D\uDE00"),
    grinning_face_with_big_eyes("\uD83D\uDE03");

    public final String unicode;

    EmoteMapping(String unicode) {
        this.unicode = unicode;
    }
}
