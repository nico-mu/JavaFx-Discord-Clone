package de.uniks.stp;

import de.uniks.stp.jpa.AccordSettingKey;
import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.jpa.model.AccordSettingDTO;
import de.uniks.stp.model.Accord;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
        File file = Objects.requireNonNull(getNotificationSoundResource(name));
        notificationSoundFileName = file.getName();
        notificationSoundFile = new Media(file.toURI().toString());
        editor.getOrCreateAccord().setNotificationSound(notificationSoundFileName);
    }

    public static void playNotificationSound() {
        if (Objects.nonNull(headlessCheck) && headlessCheck.equals("true")) {
            return;
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
        try {
            return new File(URLDecoder.decode(resPath.getPath(), StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            // not going to happen - value came from JDK's own StandardCharsets
        }
        return null;
    }

    public static File[] getNotificationSoundFiles() {
        return Objects.requireNonNull(getNotificationSoundResource(".")).listFiles();
    }

    public static String getNotificationSoundFileName() {
        return notificationSoundFileName;
    }

    private void onNotificationSoundPropertyChange(PropertyChangeEvent propertyChangeEvent) {
        final String newNotificationSound = (String) propertyChangeEvent.getNewValue();
        DatabaseService.saveAccordSetting(AccordSettingKey.NOTIFICATION_SOUND, newNotificationSound);
    }
}
