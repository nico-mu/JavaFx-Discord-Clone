package de.uniks.stp.network.voice;

import de.uniks.stp.Constants;
import de.uniks.stp.jpa.AccordSettingKey;
import de.uniks.stp.jpa.AppDatabaseService;
import de.uniks.stp.jpa.model.AccordSettingDTO;
import de.uniks.stp.model.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VoiceChatService {
    private static final Logger log = LoggerFactory.getLogger(VoiceChatService.class);
    private final AudioFormat audioFormat = new AudioFormat(
        Constants.AUDIOSTREAM_SAMPLE_RATE,
        Constants.AUDIOSTREAM_SAMPLE_SIZE_BITS,
        Constants.AUDIOSTREAM_CHANNEL,
        Constants.AUDIOSTREAM_SIGNED,
        Constants.AUDIOSTREAM_BIG_ENDIAN
    );

    private final VoiceChatClientFactory voiceChatClientFactory;
    private final AppDatabaseService databaseService;
    private final List<Mixer> availableSpeakers = new ArrayList<>();
    private final List<Mixer> availableMicrophones = new ArrayList<>();
    private Mixer selectedMicrophone;
    private Mixer selectedSpeaker;
    private VoiceChatClient voiceChatClient;

    private int inputVolume;
    private int outputVolume;


    public VoiceChatService(VoiceChatClientFactory voiceChatClientFactory, AppDatabaseService databaseService) {
        this.voiceChatClientFactory = voiceChatClientFactory;
        this.databaseService = databaseService;
        for (final Mixer mixer : getMixers()) {
            final DataLine.Info audioOut = new DataLine.Info(SourceDataLine.class, audioFormat);
            if (mixer.isLineSupported(audioOut)) {
                availableSpeakers.add(mixer);
            }
            final DataLine.Info audioIn = new DataLine.Info(TargetDataLine.class, audioFormat);
            if (mixer.isLineSupported(audioIn)) {
                availableMicrophones.add(mixer);
            }
        }
        if (isMicrophoneAvailable()) {
            final AccordSettingDTO settingDTO = databaseService.getAccordSetting(AccordSettingKey.AUDIO_IN_DEVICE);
            Mixer preferredMicrophone = availableMicrophones.get(0); // use default if none match

            if (Objects.nonNull(settingDTO)) {
                final String prefMixerInfoString = settingDTO.getValue();
                for (Mixer mixer : availableMicrophones) {
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
                for (Mixer mixer : availableSpeakers) {
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

    public List<Mixer> getMixers() {
        Mixer.Info[] infos = AudioSystem.getMixerInfo();
        List<Mixer> mixers = new ArrayList<>(infos.length);
        for (Mixer.Info info : infos) {
            Mixer mixer = AudioSystem.getMixer(info);
            mixers.add(mixer);
        }
        return mixers;
    }

    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    public byte[] adjustVolume(int volume, byte[] audioBuf) {
        /* Do not change anything if volume is not withing acceptable range of 0 - 100.
         Notice that a volume of 100 would not change anything and just return the buffer as is */
        if (volume < 0 || volume >= 100) {
            return audioBuf;
        }
        final double vol = Math.pow(volume / 100d, 2);  //for better volume scaling
        final ByteBuffer wrap = ByteBuffer.wrap(audioBuf).order(ByteOrder.LITTLE_ENDIAN);
        final ByteBuffer dest = ByteBuffer.allocate(audioBuf.length).order(ByteOrder.LITTLE_ENDIAN);

        // Copy metadata
        for (int i = 0; i < Constants.AUDIOSTREAM_METADATA_BUFFER_SIZE; i++) {
            dest.put(wrap.get());
        }

        // PCM
        while (wrap.hasRemaining()) {
            short temp = wrap.getShort();
            temp *= vol;

            byte b1 = (byte) (temp & 0xff);
            byte b2 = (byte) ((temp >> 8) & 0xff);

            dest.put(b1);
            dest.put(b2);
        }
        return dest.array();
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
        voiceChatClient = voiceChatClientFactory.create(this, model, selectedSpeaker, selectedMicrophone);
        voiceChatClient.init();
        voiceChatClient.setInputVolume(inputVolume);
        voiceChatClient.setOutputVolume(outputVolume);
    }

    public void removeVoiceChatClient(Channel model) {
        if (Objects.nonNull(voiceChatClient)) {
            voiceChatClient.stop();
        }
    }

    public int getInputVolume() {
        return inputVolume;
    }

    public void setInputVolume(int newInputVolume) {
        this.inputVolume = newInputVolume;
        databaseService.saveAccordSetting(AccordSettingKey.AUDIO_IN_VOLUME, String.valueOf(newInputVolume));
        if (Objects.nonNull(voiceChatClient)) {
            voiceChatClient.setInputVolume(newInputVolume);
        }
    }

    public int getOutputVolume() {
        return outputVolume;
    }

    public void setOutputVolume(int newOutputVolume) {
        this.outputVolume = newOutputVolume;
        databaseService.saveAccordSetting(AccordSettingKey.AUDIO_OUT_VOLUME, String.valueOf(newOutputVolume));
        if (Objects.nonNull(voiceChatClient)) {
            voiceChatClient.setOutputVolume(newOutputVolume);
        }
    }
}
