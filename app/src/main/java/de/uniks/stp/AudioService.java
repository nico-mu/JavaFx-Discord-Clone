package de.uniks.stp;

import de.uniks.stp.jpa.AccordSettingKey;
import de.uniks.stp.jpa.AppDatabaseService;
import de.uniks.stp.jpa.model.AccordSettingDTO;
import de.uniks.stp.notification.NotificationSound;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AudioService {
    private static final Logger log = LoggerFactory.getLogger(AudioService.class);
    private int volumePercent;
    Map<String, SourceDataLine>  userSourceDataLineMap;  //from VoiceChatClient, for adjusting volume
    private final String DEFAULT_SOUND_FILE = "gaming-lock.wav";
    private final AppDatabaseService databaseService;
    private Media notificationSoundFile;
    private String notificationSoundFileName;
    private final String headlessCheck = System.getProperty("testfx.headless");


    public AudioService(AppDatabaseService databaseService) {
        this.databaseService = databaseService;
        // get notification file
        AccordSettingDTO notificationSettings = databaseService.getAccordSetting(AccordSettingKey.NOTIFICATION_SOUND);
        if (Objects.nonNull(notificationSettings)) {
            setNotificationSoundFile(notificationSettings.getValue());
        } else {
            setNotificationSoundFile(DEFAULT_SOUND_FILE);
        }
        // get audio out volume value
        AccordSettingDTO volumeOutSettings = databaseService.getAccordSetting(AccordSettingKey.AUDIO_OUT_VOLUME);
        if (Objects.nonNull(volumeOutSettings)) {
            this.volumePercent = Integer.parseInt(volumeOutSettings.getValue());
        } else {
            this.volumePercent = 100;
        }
    }

    public void setUserSourceDataLineMap(Map<String, SourceDataLine>  userSourceDataLineMap){
        this.userSourceDataLineMap = userSourceDataLineMap;
    }

    public int getVolumePercent() {
        return volumePercent;
    }

    public void setVolumePercent(int volumePercent) {
        this.volumePercent = volumePercent;
        databaseService.saveAccordSetting(AccordSettingKey.AUDIO_OUT_VOLUME, String.valueOf(volumePercent));

        // adjust volume of every user in VoiceChat
        if(Objects.nonNull(userSourceDataLineMap)) {
            for (SourceDataLine audioOutDataLine: userSourceDataLineMap.values()) {
                setUserOutputVolume(audioOutDataLine);
            }
        }
    }

    public void setUserOutputVolume(SourceDataLine audioOutDataLine){
        if (audioOutDataLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl volume = (FloatControl) audioOutDataLine.getControl(FloatControl.Type.MASTER_GAIN);
            // calculate correct new value. Values below around -30.0 are not necessary, maximum is at 6.0206
            float max = 6.0206f;
            int min = -31;
            float diff = max - min;
            // in order to reach that -10% is actually -10% for all values:
            double correctPercent = 1-Math.pow(1- volumePercent*0.01,3);
            float newValue = min + (float) (correctPercent * diff);
            if (newValue < -30.5) {
                newValue = -80.0f;  // min is reached -> mute completely
            } else if (newValue > max) {
                newValue = max;  //in case rounding numbers went wrong
            }
            // set new value
            try{
                volume.setValue(newValue);
            } catch (Exception e){
                log.error("Failed to adjust volume for user in VoiceChat", e);
            }
        } else{
            log.error("AudioOutDataLine is not controlSupported!");
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
        double correctedVolume = Math.pow(volumePercent*0.01, 3);  //to reach that -10% is actually -10% for all values
        mediaPlayer.setVolume(correctedVolume);
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
