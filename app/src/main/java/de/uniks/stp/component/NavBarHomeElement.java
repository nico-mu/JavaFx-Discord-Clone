package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.router.Router;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;

import static de.uniks.stp.Constants.LBL_HOME;

public class NavBarHomeElement extends NavBarElement {

    public NavBarHomeElement() {
        Tooltip.install(navBarElement, new Tooltip(ViewLoader.loadLabel(LBL_HOME)));
        imageView.setImage(ViewLoader.loadImage("home.png"));
    }

    @Override
    protected void onMouseClicked(MouseEvent mouseEvent) {
        super.onMouseClicked(mouseEvent);
        Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_LIST_ONLINE_USERS);
    }
}