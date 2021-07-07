package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.event.ChannelChangeEvent;
import de.uniks.stp.model.Channel;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.scene.layout.HBox;

public abstract class ServerChannelElement extends HBox {

    public abstract void setActive(boolean active);

    public abstract void updateText(String newName);

    public abstract String getChannelTextId();

    public abstract void setNotificationCount(int notifications);


    protected void onMouseClicked(Channel channel, Router router) {
        this.fireEvent(new ChannelChangeEvent(this));
        RouteArgs args = new RouteArgs();
        args.addArgument(":id", channel.getCategory().getServer().getId());
        args.addArgument(":categoryId", channel.getCategory().getId());
        args.addArgument(":channelId", channel.getId());
        router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER + Constants.ROUTE_CHANNEL, args);
    }
}
