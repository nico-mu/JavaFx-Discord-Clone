package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.event.NavBarHomeElementActiveEvent;
import de.uniks.stp.model.User;
import de.uniks.stp.notification.NotificationService;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;

public class NavBarUserElement extends NavBarNotificationElement {

    final User user;

    public NavBarUserElement(User model) {
        user = model;
        this.setId(user.getId() + "-button");
        Tooltip.install(navBarElement, new Tooltip(user.getName()));
        imageView.setImage(ViewLoader.loadImage("user.png"));
    }

    @Override
    protected void onMouseClicked(MouseEvent mouseEvent) {
        super.onMouseClicked(mouseEvent);
        RouteArgs args = new RouteArgs().addArgument(Constants.ROUTE_PRIVATE_CHAT_ARGS, user.getId());
        Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_PRIVATE_CHAT, args);
        this.fireEvent(new NavBarHomeElementActiveEvent());
    }
}
