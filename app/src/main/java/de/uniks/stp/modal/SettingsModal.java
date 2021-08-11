package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXToggleButton;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.AudioService;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.component.AudioDeviceComboBox;
import de.uniks.stp.component.IntegrationButton;
import de.uniks.stp.component.KeyBasedComboBox;
import de.uniks.stp.jpa.AccordSettingKey;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.network.integration.Credentials;
import de.uniks.stp.network.integration.IntegrationService;
import de.uniks.stp.network.integration.Integrations;
import de.uniks.stp.network.integration.authorization.AbstractAuthorizationClient;
import de.uniks.stp.network.integration.authorization.AuthorizationCallback;
import de.uniks.stp.network.voice.VoiceChatService;
import de.uniks.stp.router.Router;
import de.uniks.stp.view.Languages;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.sound.sampled.Mixer;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.Objects;

public class SettingsModal extends AbstractModal {

    public static final String SETTINGS_APPLY_BUTTON = "#settings-apply-button";
    public static final String SETTINGS_CANCEL_BUTTON = "#settings-cancel-button";
    public static final String SETTINGS_COMBO_SELECT_LANGUAGE = "#combo-select-language";
    public static final String SETTINGS_COMBO_SELECT_NOTIFICATION_SOUND = "#combo-select-notification-sound";
    public static final String SETTINGS_COMBO_SELECT_INPUT_DEVICE = "#combo-select-input-device";
    public static final String SETTINGS_COMBO_SELECT_OUTPUT_DEVICE = "#combo-select-output-device";
    public static final String SETTINGS_SLIDER_INPUT_VOLUME = "#slider-input-volume";
    public static final String SETTINGS_SLIDER_OUTPUT_VOLUME = "#slider-output-volume";
    public static final String SETTINGS_INTEGRATION_CONTAINER = "#integration-container";
    public static final String SETTINGS_SLIDER_INPUT_SENSITIVITY = "#slider-input-sensitivity";
    public static final String SETTINGS_PROGRESS_BAR_INPUT_SENSITIVITY = "#progress-bar-input-sensitivity";
    public static final String SETTINGS_INPUT_SENSITIVITY_CONTAINER = "#input-sensitivity-container";
    public static final String SETTINGS_MICROPHONE_TEST_BUTTON = "#input-sensitivity-test-button";

    private final JFXButton applyButton;
    private final JFXButton cancelButton;
    private final JFXButton testMicrophoneButton;
    private final KeyBasedComboBox languageComboBox;
    private final KeyBasedComboBox notificationComboBox;
    private final AudioDeviceComboBox inputDeviceComboBox;
    private final AudioDeviceComboBox outputDeviceComboBox;
    private final JFXSlider inputVolumeSlider;
    private final JFXSlider inputSensitivitySlider;
    private final ProgressBar inputSensitivityBar;
    private final StackPane inputSensitivityContainer;
    private final JFXSlider outputVolumeSlider;
    private static final Logger log = LoggerFactory.getLogger(SettingsModal.class);
    private final ViewLoader viewLoader;
    private final Router router;
    private final SessionDatabaseService databaseService;
    private final AudioService audioService;
    private final VoiceChatService voiceChatService;
    private final Stage primaryStage;
    private final VBox integrationContainer;
    private final IntegrationService integrationService;
    private final IntegrationButton.IntegrationButtonFactory integrationButtonFactory;

    private boolean microphoneTestRunning = false;

    private String currentLanguage;
    private String currentNotificationSoundFile;

    private Mixer currentMicrophone;
    private Mixer currentSpeaker;
    private int currentInputVolume;
    private int currentInputSensitivity;
    private int currentOutputVolume;
    private IntegrationButton spotifyIntegrationButton;
    private IntegrationButton githubIntegrationButton;
    private HashMap<String, IntegrationButton> integrationButtonHashMap;

    @AssistedInject
    public SettingsModal(ViewLoader viewLoader,
                         Router router,
                         @Named("primaryStage") Stage primaryStage,
                         SessionDatabaseService databaseService,
                         AudioService audioService,
                         VoiceChatService voiceChatService,
                         IntegrationService integrationService,
                         IntegrationButton.IntegrationButtonFactory integrationButtonFactory,
                         @Assisted Parent root) {
        super(root, primaryStage);
        this.primaryStage = primaryStage;
        this.viewLoader = viewLoader;
        this.router = router;
        this.databaseService = databaseService;
        this.audioService = audioService;
        this.voiceChatService = voiceChatService;
        this.integrationService = integrationService;
        this.integrationButtonFactory = integrationButtonFactory;
        this.integrationButtonHashMap = new HashMap<>();

        setTitle(viewLoader.loadLabel(Constants.LBL_SELECT_LANGUAGE_TITLE));

        applyButton = (JFXButton) view.lookup(SETTINGS_APPLY_BUTTON);
        cancelButton = (JFXButton) view.lookup(SETTINGS_CANCEL_BUTTON);
        testMicrophoneButton = (JFXButton) view.lookup(SETTINGS_MICROPHONE_TEST_BUTTON);
        integrationContainer = (VBox) view.lookup(SETTINGS_INTEGRATION_CONTAINER);

        languageComboBox = (KeyBasedComboBox) view.lookup(SETTINGS_COMBO_SELECT_LANGUAGE);
        languageComboBox.addOptions(getLanguages());
        currentLanguage = viewLoader.getCurrentLocale().getLanguage();
        languageComboBox.setSelection(currentLanguage);

        notificationComboBox = (KeyBasedComboBox) view.lookup(SETTINGS_COMBO_SELECT_NOTIFICATION_SOUND);
        notificationComboBox.addOptions(getNotificationSounds());
        currentNotificationSoundFile = audioService.getNotificationSoundFileName();
        notificationComboBox.setSelection(currentNotificationSoundFile);

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

        // init volume slider
        inputVolumeSlider = (JFXSlider) view.lookup(SETTINGS_SLIDER_INPUT_VOLUME);
        currentInputVolume = voiceChatService.getInputVolume();
        inputVolumeSlider.setValue(currentInputVolume);

        outputVolumeSlider = (JFXSlider) view.lookup(SETTINGS_SLIDER_OUTPUT_VOLUME);
        currentOutputVolume = voiceChatService.getOutputVolume();
        outputVolumeSlider.setValue(currentOutputVolume);

        // init sensitivity slider
        inputSensitivitySlider = (JFXSlider) view.lookup(SETTINGS_SLIDER_INPUT_SENSITIVITY);
        currentInputSensitivity = voiceChatService.getInputSensitivity();
        inputSensitivitySlider.setValue(currentInputSensitivity);
        inputSensitivitySlider.setLabelFormatter(new StringConverter<>() {
            private final String unit = "db";

            @Override
            public String toString(Double value) {
                return ((int) Math.floor(value)) + unit;
            }

            @Override
            public Double fromString(String value) {
                return Double.parseDouble(value.replaceFirst(unit, ""));
            }
        });
        inputSensitivityBar = (ProgressBar) view.lookup(SETTINGS_PROGRESS_BAR_INPUT_SENSITIVITY);
        inputSensitivityContainer = (StackPane) view.lookup(SETTINGS_INPUT_SENSITIVITY_CONTAINER);

        applyButton.setOnAction(this::onApplyButtonClicked);
        applyButton.setDefaultButton(true);  // use Enter in order to press button

        cancelButton.setOnAction(this::onCancelButtonClicked);
        cancelButton.setCancelButton(true);  // use Escape in order to press button

        testMicrophoneButton.setOnAction(this::onTestMicrophoneButtonClicked);

        //integration buttons
        addIntegrationButtons();

    }

    private void onTestMicrophoneButtonClicked(ActionEvent actionEvent) {
        log.debug("Starting microphone test.");
        String nextButtonActionText;

        if (microphoneTestRunning) {
            // stop test
            voiceChatService.stopMicrophoneTest();
            nextButtonActionText = viewLoader.loadLabel(Constants.LBL_MICROPHONE_TEST_START);
        } else {
            // start test
            voiceChatService.startMicrophoneTest(inputVolumeSlider, outputVolumeSlider, inputSensitivityBar);
            nextButtonActionText = viewLoader.loadLabel(Constants.LBL_MICROPHONE_TEST_STOP);
        }
        microphoneTestRunning = !microphoneTestRunning;
        testMicrophoneButton.setText(nextButtonActionText);
    }

    private void addIntegrationButtons() {
        spotifyIntegrationButton = integrationButtonFactory.create(viewLoader.loadImage("spotify_button.png"));
        spotifyIntegrationButton.setOnButtonClicked((MouseEvent event) -> onIntegrationButtonClicked(spotifyIntegrationButton, Integrations.SPOTIFY.key));
        spotifyIntegrationButton.setOnRemoveClicked((MouseEvent event) -> onRemoveClicked(Integrations.SPOTIFY.key));
        spotifyIntegrationButton.setOnToggleClicked((MouseEvent event) -> onShowInProfileToggleClicked(event, Integrations.SPOTIFY.key));
        integrationContainer.getChildren().add(spotifyIntegrationButton);
        integrationButtonHashMap.put(Integrations.SPOTIFY.key, spotifyIntegrationButton);

        githubIntegrationButton = integrationButtonFactory.create(viewLoader.loadImage("github_button.png"));
        githubIntegrationButton.setOnButtonClicked((MouseEvent event) -> onIntegrationButtonClicked(githubIntegrationButton, Integrations.GITHUB.key));
        githubIntegrationButton.setOnRemoveClicked((MouseEvent event) -> onRemoveClicked(Integrations.GITHUB.key));
        githubIntegrationButton.setOnToggleClicked((MouseEvent event) -> onShowInProfileToggleClicked(event, Integrations.GITHUB.key));
        integrationContainer.getChildren().add(githubIntegrationButton);
        integrationButtonHashMap.put(Integrations.GITHUB.key, githubIntegrationButton);

        for (String serviceName : integrationButtonHashMap.keySet()) {
            if(integrationService.isServiceConnected(serviceName)) {
                IntegrationButton button = integrationButtonHashMap.get(serviceName);
                button.setSuccessMode();
                if(integrationService.isServiceActive(serviceName)) {
                    button.setActive();
                }
            }
        }
    }

    private void onRemoveClicked(String serviceName) {
        integrationService.removeService(serviceName);
        integrationButtonHashMap.get(serviceName).setNormalMode();
    }

    private void onShowInProfileToggleClicked(MouseEvent event, String serviceName) {
        JFXToggleButton source = (JFXToggleButton) event.getSource();
        if(source.isSelected()) {
            stopOtherService(serviceName);
            //start selected service
            integrationService.restartService(serviceName);
        }
        else {
            integrationService.stopService(serviceName);
        }
    }

    private void stopOtherService(String serviceToActivate) {
        for (String serviceName : integrationButtonHashMap.keySet()) {
            if(!serviceName.equals(serviceToActivate) && integrationService.isServiceActive(serviceName)) {
                integrationService.stopService(serviceName);
                integrationButtonHashMap.get(serviceName).setInactive();
            }
        }
    }

    private void onIntegrationButtonClicked(IntegrationButton button, String serviceName) {
        AbstractAuthorizationClient authorizationClient = integrationService.getAuthorizationClient(serviceName);

        if(Objects.nonNull(authorizationClient)) {
            authorizationClient.authorize(new AuthorizationCallback() {
                @Override
                public void onSuccess(Credentials credentials) {
                    Platform.runLater(() -> {
                        stopOtherService(serviceName);
                        integrationService.startService(serviceName, credentials);
                        button.setSuccessMode();
                        button.setActive();
                    });
                }

                @Override
                public void onFailure(String errorMessage) {
                    button.setErrorMode();
                }
            });
        }
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
        final String newLanguage = languageComboBox.getSelection();
        if (Objects.nonNull(newLanguage) && !newLanguage.equals(currentLanguage)){
            changeLanguage(newLanguage);
            currentLanguage = newLanguage;
        }
        final String newNotificationSoundFile = notificationComboBox.getSelection();
        if (Objects.nonNull(newNotificationSoundFile) && !newNotificationSoundFile.equals(currentNotificationSoundFile)) {
            audioService.setNotificationSoundFile(newNotificationSoundFile);
            currentNotificationSoundFile = newNotificationSoundFile;
        }

        final Mixer selectedMicrophone = inputDeviceComboBox.getValue();
        if (Objects.nonNull(selectedMicrophone) && !selectedMicrophone.equals(currentMicrophone)) {
            voiceChatService.setSelectedMicrophone(selectedMicrophone);
            currentMicrophone = selectedMicrophone;
        }
        final Mixer selectedSpeaker = outputDeviceComboBox.getValue();
        if (Objects.nonNull(selectedSpeaker) && !selectedSpeaker.equals(currentSpeaker)) {
            voiceChatService.setSelectedSpeaker(selectedSpeaker);
            currentSpeaker = selectedSpeaker;
        }

        final int newInputVolume = (int) inputVolumeSlider.getValue();
        if(currentInputVolume != newInputVolume){
            voiceChatService.setInputVolume(newInputVolume);
            currentInputVolume = newInputVolume;
        }
        final int newOutputVolume = (int) outputVolumeSlider.getValue();
        if(currentOutputVolume != newOutputVolume){
            voiceChatService.setOutputVolume(newOutputVolume);
            currentOutputVolume = newOutputVolume;
        }
        final int newInputSensitivity = (int) inputSensitivitySlider.getValue(); // same as discord from -100db to 0db
        if(currentInputSensitivity != newInputSensitivity){
            voiceChatService.setInputSensitivity(newInputSensitivity);
            currentInputSensitivity = newInputSensitivity;
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
        testMicrophoneButton.setOnAction(null);
        if (microphoneTestRunning) {
            voiceChatService.stopMicrophoneTest();
        }
        super.close();
    }

    @AssistedFactory
    public interface SettingsModalFactory {
        SettingsModal create(Parent view);
    }
}
