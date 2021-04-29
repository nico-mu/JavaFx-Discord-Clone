package de.uniks.stp.network;

import de.uniks.stp.Editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

public class UserKeyProvider {

    private static String userKey;
    private static Editor editor;
    //listener doesn't need to be removed, we wait for gc to collect it, when app is closed
    private static final PropertyChangeListener userKeyChangeListener = UserKeyProvider::onUserKeyChanged;

    private static void onUserKeyChanged(PropertyChangeEvent propertyChangeEvent) {
        Object newValue = propertyChangeEvent.getNewValue();

        if(Objects.nonNull(newValue) && newValue instanceof String) {
            String userKey = (String)newValue;
            if(!userKey.isEmpty()) {
                UserKeyProvider.userKey = userKey;
                return;
            }
        }
        UserKeyProvider.userKey = null;
    }

    public static String getUserKey() {
        return userKey;
    }

    public static void setEditor(Editor editor) {
        UserKeyProvider.editor = editor;
        editor.getOrCreateAccord()
            .listeners()
            .addPropertyChangeListener(userKeyChangeListener);
    }
}
