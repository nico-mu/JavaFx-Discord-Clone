package de.uniks.stp.component;

public interface NotificationComponentInterface {
    /**
     * sets the notification label to given number and handles the visibility of the label and circle
     * @param notifications the number of the element notifications
     */
    void setNotificationCount(int notifications);

    /**
     * gets called by setNotificationCount, DO NOT CALL IT AT ANY OTHER POINT!
     * @param mode
     */
    void setNotificationVisibility(boolean mode);
}
