package de.uniks.stp.event;


import javafx.event.Event;
import javafx.event.EventType;

public abstract class AbstractEvent extends Event {
    public static final EventType<AbstractEvent> ABSTRACT_EVENT_TYPE = new EventType<>(ANY);

    public AbstractEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }

    public abstract void invokeHandler(CustomEventHandler handler);
}
