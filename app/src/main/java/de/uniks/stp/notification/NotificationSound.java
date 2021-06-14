package de.uniks.stp.notification;

import java.util.Objects;

public enum NotificationSound {
    ATM_CASH_MACHINE_KEY_PRESS("atm-cash-machine-key-press.wav"),
    DRY_POP_UP_NOTIFICATION_ALERT("dry-pop-up-notification-alert.wav"),
    GAMING_LOCK("gaming-lock.wav"),
    LIGHT_BUTTON("light-button.wav"),
    LITTLE_CUTE_KISS("little-cute-kiss.wav"),
    MARTIAL_ARTS_FAST_PUNCH("martial-arts-fast-punch.wav"),
    RETRO_GAME_NOTIFICATION("retro-game-notification.wav");

    public final String key;

    NotificationSound(String key) {
        this.key = key;
    }

    public static NotificationSound getDefault() {
        return GAMING_LOCK;
    }

    public static NotificationSound fromKeyOrDefault(String key) {
        if (Objects.nonNull(key)) {
            for (final NotificationSound notificationSound : values()) {
                if (key.equals(notificationSound.key)) {
                    return notificationSound;
                }
            }
        }
        return getDefault();
    }
}
