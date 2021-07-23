package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.AudioService;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.component.AudioDeviceComboBox;
import de.uniks.stp.component.KeyBasedComboBox;
import de.uniks.stp.jpa.AccordSettingKey;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.network.voice.VoiceChatService;
import de.uniks.stp.router.Router;
import de.uniks.stp.view.Languages;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Slider;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.sound.sampled.Mixer;
import java.util.HashMap;
import java.util.MissingResourceException;

public class SettingsModal extends AbstractModal {

    public static final String SETTINGS_APPLY_BUTTON = "#settings-apply-button";
    public static final String SETTINGS_CANCEL_BUTTON = "#settings-cancel-button";
    public static final String SETTINGS_COMBO_SELECT_LANGUAGE = "#combo-select-language";
    public static final String SETTINGS_COMBO_SELECT_NOTIFICATION_SOUND = "#combo-select-notification-sound";
    public static final String SETTINGS_SLIDER_INPUT_VOLUME = "#slider-input-volume";
    public static final String SETTINGS_SLIDER_OUTPUT_VOLUME = "#slider-output-volume";
    public static final String SETTINGS_COMBO_SELECT_INPUT_DEVICE = "#combo-select-input-device";
    public static final String SETTINGS_COMBO_SELECT_OUTPUT_DEVICE = "#combo-select-output-device";
    private final JFXButton applyButton;
    private final JFXButton cancelButton;
    private final KeyBasedComboBox languageComboBox;
    private final KeyBasedComboBox notificationComboBox;
    private final Slider inputVolumeSlider;
    private final Slider outputVolumeSlider;
    private final AudioDeviceComboBox inputDeviceComboBox;
    private final AudioDeviceComboBox outputDeviceComboBox;
    private static final Logger log = LoggerFactory.getLogger(SettingsModal.class);
    private final ViewLoader viewLoader;
    private final Router router;
    private final SessionDatabaseService databaseService;
    private final AudioService audioService;
    private final VoiceChatService voiceChatService;
    private final Stage primaryStage;
    private Mixer currentMicrophone;
    private Mixer currentSpeaker;

    private String currentLanguage;
    private String currentNotificationSoundFile;
    private int currentOutputVolume;

    @AssistedInject
    public SettingsModal(ViewLoader viewLoader,
                         Router router,
                         @Named("primaryStage") Stage primaryStage,
                         SessionDatabaseService databaseService,
                         AudioService audioService,
                         VoiceChatService voiceChatService,
                         @Assisted Parent root) {
        super(root, primaryStage);
        this.primaryStage = primaryStage;
        this.viewLoader = viewLoader;
        this.router = router;
        this.databaseService = databaseService;
        this.audioService = audioService;
        this.voiceChatService = voiceChatService;

        setTitle(viewLoader.loadLabel(Constants.LBL_SELECT_LANGUAGE_TITLE));

        applyButton = (JFXButton) view.lookup(SETTINGS_APPLY_BUTTON);
        cancelButton = (JFXButton) view.lookup(SETTINGS_CANCEL_BUTTON);

        languageComboBox = (KeyBasedComboBox) view.lookup(SETTINGS_COMBO_SELECT_LANGUAGE);
        languageComboBox.addOptions(getLanguages());
        currentLanguage = viewLoader.getCurrentLocale().getLanguage();
        languageComboBox.setSelection(currentLanguage);

        notificationComboBox = (KeyBasedComboBox) view.lookup(SETTINGS_COMBO_SELECT_NOTIFICATION_SOUND);
        notificationComboBox.addOptions(getNotificationSounds());
        currentNotificationSoundFile = audioService.getNotificationSoundFileName();
        notificationComboBox.setSelection(currentNotificationSoundFile);

        inputVolumeSlider = (Slider) view.lookup(SETTINGS_SLIDER_INPUT_VOLUME);
        // TODO
        outputVolumeSlider = (Slider) view.lookup(SETTINGS_SLIDER_OUTPUT_VOLUME);
        outputVolumeSlider.setValue(audioService.getVolumePercent());

        inputDeviceComboBox = (AudioDeviceComboBox) view.lookup(SETTINGS_COMBO_SELECT_INPUT_DEVICE);
        inputDeviceComboBox.init();
        inputDeviceComboBox.withOptions(voiceChatService.getAvailableMicrophones());
        currentMicrophone = voiceChatService.getSelectedMicrophone();
        inputDeviceComboBox.setSelection(currentMicrophone);

        outputDeviceComboBox = (AudioDeviceComboBox) view.lookup(SETTINGS_COMBO_SELECT_OUTPUT_DEVICE);
        outputDeviceComboBox.init();
        outputDeviceComboBox.withOptions(voiceChatService.getAvailableSpeakers());
        currentSpeaker = voiceChatService.getSelectedSpeaker();
        outputDeviceComboBox.setSelection(currentSpeaker);

        applyButton.setOnAction(this::onApplyButtonClicked);
        applyButton.setDefaultButton(true);  // use Enter in order to press button

        cancelButton.setOnAction(this::onCancelButtonClicked);
        cancelButton.setCancelButton(true);  // use Escape in order to press button
    }

    public HashMap<String, String> getLanguages() {
        HashMap<String, String> languageMap = new HashMap<>();
        for(Languages language : Languages.values()) {
            String labelName = Constants.LANG_LABEL_PREFIX + language.key.toUpperCase();

            try {
                String label = viewLoader.loadLabel(labelName);
                languageMap.put(language.key, label);
            }
            catch (MissingResourceException ex) {
                log.warn("Could not load label for language {}", language.key);
            }
        }
        return languageMap;
    }

    public HashMap<String, String> getNotificationSounds() {
        HashMap<String, String> notificationSoundMap = new HashMap<>();
        for (String path : audioService.getNotificationSoundPaths()) {
            String fileName = audioService.pathToFileName(path);
            notificationSoundMap.put(fileName, fileName.substring(0, fileName.lastIndexOf('.')));
        }
        return notificationSoundMap;
    }

    private void onCancelButtonClicked(ActionEvent actionEvent) {
        this.close();
    }

    private void onApplyButtonClicked(ActionEvent actionEvent) {
        // call change-methods only when there really was a change
        String newLanguage = languageComboBox.getSelection();
        if (! currentLanguage.equals(newLanguage)){
            changeLanguage(newLanguage);
            currentLanguage = newLanguage;
        }
        String newNotificationSoundFile = notificationComboBox.getSelection();
        if (! currentNotificationSoundFile.equals(newNotificationSoundFile)){
            audioService.setNotificationSoundFile(newNotificationSoundFile);
            currentNotificationSoundFile = newNotificationSoundFile;
        }
        int newOutputVolume = (int) outputVolumeSlider.getValue();
        if(currentOutputVolume != newOutputVolume){
            audioService.setVolumePercent(newOutputVolume);
            voiceChatService.setSpeakerVolumePercent(newOutputVolume);
            currentOutputVolume = newOutputVolume;
        }
        final Mixer selectedMicrophone = inputDeviceComboBox.getValue();
        if (!currentMicrophone.equals(selectedMicrophone)) {
            voiceChatService.setSelectedMicrophone(selectedMicrophone);
            currentMicrophone = selectedMicrophone;
        }
        final Mixer selectedSpeaker = outputDeviceComboBox.getValue();
        if (!currentSpeaker.equals(selectedSpeaker)) {
            voiceChatService.setSelectedSpeaker(selectedSpeaker);
            currentSpeaker = selectedSpeaker;
        }

        this.close();
    }

    private void changeLanguage(String newLanguageString) {
        final Languages newLanguage = Languages.fromKeyOrDefault(newLanguageString);
        viewLoader.changeLanguage(newLanguage);
        router.forceReload();

        Platform.runLater(() -> {
            primaryStage.setWidth(primaryStage.getWidth() + 0.1);
            if(primaryStage.isMaximized()) {
                primaryStage.setWidth(Constants.RES_MAIN_SCREEN_WIDTH);
                primaryStage.setHeight(Constants.RES_MAIN_SCREEN_HEIGHT);
                primaryStage.centerOnScreen();
            }
        });
        databaseService.saveAccordSetting(AccordSettingKey.LANGUAGE, newLanguage.key);
    }

    @Override
    public void close() {
        applyButton.setOnAction(null);
        cancelButton.setOnAction(null);
        super.close();
    }

    @AssistedFactory
    public interface SettingsModalFactory {
        SettingsModal create(Parent view);
    }
}
