package de.uniks.stp.network.voice;

import de.uniks.stp.Constants;
import de.uniks.stp.model.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VoiceChatService {
    private static final Logger log = LoggerFactory.getLogger(VoiceChatClient.class);
    private static final AudioFormat audioFormat = getAudioFormat();

    public Mixer getSelectedMicrophone() {
        return selectedMicrophone;
    }

    public void setSelectedMicrophone(Mixer selectedMicrophone) {
        this.selectedMicrophone = selectedMicrophone;
    }

    public Mixer getSelectedSpeaker() {
        return selectedSpeaker;
    }

    public void setSelectedSpeaker(Mixer selectedSpeaker) {
        this.selectedSpeaker = selectedSpeaker;
    }

    public List<Mixer> getAvailableSpeakers() {
        return availableSpeakers;
    }

    public List<Mixer> getAvailableMicrophones() {
        return availableMicrophones;
    }

    private static AudioFormat getAudioFormat() {
        return new AudioFormat(
            Constants.AUDIOSTREAM_SAMPLE_RATE,
            Constants.AUDIOSTREAM_SAMPLE_SIZE_BITS,
            Constants.AUDIOSTREAM_CHANNEL,
            Constants.AUDIOSTREAM_SIGNED,
            Constants.AUDIOSTREAM_BIG_ENDIAN
        );
    }

    private final VoiceChatClientFactory voiceChatClientFactory;

    private Mixer selectedMicrophone;
    private Mixer selectedSpeaker;

    private final List<Mixer> availableSpeakers = new ArrayList<>();
    private final List<Mixer> availableMicrophones = new ArrayList<>();
    private VoiceChatClient voiceChatClient;


    public VoiceChatService(VoiceChatClientFactory voiceChatClientFactory) {
        this.voiceChatClientFactory = voiceChatClientFactory;
        for (final Mixer.Info info : AudioSystem.getMixerInfo()) {
            final Mixer mixer = AudioSystem.getMixer(info);
            final DataLine.Info audioOut = new DataLine.Info(SourceDataLine.class, audioFormat);
            if (mixer.isLineSupported(audioOut)) {
                availableSpeakers.add(mixer);
                log.debug("Found speaker: {}", mixer.getMixerInfo());
            }
            final DataLine.Info audioIn = new DataLine.Info(TargetDataLine.class, audioFormat);
            if (mixer.isLineSupported(audioIn)) {
                availableMicrophones.add(mixer);
                log.debug("Found microphone: {}", mixer.getMixerInfo());
            }
        }
        if (isMicrophoneAvailable()) {
            // TODO: TG21-187 select Input Device
            selectedMicrophone = availableMicrophones.get(0);
        }
        if (isSpeakerAvailable()) {
            // TODO: TG21-188 select Output Device
            selectedSpeaker = availableSpeakers.get(0);
        }
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
    }

    public void removeVoiceChatClient(Channel model) {
        if (Objects.nonNull(voiceChatClient)) {
            voiceChatClient.stop();
        }
    }
}
