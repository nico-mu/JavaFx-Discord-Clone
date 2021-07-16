package de.uniks.stp.network.voice;

import de.uniks.stp.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;

public class VoiceChatService {
    private static final Logger log = LoggerFactory.getLogger(VoiceChatClient.class);
    private static final AudioFormat audioFormat = getAudioFormat();

    private static AudioFormat getAudioFormat() {
        return new AudioFormat(
            Constants.AUDIOSTREAM_SAMPLE_RATE,
            Constants.AUDIOSTREAM_SAMPLE_SIZE_BITS,
            Constants.AUDIOSTREAM_CHANNEL,
            Constants.AUDIOSTREAM_SIGNED,
            Constants.AUDIOSTREAM_BIG_ENDIAN
        );
    }

    private final List<Mixer> availableSpeakers = new ArrayList<>();
    private final List<Mixer> availableMicrophones = new ArrayList<>();


    public VoiceChatService() {
        for (final Mixer.Info info : AudioSystem.getMixerInfo()) {
            final Mixer mixer = AudioSystem.getMixer(info);
            if (mixer.isLineSupported(Port.Info.SPEAKER)) {
                availableSpeakers.add(mixer);
                log.debug("Found speaker: {}", mixer.getMixerInfo().getDescription());
            }
            if (mixer.isLineSupported(Port.Info.MICROPHONE)) {
                availableMicrophones.add(mixer);
                log.debug("Found microphone: {}", mixer.getMixerInfo().getDescription());
            }
        }

    }

    public boolean isAudioOutAvailable() {
        return !availableSpeakers.isEmpty();
    }

    public boolean isAudioInAvailable() {
        return !availableMicrophones.isEmpty();
    }
}
