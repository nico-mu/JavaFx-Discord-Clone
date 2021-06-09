package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Server;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class NavBarServerElement extends NavBarNotificationElement {

    Server model;
    PropertyChangeListener serverNamePropertyChangeListener = this::onServerNamePropertyChange;

    public NavBarServerElement(Server model) {
        navBarElement.setId(model.getId()+"-navBarElement");
        this.model = model;
        Tooltip.install(navBarElement, new Tooltip(model.getName()));
        imageView.setImage(ViewLoader.loadImage("server.png"));
        this.setNotificationVisibility(false);
        model.listeners().addPropertyChangeListener(Server.PROPERTY_NAME, serverNamePropertyChangeListener);
    }

    @Override
    protected void onMouseClicked(MouseEvent mouseEvent) {
        super.onMouseClicked(mouseEvent);
        RouteArgs args = new RouteArgs().addArgument(":id", model.getId());
        Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER, args);
    }

    private void onServerNamePropertyChange(PropertyChangeEvent propertyChangeEvent) {
        Tooltip.install(navBarElement, new Tooltip(model.getName()));
    }
}
