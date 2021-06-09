package de.uniks.stp.emote;

import de.uniks.stp.ViewLoader;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.util.Objects;

public class RealLinkedImage implements LinkedImage {
    private final String imagePath;

    public RealLinkedImage(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public boolean isReal() {
        return true;
    }

    @Override
    public String getImagePath() {
        return imagePath;
    }

    @Override
    public String toString() {
        return imagePath.replace(".png", "");
    }

    @Override
    public Node createNode() {
        InputStream inputStream = Objects.requireNonNull(ViewLoader.class.getResourceAsStream("emote/" + getImagePath()));
        Image image = new Image(inputStream);
        ImageView imageView = new ImageView();
        imageView.setScaleX(1.75);
        imageView.setScaleY(1.75);
        imageView.setFitWidth(13);
        imageView.setFitHeight(13);
        imageView.setImage(image);
        return imageView;
    }
}
