package de.uniks.stp.component;

import java.net.URL;

public enum Components {
    USER_LIST("UserList.fxml"),
    USER_LIST_ENTRY("UserListEntry.fxml"),
    NAV_BAR_ELEMENT("NavBarElement.fxml"),
    NAV_BAR_LIST("NavBarList.fxml"),
    SERVER_CATEGORY_LIST("ServerCategoryList.fxml"),
    SERVER_CATEGORY_ELEMENT("ServerCategoryElement.fxml"),
    SERVER_CHANNEL_ELEMENT("ServerChannelElement.fxml");


    public final URL path;

    Components(String path) {
        this.path = Components.class.getResource(path);
    }
}
