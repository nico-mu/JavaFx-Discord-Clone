package de.uniks.stp;

import de.uniks.stp.jpa.AccordSettingKey;
import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.jpa.model.AccordSettingDTO;
import de.uniks.stp.model.Accord;
import de.uniks.stp.notification.NotificationSound;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AudioService {
    private static final String DEFAULT_SOUND_FILE = "gaming-lock.wav";
    private static Media notificationSoundFile;
    private static String notificationSoundFileName;
    private static final String headlessCheck = System.getProperty("testfx.headless");
    private final PropertyChangeListener notificationSoundPropertyChangeListener = this::onNotificationSoundPropertyChange;
    private final Editor editor;


    public AudioService(Editor editor) {
        this.editor = editor;
        this.editor.getOrCreateAccord().listeners().addPropertyChangeListener(Accord.PROPERTY_NOTIFICATION_SOUND, notificationSoundPropertyChangeListener);
        AccordSettingDTO settingsDTO = DatabaseService.getAccordSetting(AccordSettingKey.NOTIFICATION_SOUND);
        if (Objects.nonNull(settingsDTO)) {
            setNotificationSoundFile(settingsDTO.getValue());
        } else {
            setNotificationSoundFile(DEFAULT_SOUND_FILE);
        }
    }

    public void stop() {
        this.editor.getOrCreateAccord().listeners().removePropertyChangeListener(Accord.PROPERTY_NOTIFICATION_SOUND, notificationSoundPropertyChangeListener);
    }

    public void setNotificationSoundFile(String name) {
        if (Objects.isNull(name)) {
            name = DEFAULT_SOUND_FILE;
        }
        String path = getNotificationSoundPath(name);
        notificationSoundFileName = pathToFileName(path);
        notificationSoundFile = new Media(path);
        editor.getOrCreateAccord().setNotificationSound(notificationSoundFileName);
    }

    public static String pathToFileName(String path) {
        return Objects.requireNonNull(path).substring(path.lastIndexOf('/') + 1);
    }

    public static void playNotificationSound() {
        if (Objects.nonNull(headlessCheck) && headlessCheck.equals("true")) {
            return;
        }
        MediaPlayer mediaPlayer = new MediaPlayer(notificationSoundFile);
        mediaPlayer.play();
    }

    public static String getNotificationSoundPath(String name) {
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

    public static List<String> getNotificationSoundPaths() {
        List<String> paths = new ArrayList<>();
        for (NotificationSound sound : NotificationSound.values()) {
            paths.add(getNotificationSoundPath(sound.key));
        }
        return paths;
    }

    public static String getNotificationSoundFileName() {
        return notificationSoundFileName;
    }

    private void onNotificationSoundPropertyChange(PropertyChangeEvent propertyChangeEvent) {
        final String newNotificationSound = (String) propertyChangeEvent.getNewValue();
        DatabaseService.saveAccordSetting(AccordSettingKey.NOTIFICATION_SOUND, newNotificationSound);
    }
}
