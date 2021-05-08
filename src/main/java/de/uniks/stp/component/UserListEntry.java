package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import javafx.scene.Parent;
import javafx.scene.text.Text;

public class UserListEntry {
    private static final String USER_NAME_TEXT_ID = "#user-name-text";
    private static final String USER_STATUS_TEXT_ID = "#user-status-text";

    private final Text userNameText;
    private final Text userStatusText;

    UserListEntry() {
        final Parent viewElement = ViewLoader.loadComponent(Components.USER_LIST_ENTRY);

        userNameText = (Text) viewElement.lookup(USER_NAME_TEXT_ID);
        userStatusText = (Text) viewElement.lookup(USER_STATUS_TEXT_ID);
    }

    public void setUserName(final String userName) {
        userNameText.setText(userName);
    }

    public void setUserStatusText(final UserStatus userStatus) {
        switch (userStatus) {
            case ONLINE:
                userStatusText.setText("online"); // TODO: Enable language-change
                break;
            case OFFLINE:
                userStatusText.setText("offline"); // TODO: Enable language-change
                break;
        }
    }

    public enum UserStatus {
        ONLINE, OFFLINE
    }
}
