package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import de.uniks.stp.controller.CustomEventCallback;
import de.uniks.stp.model.Server;
import javafx.scene.control.Tooltip;

public class NavBarServerElement extends NavBarElement {
    public NavBarServerElement(Server model, CustomEventCallback eventCallback) {
        super(eventCallback);
        Tooltip.install(navBarElement, new Tooltip(model.getName()));
        navBarImageView.setImage(ViewLoader.loadImage("server.png"));
    }
}
