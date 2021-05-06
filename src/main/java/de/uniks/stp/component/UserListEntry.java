package de.uniks.stp.component;

import de.uniks.stp.model.User;
import de.uniks.stp.ViewLoader;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class UserListEntry extends AbstractComponent<HBox> {
    private static final String USER_NAME_TEXT_ID = "#user-name-text";

    private final Text userNameText;

    public UserListEntry(final User user) {
        setRootElement((HBox) ViewLoader.loadComponent(Components.USER_LIST_ENTRY));

        userNameText = (Text) getRootElement().lookup(USER_NAME_TEXT_ID);
        setUserName(user.getName());
    }

    public void setUserName(final String userName) {
        userNameText.setText(userName);
    }
}
