package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import java.io.InputStream;
import java.util.Objects;

public class TextWithEmoji extends FlowPane {

    public TextWithEmoji() {
        // addEmojiImage();
        addText("Hello World");
        this.setAlignment(Pos.CENTER_LEFT);
    }

    /* private Image loadEmoji(Emojis emoji) {
        final InputStream inputStream = Objects.requireNonNull(ViewLoader.class.getResourceAsStream("emojis/" + emoji));
        return new Image(inputStream);
    }

    public void addEmojiImage() {
        Image image = loadEmoji(Emojis.ROLLING_ON_THE_FLOOR_LAUGHING);
        ImageView imageView = new ImageView();
        imageView.setFitHeight(16);
        imageView.setFitWidth(16);
        imageView.setImage(image);

        this.getChildren().add(imageView);
    }*/

    public void addText(String text) {
        this.getChildren().add(new Text(text));
    }
}
