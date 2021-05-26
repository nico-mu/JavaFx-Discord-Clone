package de.uniks.stp.component;

import de.uniks.stp.model.Notification;
import javafx.application.Platform;

public class NavBarNotificationElement extends NavBarElement {

    Notification model;

    public NavBarNotificationElement(Notification model) {
        this.model = model;
        setNotificationCount();
    }

    public Notification getModel() {
        return model;
    }

    /**
     * sets the notification label to given number and handles the visibility of the label and circle
     */
    void setNotificationCount() {
        int notifications = this.model.getNotificationCounter();
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

    private void setNotificationVisibility(boolean mode) {
        notificationLabel.setVisible(mode);
        circle.setVisible(mode);
    }

    private void setNotificationsLabel(String value) {
        Platform.runLater(() -> {
            notificationLabel.setText(value);
        });
    }
}
