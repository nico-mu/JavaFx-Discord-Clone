package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Server;
import javafx.scene.control.Tooltip;

public class NavBarServerElement extends NavBarElement {
    public NavBarServerElement(Server model) {
        Tooltip.install(navBarElement, new Tooltip(model.getName()));
        imageView.setImage(ViewLoader.loadImage("server.png"));
    }
}
