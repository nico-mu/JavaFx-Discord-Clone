package de.uniks.stp.network.voice;

import de.uniks.stp.jpa.AccordSettingKey;
import de.uniks.stp.jpa.AppDatabaseService;
import de.uniks.stp.jpa.model.AccordSettingDTO;
import de.uniks.stp.model.Channel;
import de.uniks.stp.util.VoiceChatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VoiceChatService {
    private static final Logger log = LoggerFactory.getLogger(VoiceChatService.class);

    private final VoiceChatClientFactory voiceChatClientFactory;
    private final AppDatabaseService databaseService;

    private Mixer selectedMicrophone;
    private Mixer selectedSpeaker;

    private final List<Mixer> availableSpeakers = new ArrayList<>();
    private final List<Mixer> availableMicrophones = new ArrayList<>();
    private VoiceChatClient voiceChatClient;

    private int inputVolume;
    private int outputVolume;


    public VoiceChatService(VoiceChatClientFactory voiceChatClientFactory, AppDatabaseService databaseService) {
        this.voiceChatClientFactory = voiceChatClientFactory;
        this.databaseService = databaseService;
        for (final Mixer mixer : VoiceChatUtil.getMixers()) {
            final DataLine.Info audioOut = new DataLine.Info(SourceDataLine.class, VoiceChatUtil.AUDIO_FORMAT);
            if (mixer.isLineSupported(audioOut)) {
                availableSpeakers.add(mixer);
                log.debug("Found speaker:\n{}", VoiceChatUtil.getMixerHierarchyInfo(mixer));
            }
            final DataLine.Info audioIn = new DataLine.Info(TargetDataLine.class, VoiceChatUtil.AUDIO_FORMAT);
            if (mixer.isLineSupported(audioIn)) {
                availableMicrophones.add(mixer);
                log.debug("Found microphone:\n{}", VoiceChatUtil.getMixerHierarchyInfo(mixer));
            }
        }
        if (isMicrophoneAvailable()) {
            final AccordSettingDTO settingDTO = databaseService.getAccordSetting(AccordSettingKey.AUDIO_IN_DEVICE);
            Mixer preferredMicrophone = availableMicrophones.get(0); // use default if none match

            if (Objects.nonNull(settingDTO)) {
                final String prefMixerInfoString = settingDTO.getValue();
                for(Mixer mixer: availableMicrophones) {
                    if (persistenceString(mixer).equals(prefMixerInfoString)) {
                        preferredMicrophone = mixer;
                        break;
                    }
                }
            }
            selectedMicrophone = preferredMicrophone;
        }
        if (isSpeakerAvailable()) {
            final AccordSettingDTO settingDTO = databaseService.getAccordSetting(AccordSettingKey.AUDIO_OUT_DEVICE);
            Mixer preferredSpeaker = availableSpeakers.get(0); // use default if none match

            if (Objects.nonNull(settingDTO)) {
                final String prefMixerInfoString = settingDTO.getValue();
                for(Mixer mixer: availableSpeakers) {
                    if (persistenceString(mixer).equals(prefMixerInfoString)) {
                        preferredSpeaker = mixer;
                        break;
                    }
                }
            }
            selectedSpeaker = preferredSpeaker;
        }

        // get audio out volume value
        AccordSettingDTO volumeOutSettings = databaseService.getAccordSetting(AccordSettingKey.AUDIO_IN_VOLUME);
        if (Objects.nonNull(volumeOutSettings)) {
            this.inputVolume = Integer.parseInt(volumeOutSettings.getValue());
        } else {
            this.inputVolume = 100;
        }
        // get audio out volume value
        AccordSettingDTO volumeInSettings = databaseService.getAccordSetting(AccordSettingKey.AUDIO_OUT_VOLUME);
        if (Objects.nonNull(volumeOutSettings)) {
            this.outputVolume = Integer.parseInt(volumeInSettings.getValue());
        } else {
            this.outputVolume = 100;
        }
    }

    private static String persistenceString(Mixer mixer) {
        return mixer.getMixerInfo().toString();
    }

    public Mixer getSelectedMicrophone() {
        return selectedMicrophone;
    }

    public void setSelectedMicrophone(Mixer selectedMicrophone) {
        if (!this.selectedMicrophone.equals(selectedMicrophone)) {
            if (Objects.nonNull(voiceChatClient)) {
                voiceChatClient.changeMicrophone(selectedMicrophone);
            }
            this.selectedMicrophone = selectedMicrophone;
            databaseService.saveAccordSetting(AccordSettingKey.AUDIO_IN_DEVICE, persistenceString(selectedMicrophone));
        }
    }

    public Mixer getSelectedSpeaker() {
        return selectedSpeaker;
    }

    public void setSelectedSpeaker(Mixer selectedSpeaker) {
        if (!this.selectedSpeaker.equals(selectedSpeaker)) {
            if (Objects.nonNull(voiceChatClient)) {
                voiceChatClient.changeSpeaker(selectedSpeaker);
            }
            this.selectedSpeaker = selectedSpeaker;
            databaseService.saveAccordSetting(AccordSettingKey.AUDIO_OUT_DEVICE, persistenceString(selectedSpeaker));
        }
    }

    public List<Mixer> getAvailableSpeakers() {
        return availableSpeakers;
    }

    public List<Mixer> getAvailableMicrophones() {
        return availableMicrophones;
    }

    public boolean isSpeakerAvailable() {
        return !availableSpeakers.isEmpty();
    }

    public boolean isMicrophoneAvailable() {
        return !availableMicrophones.isEmpty();
    }

    public void addVoiceChatClient(Channel model) {
        if (Objects.nonNull(voiceChatClient)) {
            voiceChatClient.stop();
        }
        voiceChatClient = voiceChatClientFactory.create(model, selectedSpeaker, selectedMicrophone);
        voiceChatClient.init();
        voiceChatClient.setInputVolume(inputVolume);
        voiceChatClient.setOutputVolume(outputVolume);
    }

    public void removeVoiceChatClient(Channel model) {
        if (Objects.nonNull(voiceChatClient)) {
            voiceChatClient.stop();
        }
    }

    public void setInputVolume(int newInputVolume) {
        this.inputVolume = newInputVolume;
        databaseService.saveAccordSetting(AccordSettingKey.AUDIO_IN_VOLUME, String.valueOf(newInputVolume));
        if (Objects.nonNull(voiceChatClient)){
            voiceChatClient.setInputVolume(newInputVolume);
        }
    }

    public int getInputVolume() {
        return inputVolume;
    }

    public void setOutputVolume(int newOutputVolume) {
        this.outputVolume = newOutputVolume;
        databaseService.saveAccordSetting(AccordSettingKey.AUDIO_OUT_VOLUME, String.valueOf(newOutputVolume));
        if (Objects.nonNull(voiceChatClient)){
            voiceChatClient.setOutputVolume(newOutputVolume);
        }
    }

    public int getOutputVolume() {
        return outputVolume;
    }
}
