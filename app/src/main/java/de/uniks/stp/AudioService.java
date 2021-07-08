package de.uniks.stp;

import de.uniks.stp.jpa.AccordSettingKey;
import de.uniks.stp.jpa.AppDatabaseService;
import de.uniks.stp.jpa.model.AccordSettingDTO;
import de.uniks.stp.notification.NotificationSound;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AudioService {
    private final String DEFAULT_SOUND_FILE = "gaming-lock.wav";
    private final AppDatabaseService databaseService;
    private Media notificationSoundFile;
    private String notificationSoundFileName;
    private final String headlessCheck = System.getProperty("testfx.headless");


    public AudioService(AppDatabaseService databaseService) {
        this.databaseService = databaseService;
        AccordSettingDTO settingsDTO = databaseService.getAccordSetting(AccordSettingKey.NOTIFICATION_SOUND);
        if (Objects.nonNull(settingsDTO)) {
            setNotificationSoundFile(settingsDTO.getValue());
        } else {
            setNotificationSoundFile(DEFAULT_SOUND_FILE);
        }
    }

    /**
     * Sets the notification sound file which the service uses.
     * @param name file name with extension
     */
    public void setNotificationSoundFile(String name) {
        if (Objects.isNull(name)) {
            name = DEFAULT_SOUND_FILE;
        }
        String path = getNotificationSoundPath(name);
        notificationSoundFileName = pathToFileName(path);
        notificationSoundFile = new Media(path);
        databaseService.saveAccordSetting(AccordSettingKey.NOTIFICATION_SOUND,
            NotificationSound.fromKeyOrDefault(notificationSoundFileName).key);
    }

    /**
     * Converts the path of a file to the file name.
     * @param path path to a file
     * @return file name
     */
    public String pathToFileName(String path) {
        return Objects.requireNonNull(path).substring(path.lastIndexOf('/') + 1);
    }

    /**
     * Plays the set notification sound file
     */
    public void playNotificationSound() {
        if (Objects.nonNull(headlessCheck) && headlessCheck.equals("true")) {
            return;
        }
        MediaPlayer mediaPlayer = new MediaPlayer(notificationSoundFile);
        mediaPlayer.play();
    }

    /**
     * Converts a file name to the path of a file.
     * @param name file name
     * @return path of the file
     */
    public String getNotificationSoundPath(String name) {
        URL resPath = AudioService.class.getResource("audio/notification/" + name);
        if (Objects.isNull(resPath)) {
            return null;
        }
        try {
            return resPath.toURI().toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get the path of all files located in the resources bundles under audio/notification/.
     * @return list of all file paths
     */
    public List<String> getNotificationSoundPaths() {
        List<String> paths = new ArrayList<>();
        for (NotificationSound sound : NotificationSound.values()) {
            paths.add(getNotificationSoundPath(sound.key));
        }
        return paths;
    }

    public String getNotificationSoundFileName() {
        return notificationSoundFileName;
    }
}
