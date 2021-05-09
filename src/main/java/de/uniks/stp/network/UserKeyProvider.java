package de.uniks.stp.network;

import de.uniks.stp.Editor;
import de.uniks.stp.model.Accord;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

public class UserKeyProvider {

    private static String userKey;
    //listener doesn't need to be removed, we wait for gc to collect it, when app is closed
    private static final PropertyChangeListener userKeyChangeListener = UserKeyProvider::onUserKeyChanged;
    private static Editor editor;

    private static void onUserKeyChanged(PropertyChangeEvent propertyChangeEvent) {
        Object newValue = propertyChangeEvent.getNewValue();

        if (Objects.nonNull(newValue) && newValue instanceof String) {
            String userKey = (String) newValue;
            if (!userKey.isEmpty()) {
                WebSocketService.stop();
                UserKeyProvider.userKey = userKey;

                WebSocketService.init();
                return;
            }
        }
        WebSocketService.stop();
        UserKeyProvider.userKey = null;
    }

    public static String getUserKey() {
        return userKey;
    }

    public static void setEditor(Editor editor) {
        UserKeyProvider.editor = editor;
        editor.getOrCreateAccord()
            .listeners()
            .addPropertyChangeListener(Accord.PROPERTY_USER_KEY, userKeyChangeListener);
    }
}
