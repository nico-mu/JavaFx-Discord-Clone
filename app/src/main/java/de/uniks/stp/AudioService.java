package de.uniks.stp;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

public class AudioService {
    private static Media notificationSoundFile;
    private static final Logger log = LoggerFactory.getLogger(AudioService.class);
    private static final String headlessCheck = System.getProperty("testfx.headless");

    public static void setNotificationSoundFile(String name) {
        notificationSoundFile = new Media(getNotificationSoundResource(name));
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

    public static String getNotificationSoundResource(String name) {
        URL resPath;
        if (name.equals(".")) {
            resPath = AudioService.class.getResource("audio/notification");
        } else {
            resPath = AudioService.class.getResource("audio/notification/" + name);
        }
        try {
            if (Objects.isNull(resPath)) {
                return "";
            }
            return new File(resPath.toURI()).toURI().toString();
        } catch (URISyntaxException e) {
            log.error("Could not find notification audio file " + name);
        }
        return "";
    }

    public static File[] getNotificationSoundFiles() {
        return new File(getNotificationSoundResource(".")).listFiles();
    }
}
