package de.uniks.stp.component;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Server;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.scene.input.MouseEvent;

public class NavBarServerElement extends NavBarNotificationElement {

    private final Router router;
    Server model;

    @AssistedInject
    public NavBarServerElement(ViewLoader viewLoader,
                               Router router,
                               @Assisted Server model) {
        super(viewLoader);
        this.router = router;
        navBarElement.setId(model.getId()+"-navBarElement");  //used for tests
        this.model = model;
        installTooltip(model.getName());
        imageView.setImage(viewLoader.loadImage("server.png"));
        this.setNotificationVisibility(false);
    }

    @Override
    protected void onMouseClicked(MouseEvent mouseEvent) {
        super.onMouseClicked(mouseEvent);
        RouteArgs args = new RouteArgs().addArgument(":id", model.getId());
        router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args);
    }

    @AssistedFactory
    public interface NavBarServerElementFactory {
        NavBarServerElement create(Server model);
    }
}
