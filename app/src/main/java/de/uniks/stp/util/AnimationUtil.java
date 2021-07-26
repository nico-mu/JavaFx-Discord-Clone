package de.uniks.stp.util;

import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;

public class AnimationUtil {

    public void gearEntered(ImageView imageView) {
        imageView.setRotate(20);
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setContrast(-0.6);
        imageView.setEffect(colorAdjust);
    }

    public void gearExited(ImageView imageView) {
        imageView.setRotate(0);
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setContrast(0);
        imageView.setEffect(colorAdjust);
    }

    public void iconEntered(ImageView imageView) {
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setContrast(-0.6);
        imageView.setEffect(colorAdjust);
    }

    public void iconExited(ImageView imageView) {
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setContrast(0);
        imageView.setEffect(colorAdjust);
    }
}
