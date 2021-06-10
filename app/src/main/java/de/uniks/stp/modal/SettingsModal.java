package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.*;
import de.uniks.stp.component.KeyBasedComboBox;
import de.uniks.stp.view.Languages;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.MissingResourceException;

public class SettingsModal extends AbstractModal {

    public static final String SETTINGS_APPLY_BUTTON = "#settings-apply-button";
    public static final String SETTINGS_CANCEL_BUTTON = "#settings-cancel-button";
    public static final String SETTINGS_COMBO_SELECT_LANGUAGE = "#combo-select-language";
    public static final String SETTINGS_COMBO_SELECT_NOTIFICATION_SOUND = "#combo-select-notification-sound";
    private final JFXButton applyButton;
    private final JFXButton cancelButton;
    private final KeyBasedComboBox languageComboBox;
    private final KeyBasedComboBox notificationComboBox;
    private final Editor editor;
    private static final Logger log = LoggerFactory.getLogger(SettingsModal.class);

    public SettingsModal(Parent root, Editor editor) {
        super(root);

        setTitle(ViewLoader.loadLabel(Constants.LBL_SELECT_LANGUAGE_TITLE));

        this.editor = editor;
        applyButton = (JFXButton) view.lookup(SETTINGS_APPLY_BUTTON);
        cancelButton = (JFXButton) view.lookup(SETTINGS_CANCEL_BUTTON);

        languageComboBox = (KeyBasedComboBox) view.lookup(SETTINGS_COMBO_SELECT_LANGUAGE);

        languageComboBox.addOptions(getLanguages());
        languageComboBox.setSelection(editor.getOrCreateAccord().getLanguage());

        notificationComboBox = (KeyBasedComboBox) view.lookup(SETTINGS_COMBO_SELECT_NOTIFICATION_SOUND);

        notificationComboBox.addOptions(getNotificationSounds());
        notificationComboBox.setSelection(AudioService.getNotificationSoundFileName());

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
                String label = ViewLoader.loadLabel(labelName);
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
        File[] files = AudioService.getNotificationSoundFiles();
        for (File file : files) {
            if (file.isFile()) {
                notificationSoundMap.put(file.getName(), file.getName().substring(0, file.getName().lastIndexOf('.')));
            }
        }
        return notificationSoundMap;
    }

    private void onCancelButtonClicked(ActionEvent actionEvent) {
        this.close();
    }

    private void onApplyButtonClicked(ActionEvent actionEvent) {
        editor.getOrCreateAccord().setLanguage(languageComboBox.getSelection());
        StageManager.getAudioService().setNotificationSoundFile(notificationComboBox.getSelection());
        this.close();
    }

    @Override
    public void close() {
        applyButton.setOnAction(null);
        cancelButton.setOnAction(null);
        super.close();
    }
}
