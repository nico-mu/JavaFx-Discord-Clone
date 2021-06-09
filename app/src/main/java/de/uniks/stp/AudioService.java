package de.uniks.stp;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

public class AudioService {
    private static Media notificationSoundFile;
    private static Logger log = LoggerFactory.getLogger(AudioService.class);

    public static void setNotificationSoundFile(String name) {
        URL resPath = AudioService.class.getResource("audio/notification/" + name);
        String fileURI = null;
        try {
            if (Objects.isNull(resPath)) {
                return;
            }
            fileURI = new File(resPath.toURI()).toURI().toString();
        } catch (URISyntaxException e) {
            log.error("Could not find notification audio file " + name);
        }
        notificationSoundFile = new Media(fileURI);
    }

    public static void playNotificationSound() {
        if (Objects.isNull(notificationSoundFile)) {
            setNotificationSoundFile("default.wav");
        }
        MediaPlayer mediaPlayer = new MediaPlayer(notificationSoundFile);
        mediaPlayer.play();
    }
}
