package de.uniks.stp.emote;

import de.uniks.stp.component.EmotePicker;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class EmoteRenderer {
    public Map<String, String> getEmoteMapping() {
        return EmoteParser.getEmoteMapping();
    }

    public boolean isEmoteName(String emoteName) {
        return EmoteParser.isEmoteName(emoteName);
    }

    public TextFlow render(String input) {
        // InputStream inputStream = Objects.requireNonNull(ViewLoader.class.getResourceAsStream("emote/OpenMoji-Color.ttf"));
        // Font font = Font.loadFont(inputStream, 40);
        // Text text = new Text();
        // text.setFont(font);
        // text.setText("\uD83D\uDE00");

       TextFlow renderResult = new TextFlow();

        return renderResult;
    }
}
