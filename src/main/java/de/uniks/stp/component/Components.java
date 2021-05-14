package de.uniks.stp.component;

import java.net.URL;

public enum Components {
    USER_LIST("UserList.fxml"),
    USER_LIST_ENTRY("UserListEntry.fxml"),
    NAV_BAR_ELEMENT("NavBarElement.fxml"),
    NAV_BAR_LIST("NavBarList.fxml"),
    USER_LIST_SIDEBAR_ENTRY("UserListSidebarEntry.fxml"),
    CHAT_VIEW("ChatView.fxml"),
    CHAT_MESSAGE("ChatMessage.fxml");

    public final URL path;

    Components(String path) {
        this.path = Components.class.getResource(path);
    }
}
