package de.uniks.stp.component;

import de.uniks.stp.model.Notification;
import javafx.application.Platform;

public class NavBarNotificationElement extends NavBarElement {

    /**
     * sets the notification label to given number and handles the visibility of the label and circle
     * @param notifications the number of the element notifications
     */
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

    void setNotificationVisibility(boolean mode) {
        notificationLabel.setVisible(mode);
        circle.setVisible(mode);
    }

    private void setNotificationsLabel(String value) {
        Platform.runLater(() -> {
            notificationLabel.setText(value);
        });
    }
}
