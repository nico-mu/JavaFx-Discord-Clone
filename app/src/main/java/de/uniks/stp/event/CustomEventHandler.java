package de.uniks.stp.event;

import de.uniks.stp.component.ServerChannelElement;
import javafx.event.EventHandler;

public abstract class CustomEventHandler implements EventHandler<AbstractEvent> {

    public abstract void onChannelChanged(ServerChannelElement element);

    @Override
    public void handle(AbstractEvent event) {
        event.invokeHandler(this);
    }
}
