package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import javafx.application.Platform;

public class NavBarNotificationElement extends NavBarElement implements NotificationComponentInterface {

    public NavBarNotificationElement(ViewLoader viewLoader) {
        super(viewLoader);
    }

    @Override
    public void setNotificationCount(int notifications) {
        if (0 < notifications && notifications < 10) {
            setNotificationsLabel(Integer.toString(notifications));
            setNotificationVisibility(true);
        } else if (10 <= notifications) {
            setNotificationsLabel("9+");
            setNotificationVisibility(true);
        } else {
            setNotificationsLabel("0");
            setNotificationVisibility(false);
        }
    }

    @Override
    public void setNotificationVisibility(boolean mode) {
        notificationLabel.setVisible(mode);
        circle.setVisible(mode);
    }

    private void setNotificationsLabel(String value) {
        Platform.runLater(() -> {
            notificationLabel.setText(value);
        });
    }
}
