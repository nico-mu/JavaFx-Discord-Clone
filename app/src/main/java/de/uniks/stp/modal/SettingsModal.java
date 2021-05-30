package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.component.KeyBasedComboBox;
import de.uniks.stp.view.Languages;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.MissingResourceException;

public class SettingsModal extends AbstractModal {

    public static final String SETTINGS_APPLY_BUTTON = "#settings-apply-button";
    public static final String SETTINGS_CANCEL_BUTTON = "#settings-cancel-button";
    public static final String SETTINGS_COMBO_SELECT_LANGUAGE = "#combo-select-language";
    private final JFXButton applyButton;
    private final JFXButton cancelButton;
    private final KeyBasedComboBox comboBox;
    private final Editor editor;
    private static final Logger log = LoggerFactory.getLogger(SettingsModal.class);

    public SettingsModal(Parent root, Editor editor) {
        super(root);

        setTitle(ViewLoader.loadLabel(Constants.LBL_SELECT_LANGUAGE_TITLE));

        this.editor = editor;
        applyButton = (JFXButton) view.lookup(SETTINGS_APPLY_BUTTON);
        cancelButton = (JFXButton) view.lookup(SETTINGS_CANCEL_BUTTON);

        comboBox = (KeyBasedComboBox) view.lookup(SETTINGS_COMBO_SELECT_LANGUAGE);

        comboBox.addOptions(getLanguages());
        comboBox.setSelection(editor.getOrCreateAccord().getLanguage());

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

    private void onCancelButtonClicked(ActionEvent actionEvent) {
        this.close();
    }

    private void onApplyButtonClicked(ActionEvent actionEvent) {
        editor.getOrCreateAccord().setLanguage(comboBox.getSelection());
        this.close();
    }

    @Override
    public void close() {
        applyButton.setOnAction(null);
        cancelButton.setOnAction(null);
        super.close();
    }
}
