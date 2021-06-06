package de.uniks.stp.emote;

import de.uniks.stp.component.EmotePicker;
import de.uniks.stp.util.Triple;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class EmoteParser {
    private static final Logger log = LoggerFactory.getLogger(EmoteParser.class);
    private static final Map<String, String> emoteMapping;

    static {
        emoteMapping = createEmoteMapping();
    }

    public static Map<String, String> getEmoteMapping() {
        return emoteMapping;
    }

    public static boolean isEmoteName(String emoteName) {
        return emoteMapping.containsKey(emoteName);
    }

    public static String getEmoteByName(String emoteName) {
        return getEmoteMapping().get(emoteName);
    }

    public static List<String> getAllEmoteNames() {
        return new LinkedList<>(emoteMapping.keySet());
    }

    public static LinkedList<Triple<Integer, Integer, String>> parse(String input) {
        LinkedList<Triple<Integer, Integer, String>> parsingResult = new LinkedList<>();

        int index = 0;
        Integer lastEmoteStart = null;
        boolean isCollectingEmoteName = false;
        StringBuilder emoteNameBuffer = new StringBuilder();

        while (index < input.length()) {
            char character = input.charAt(index);

            if (character == ':') {
                isCollectingEmoteName  = !isCollectingEmoteName;
                if (isCollectingEmoteName) {
                    lastEmoteStart = index;
                }
            }

            if (!isCollectingEmoteName && isEmoteName(emoteNameBuffer.toString()) && Objects.nonNull(lastEmoteStart)) {
                parsingResult.add(new Triple<>(lastEmoteStart, index, emoteNameBuffer.toString()));
                emoteNameBuffer = new StringBuilder();
                lastEmoteStart = null;
            } else if (!isCollectingEmoteName && !isEmoteName(emoteNameBuffer.toString())) {
                emoteNameBuffer = new StringBuilder();
                lastEmoteStart = null;
            } else if (isCollectingEmoteName && character != ':') {
                emoteNameBuffer.append(character);
            }

            index++;
        }
        // Clear emote name buffer
        if (isEmoteName(emoteNameBuffer.toString()) && Objects.nonNull(lastEmoteStart)) {
            parsingResult.add(new Triple<>(lastEmoteStart, index, emoteNameBuffer.toString()));
        }

        return parsingResult;
    }

    public static String convertTextWithUnicodeToNames(String text) {
        for (String emoteName : getAllEmoteNames()) {
            text = text.replace(getEmoteByName(emoteName), ":" + emoteName + ":");
        }

        return text;
    }

    public static String toUnicodeString(String input) {
        StringBuilder renderResult = new StringBuilder();
        LinkedList<Triple<Integer, Integer, String>> parsingResult = EmoteParser.parse(input);
        int from = 0;

        for (Triple<Integer, Integer, String> emoteInfo : parsingResult) {
            if (input.substring(from, emoteInfo.getFirst()).length() > 0) {
                renderResult.append(input.substring(from, emoteInfo.getFirst()));
            }
            renderResult.append(getEmoteByName(emoteInfo.getThird()));
            from = emoteInfo.getSecond() + 1;
        }
        if (from < input.length()) {
            renderResult.append(input.substring(from));
        }

        return renderResult.toString();
    }

    private static Map<String, String> createEmoteMapping() {
        Map<String, String> map = new HashMap<>();

        for (EmoteMapping entry : EmoteMapping.values()) {
            map.put(entry.toString(), entry.unicode);
        };

        return map;
    }
}
