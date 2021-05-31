package de.uniks.stp.event;

import javafx.event.EventType;

public class NavBarHomeElementActiveEvent extends AbstractEvent {

    public static final EventType<NavBarHomeElementActiveEvent> NAV_BAR_HOME_ELEMENT_ACTIVE = new EventType<>(ABSTRACT_EVENT_TYPE, "NavBarHomeElementActive");

    public NavBarHomeElementActiveEvent() {
        super(NAV_BAR_HOME_ELEMENT_ACTIVE);
    }

}
