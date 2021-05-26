package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.event.NavBarHomeElementActiveEvent;
import de.uniks.stp.model.User;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;

public class NavBarUserElement extends NavBarNotificationElement {

    User model;

    public NavBarUserElement(User model) {
        this.model = model;
        this.setId(model.getId() + "-button");
        Tooltip.install(navBarElement, new Tooltip(model.getName()));
        imageView.setImage(ViewLoader.loadImage("user.png"));
    }

    public User getModel() {
        return model;
    }

    @Override
    protected void onMouseClicked(MouseEvent mouseEvent) {
        super.onMouseClicked(mouseEvent);
        resetNotifications();
        RouteArgs args = new RouteArgs().addArgument(Constants.ROUTE_PRIVATE_CHAT_ARGS, model.getId());
        Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_PRIVATE_CHAT, args);
        this.fireEvent(new NavBarHomeElementActiveEvent());
    }
}
