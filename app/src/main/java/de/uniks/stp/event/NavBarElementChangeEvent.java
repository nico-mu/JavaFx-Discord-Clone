package de.uniks.stp.event;

import de.uniks.stp.component.NavBarElement;
import javafx.event.EventType;

public class NavBarElementChangeEvent extends AbstractEvent implements ParameterizedEventInterface<NavBarElement> {

    public static final EventType<NavBarElementChangeEvent> NAV_BAR_ELEMENT_CHANGE = new EventType<>(ABSTRACT_EVENT_TYPE, "NavBarElementChange");
    private final NavBarElement param;

    public NavBarElementChangeEvent(NavBarElement navBarElement) {
        super(NAV_BAR_ELEMENT_CHANGE);
        param = navBarElement;
    }

    @Override
    public NavBarElement getParam() {
        return param;
    }
}
