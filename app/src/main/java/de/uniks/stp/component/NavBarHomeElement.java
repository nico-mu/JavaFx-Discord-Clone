package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.router.Router;
import javafx.scene.input.MouseEvent;

import javax.inject.Inject;

import static de.uniks.stp.Constants.LBL_HOME;

public class NavBarHomeElement extends NavBarElement {

    private final Router router;

    @Inject
    public NavBarHomeElement(ViewLoader viewLoader, Router router) {
        super(viewLoader);
        this.router = router;
        installTooltip(viewLoader.loadLabel(LBL_HOME));
        imageView.setImage(viewLoader.loadImage("home.png"));
        this.setId("home-button");
        notificationLabel.setVisible(false);
        circle.setVisible(false);
    }

    @Override
    protected void onMouseClicked(MouseEvent mouseEvent) {
        super.onMouseClicked(mouseEvent);
        router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_LIST_ONLINE_USERS);
    }
}
