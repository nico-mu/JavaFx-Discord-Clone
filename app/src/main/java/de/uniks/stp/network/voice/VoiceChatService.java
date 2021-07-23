package de.uniks.stp.network.voice;

import de.uniks.stp.model.Channel;
import de.uniks.stp.util.VoiceChatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VoiceChatService {
    private static final Logger log = LoggerFactory.getLogger(VoiceChatClient.class);

    private final VoiceChatClientFactory voiceChatClientFactory;

    private Mixer selectedMicrophone;
    private Mixer selectedSpeaker;

    private final List<Mixer> availableSpeakers = new ArrayList<>();
    private final List<Mixer> availableMicrophones = new ArrayList<>();
    private VoiceChatClient voiceChatClient;


    public VoiceChatService(VoiceChatClientFactory voiceChatClientFactory) {
        this.voiceChatClientFactory = voiceChatClientFactory;

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
