package de.uniks.stp.component;

public interface Notifications {
    /**
     * sets the notification label to given number and handles the visibility of the label and circle
     * @param notifications the number of the element notifications
     */
    void setNotificationCount(int notifications);

    void setNotificationVisibility(boolean mode);
}
