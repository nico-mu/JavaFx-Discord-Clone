package de.uniks.stp.language;

import de.uniks.stp.Editor;
import de.uniks.stp.StageManager;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.jpa.AccordSettingKey;
import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.jpa.model.AccordSettingDTO;
import de.uniks.stp.model.Accord;
import de.uniks.stp.router.Router;
import de.uniks.stp.view.Languages;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

public class LanguageService {
    private final Editor editor;
    private final PropertyChangeListener languagePropertyChangeListener = this::onLanguagePropertyChange;

    public LanguageService(Editor editor) {
        this.editor = editor;
    }

    public void startLanguageAwareness() {
        final AccordSettingDTO accordLanguageSetting = DatabaseService.getAccordSetting(AccordSettingKey.LANGUAGE);
        final Languages language = Objects.nonNull(accordLanguageSetting) ?
            Languages.fromKeyOrDefault(accordLanguageSetting.getValue()) : Languages.getDefault();

        final Accord accord = editor.getOrCreateAccord();
        accord.setLanguage(language.key);
        ViewLoader.changeLanguage(language);
        accord.listeners().addPropertyChangeListener(Accord.PROPERTY_LANGUAGE, languagePropertyChangeListener);
    }

    public void stopLanguageAwareness() {
        final Accord accord = editor.getOrCreateAccord();
        accord.listeners().removePropertyChangeListener(languagePropertyChangeListener);
    }

    private void onLanguagePropertyChange(final PropertyChangeEvent languageChangeEvent) {
        final Languages newLanguage = Languages.fromKeyOrDefault((String) languageChangeEvent.getNewValue());
        ViewLoader.changeLanguage(newLanguage);
        Router.forceReload();

        Stage stage = StageManager.getStage();
        double width = stage.getWidth();
        Platform.runLater(() -> {
            stage.setWidth(width + 0.1);
            if(stage.isMaximized()) {
                stage.setWidth(1300);
                stage.setHeight(750);
                stage.centerOnScreen();
            }
        });

        DatabaseService.saveAccordSetting(AccordSettingKey.LANGUAGE, newLanguage.key);
    }
}
