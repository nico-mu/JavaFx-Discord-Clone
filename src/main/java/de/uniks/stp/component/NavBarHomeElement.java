package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import de.uniks.stp.controller.CustomEventCallback;
import javafx.scene.control.Tooltip;

import static de.uniks.stp.Constants.LBL_HOME;

public class NavBarHomeElement extends NavBarElement {
    public NavBarHomeElement(CustomEventCallback eventCallback) {
        super(eventCallback);
        Tooltip.install(navBarElement, new Tooltip(ViewLoader.loadLabel(LBL_HOME)));
        navBarImageView.setImage(ViewLoader.loadImage("home.png"));
    }
}
