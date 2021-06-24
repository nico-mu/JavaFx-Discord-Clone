package de.uniks.stp.component;

import javafx.scene.layout.HBox;

public abstract class ServerChannelElement extends HBox {

    public abstract void setActive(boolean active);

    public abstract void updateText(String newName);

    public abstract String getChannelTextId();

    public abstract void setNotificationCount(int notifications);
}
