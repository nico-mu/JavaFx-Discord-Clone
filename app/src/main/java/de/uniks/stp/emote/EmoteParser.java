package de.uniks.stp.emote;

import de.uniks.stp.ViewLoader;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class EmoteParser {
    private static final Map<String, String> emoteMapping = new HashMap<>();
    private static final Map<String, String> emoteAliasMapping = new HashMap<>();
    private static final LinkedList<String> allEmoteNames;
    private static final LinkedList<String> allAlias;

    static {
        // Fill emote mapping
        InputStream inputStream = Objects.requireNonNull(ViewLoader.class.getResourceAsStream("emote/emote-list.json"));
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String text = bufferedReader.lines().collect(Collectors.joining("\n"));

        try {
            bufferedReader.close();
            inputStreamReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject(text);

        addEmotesFromJSONArray(jsonObject.getJSONArray("Smileys & People"));
        addEmotesFromJSONArray(jsonObject.getJSONArray("Objects"));
        addEmotesFromJSONArray(jsonObject.getJSONArray("Animals & Nature"));
        addEmotesFromJSONArray(jsonObject.getJSONArray("Flags"));
        allEmoteNames = new LinkedList<>(emoteMapping.keySet());

        // Fill emote alias mapping
        for (EmoteAlias alias : EmoteAlias.values()) {
            emoteAliasMapping.put(alias.getAlias(), alias.toString());
        }
        allAlias = new LinkedList<>(emoteAliasMapping.keySet());
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
        return allEmoteNames;
    }

    public static Map<String, String> getEmoteAliasMapping() {
        return emoteAliasMapping;
    }

    public static LinkedList<String> getAllAlias() {
        return allAlias;
    }

    public static String getEmoteNameByAlias(String alias) {
        return getEmoteAliasMapping().get(alias);
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
                isCollectingEmoteName = !isCollectingEmoteName;
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

    public static String convertTextWithAliasToNames(String text) {
        for (String alias : getAllAlias()) {
            String aliasName = ":" + getEmoteNameByAlias(alias).toLowerCase() + ":";
            text = text.replace(alias, aliasName);
            // Ability to escape alias via \
            text = text.replaceAll("\\\\" + aliasName, alias);
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

    private static boolean isParsableEmoteName(String str) {
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
