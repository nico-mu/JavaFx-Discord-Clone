package de.uniks.stp.emote;

// Emotes from https://www.unicode.org/emoji/charts/full-emoji-list.html
public enum EmoteMapping {
    grinning_face("\uD83D\uDE00"),
    grinning_face_with_smiling_eyes("\uD83D\uDE04"),
    winking_face("\uD83D\uDE09"),
    thinking_face("\uD83E\uDD14"),
    neutral_face("\uD83D\uDE10"),
    smirking_face("\uD83D\uDE0F"),
    smiling_face_with_sunglasses("\uD83D\uDE0E"),
    grinning_face_with_big_eyes("\uD83D\uDE03");

    public final String unicode;

    EmoteMapping(String unicode) {
        this.unicode = unicode;
    }
}
