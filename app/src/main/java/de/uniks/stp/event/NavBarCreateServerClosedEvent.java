package de.uniks.stp.event;

import javafx.event.EventType;

public class NavBarCreateServerClosedEvent extends AbstractEvent {

    public static final EventType<NavBarCreateServerClosedEvent> NAV_BAR_CREATE_SERVER_CLOSED = new EventType<>(ABSTRACT_EVENT_TYPE, "NavBarCreateServerClosed");

    public NavBarCreateServerClosedEvent() {
        super(NAV_BAR_CREATE_SERVER_CLOSED);
    }
}
