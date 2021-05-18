package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

public class SettingsModal extends AbstractModal<VBox> {

    public static final String SETTINGS_APPLY_BUTTON = "#settings-apply-button";
    public static final String SETTINGS_CANCEL_BUTTON = "#settings-cancel-button";
    public static final String SETTINGS_COMBO_SELECT_LANGUAGE = "#combo-select-language";
    private final JFXButton applyButton;
    private final JFXButton cancelButton;
    private final JFXComboBox<String> comboBox;
    private Editor editor;

    public SettingsModal(Parent root, Editor editor) {
        super(root);

        this.editor = editor;
        applyButton = (JFXButton) view.lookup(SETTINGS_APPLY_BUTTON);
        cancelButton = (JFXButton) view.lookup(SETTINGS_CANCEL_BUTTON);

        comboBox = (JFXComboBox<String>) view.lookup(SETTINGS_COMBO_SELECT_LANGUAGE);
        String english = ViewLoader.loadLabel(Constants.LANG_EN);
        String german = ViewLoader.loadLabel(Constants.LANG_DE);
        comboBox.getItems().addAll(german, english);

        applyButton.setOnAction(this::onApplyButtonClicked);
        cancelButton.setOnAction(this::onCancelButtonClicked);
    }

    private void onCancelButtonClicked(ActionEvent actionEvent) {
        this.close();
    }

    private void onApplyButtonClicked(ActionEvent actionEvent) {
        this.close();
    }

    @Override
    public void close() {
        applyButton.setOnAction(null);
        cancelButton.setOnAction(null);
        super.close();
    }
}
