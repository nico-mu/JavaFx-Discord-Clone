package de.uniks.stp.event;

import de.uniks.stp.component.ServerChannelElement;
import javafx.event.EventType;

public class ChannelChangeEvent extends AbstractEvent {

    public static final EventType<AbstractEvent> CHANNEL_CHANGE_EVENT_EVENT_TYPE = new EventType<>(ABSTRACT_EVENT_TYPE, "ChannelChangeEvent");
    private final ServerChannelElement param;

    public ChannelChangeEvent(ServerChannelElement serverChannelElement) {
        super(CHANNEL_CHANGE_EVENT_EVENT_TYPE);
        param = serverChannelElement;
    }

    @Override
    public void invokeHandler(CustomEventHandler handler) {
        handler.onChannelChanged(param);
    }
}
