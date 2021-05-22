package de.uniks.stp.event;

import de.uniks.stp.component.ServerChannelElement;
import javafx.event.EventType;

public class ChannelChangeEvent extends AbstractEvent implements ParameterizedEventInterface<ServerChannelElement> {

    public static final EventType<ChannelChangeEvent> CHANNEL_CHANGE = new EventType<>(ABSTRACT_EVENT_TYPE, "ChannelChangeEvent");
    private final ServerChannelElement param;

    public ChannelChangeEvent(ServerChannelElement serverChannelElement) {
        super(CHANNEL_CHANGE);
        param = serverChannelElement;
    }

    public ServerChannelElement getParam() {
        return param;
    }
}
