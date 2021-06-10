package de.uniks.stp.emote;

import de.uniks.stp.ViewLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.io.InputStream;
import java.util.Objects;

public class RealLinkedImage implements LinkedImage {
    private final String emoteName;

    public RealLinkedImage(String emoteName) {
        this.emoteName = emoteName;
    }

    @Override
    public boolean isReal() {
        return true;
    }

    @Override
    public String getEmoteName() {
        return emoteName;
    }

    @Override
    public String toString() {
        return emoteName;
    }

    @Override
    public Node createNode() {
        HBox container = new HBox();
        container.setPadding(new Insets(2,4,0,4));
        container.setAlignment(Pos.CENTER);
        String hexCode = (String) EmoteParser.getEmoteByName(getEmoteName()).codePoints().mapToObj(Integer::toHexString).toArray()[0];
        InputStream inputStream = Objects.requireNonNull(ViewLoader.class.getResourceAsStream("emote/openmoji/" + hexCode.toUpperCase() + ".png"));
        Image image = new Image(inputStream);
        ImageView imageView = new ImageView();
        imageView.setScaleX(1.7);
        imageView.setScaleY(1.7);
        imageView.setFitWidth(12);
        imageView.setFitHeight(12);
        imageView.setImage(image);
        container.getChildren().addAll(imageView);
        return container;
    }
}
