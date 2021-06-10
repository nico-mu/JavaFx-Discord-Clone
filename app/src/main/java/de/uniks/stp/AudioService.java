package de.uniks.stp;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

public class AudioService {
    private static Media notificationSoundFile;
    private static String notificationSoundFileName;
    private static final String headlessCheck = System.getProperty("testfx.headless");

    public static void setNotificationSoundFile(String name) {
        File file = Objects.requireNonNull(getNotificationSoundResource(name));
        notificationSoundFileName = file.getName();
        notificationSoundFile = new Media(file.toURI().toString());
    }

    public static void playNotificationSound() {
        if (Objects.nonNull(headlessCheck) && headlessCheck.equals("true")) {
            return;
        }
        if (Objects.isNull(notificationSoundFile)) {
            setNotificationSoundFile("gaming-lock.wav");
        }
        MediaPlayer mediaPlayer = new MediaPlayer(notificationSoundFile);
        mediaPlayer.play();
    }

    public static File getNotificationSoundResource(String name) {
        URL resPath;
        if (name.equals(".")) {
            resPath = AudioService.class.getResource("audio/notification");
        } else {
            resPath = AudioService.class.getResource("audio/notification/" + name);
        }
        if (Objects.isNull(resPath)) {
            return null;
        }
        return new File(resPath.getPath());
    }

    public static File[] getNotificationSoundFiles() {
        return Objects.requireNonNull(getNotificationSoundResource(".")).listFiles();
    }

    public static String getNotificationSoundFileName() {
        return notificationSoundFileName;
    }
}
