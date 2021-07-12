package de.uniks.stp.component;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.User;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.scene.input.MouseEvent;

public class NavBarUserElement extends NavBarNotificationElement {

    final User user;
    private final Router router;

    @AssistedInject
    public NavBarUserElement(ViewLoader viewLoader,
                             Router router,
                             @Assisted User model) {
        super(viewLoader);
        user = model;
        this.router = router;
        this.setId(user.getId() + "-button");
        installTooltip(user.getName());
        imageView.setImage(viewLoader.loadImage("user.png"));
    }

    @Override
    protected void onMouseClicked(MouseEvent mouseEvent) {
        super.onMouseClicked(mouseEvent);
        RouteArgs args = new RouteArgs().addArgument(Constants.ROUTE_PRIVATE_CHAT_ARGS, user.getId());
        router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_PRIVATE_CHAT, args);
    }

    @AssistedFactory
    public interface NavBarUserElementFactory {
        NavBarUserElement create(User model);
    }
}
