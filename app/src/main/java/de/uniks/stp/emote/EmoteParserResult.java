package de.uniks.stp.emote;

public class EmoteParserResult {
    private int startIndex;
    private int endIndex;
    private String emoteName;

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public String getEmoteName() {
        return emoteName;
    }

    public EmoteParserResult setStartIndex(int startIndex) {
        this.startIndex = startIndex;
        return this;
    }

    public EmoteParserResult setEndIndex(int endIndex) {
        this.endIndex = endIndex;
        return this;
    }

    public EmoteParserResult setEmoteName(String emoteName) {
        this.emoteName = emoteName;
        return this;
    }
}
