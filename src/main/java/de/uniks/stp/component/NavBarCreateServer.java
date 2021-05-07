package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import javafx.scene.control.Tooltip;

public class NavBarCreateServer extends NavBarElement {

    public NavBarCreateServer() {
        Tooltip.install(navBarElement, new Tooltip(ViewLoader.loadLabel(Constants.LBL_CREATE_SERVER)));
        imageView.setImage(ViewLoader.loadImage("plus.png"));
    }
}
