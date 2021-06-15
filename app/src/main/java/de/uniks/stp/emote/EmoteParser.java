package de.uniks.stp.emote;

import de.uniks.stp.ViewLoader;
import de.uniks.stp.network.WebSocketService;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class EmoteParser {
    private static final Logger log = LoggerFactory.getLogger(EmoteParser.class);
    private static final Map<String, String> emoteMapping = new HashMap<>();;

    static {
        InputStream inputStream = Objects.requireNonNull(ViewLoader.class.getResourceAsStream("emote/emote-list.json"));
        String text = new BufferedReader(
            new InputStreamReader(inputStream, StandardCharsets.UTF_8))
        .lines()
            .collect(Collectors.joining("\n"));
        JSONObject jsonObject = new JSONObject(text);
        
        addEmotesFromJSONArray(jsonObject.getJSONArray("Smileys & People"));
        addEmotesFromJSONArray(jsonObject.getJSONArray("Objects"));
        addEmotesFromJSONArray(jsonObject.getJSONArray("Animals & Nature"));
        addEmotesFromJSONArray(jsonObject.getJSONArray("Flags"));
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

    public static LinkedList<EmoteParserResult> parse(String input) {
        LinkedList<EmoteParserResult> parsingResult = new LinkedList<>();

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
                parsingResult.add(new EmoteParserResult()
                    .setStartIndex(lastEmoteStart)
                    .setEndIndex(index)
                    .setEmoteName(emoteNameBuffer.toString()));
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
        if (isEmoteName(emoteNameBuffer.toString()) && input.charAt(input.length() - 1) == ':' && Objects.nonNull(lastEmoteStart)) {
            parsingResult.add(new EmoteParserResult()
                .setStartIndex(lastEmoteStart)
                .setEndIndex(index)
                .setEmoteName(emoteNameBuffer.toString()));
        }

        return parsingResult;
    }

    public static String convertTextWithUnicodeToNames(String text) {
        for (String emoteName : getAllEmoteNames()) {
            text = text.replaceAll(getEmoteByName(emoteName), ":" + emoteName + ":");
        }

        return text;
    }

    public static String toUnicodeString(String input) {
        StringBuilder renderResult = new StringBuilder();
        LinkedList<EmoteParserResult> parsingResult = EmoteParser.parse(input);
        int from = 0;

        for (EmoteParserResult emoteInfo : parsingResult) {
            if (input.substring(from, emoteInfo.getStartIndex()).length() > 0) {
                renderResult.append(input, from, emoteInfo.getStartIndex());
            }
            renderResult.append(getEmoteByName(emoteInfo.getEmoteName()));
            from = emoteInfo.getEndIndex() + 1;
        }
        if (from < input.length()) {
            renderResult.append(input.substring(from));
        }

        return renderResult.toString();
    }

    private static boolean isParsableEmoteName(String str)
    {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isLetter(str.charAt(i)) && str.charAt(i) != '_' && str.charAt(i) != '-') {
                return false;
            }
        }
        return true;
    }

    private static void addEmotesFromJSONArray(JSONArray jsonArray) {
        jsonArray.forEach((emoteInfo) -> {
            String emote = ((JSONObject) emoteInfo).getString("emoji");
            String emoteName = ((JSONObject) emoteInfo).getString("description");
            String[] split = emoteName.split("\\|");
            if (split.length > 1) {
                log.debug("{}", split[1]);
                emoteName = split[1].trim().replaceAll(" ", "_").toLowerCase();
            } else {
                emoteName = emoteName.replaceAll(" ", "_").toLowerCase();
            }

            if (!isParsableEmoteName(emoteName)) {
                return;
            }
            emoteMapping.put(emoteName, emote);
        });
    }
}
