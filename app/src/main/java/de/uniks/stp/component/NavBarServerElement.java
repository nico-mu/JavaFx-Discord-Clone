package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Server;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;

public class NavBarServerElement extends NavBarNotificationElement {

    Server model;

    public NavBarServerElement(Server model) {
        this.model = model;
        Tooltip.install(navBarElement, new Tooltip(model.getName()));
        imageView.setImage(ViewLoader.loadImage("server.png"));
        notificationLabel.setVisible(false);
        circle.setVisible(false);
    }

    public Server getModel() {
        return model;
    }

    @Override
    protected void onMouseClicked(MouseEvent mouseEvent) {
        super.onMouseClicked(mouseEvent);
        resetNotifications();
        RouteArgs args = new RouteArgs().addArgument(":id", model.getId());
        Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args);
    }
}
