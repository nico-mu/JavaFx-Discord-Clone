package de.uniks.stp.network.voice;

import com.jfoenix.controls.JFXSlider;
import de.uniks.stp.Constants;
import de.uniks.stp.jpa.AccordSettingKey;
import de.uniks.stp.jpa.AppDatabaseService;
import de.uniks.stp.jpa.model.AccordSettingDTO;
import de.uniks.stp.model.Channel;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VoiceChatService {
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
    private int inputSensitivity;
    private int outputVolume;
    private ExecutorService executorService;
    private boolean microphoneTestRunning = false;


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
        final AccordSettingDTO volumeInSettings = databaseService.getAccordSetting(AccordSettingKey.AUDIO_IN_VOLUME);
        if (Objects.nonNull(volumeInSettings)) {
            this.inputVolume = Integer.parseInt(volumeInSettings.getValue());
        } else {
            this.inputVolume = 100;
        }
        // get audio in sensitivity value
        final AccordSettingDTO sensitivityInSettings = databaseService.getAccordSetting(AccordSettingKey.AUDIO_IN_SENSITIVITY);
        if (Objects.nonNull(sensitivityInSettings)) {
            this.inputSensitivity = Integer.parseInt(sensitivityInSettings.getValue());
        } else {
            this.inputSensitivity = -85;
        }
        // get audio out volume value
        final AccordSettingDTO volumeOutSettings = databaseService.getAccordSetting(AccordSettingKey.AUDIO_OUT_VOLUME);
        if (Objects.nonNull(volumeOutSettings)) {
            this.outputVolume = Integer.parseInt(volumeOutSettings.getValue());
        } else {
            this.outputVolume = 100;
        }
    }

    public String persistenceString(Mixer mixer) {
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
        return this.adjustVolume(volume, audioBuf, Constants.AUDIOSTREAM_METADATA_BUFFER_SIZE);
    }

    private byte[] adjustVolume(int volume, byte[] audioBuf, int metadataSize) {
        /* Do not change anything if volume is not withing acceptable range of 0 - 100.
         Notice that a volume of 100 would not change anything and just return the buffer as is */
        if (volume < 0 || volume >= 100) {
            return audioBuf;
        }
        final double vol = Math.pow(volume / 100d, 2);  //for better volume scaling
        final ByteBuffer wrap = ByteBuffer.wrap(audioBuf).order(ByteOrder.LITTLE_ENDIAN);
        final ByteBuffer dest = ByteBuffer.allocate(audioBuf.length).order(ByteOrder.LITTLE_ENDIAN);

        // Copy metadata
        for (int i = 0; i < metadataSize; i++) {
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
        if (Objects.isNull(this.selectedMicrophone) || !this.selectedMicrophone.equals(selectedMicrophone)) {
            this.selectedMicrophone = selectedMicrophone;
            if (Objects.nonNull(voiceChatClient)) {
                voiceChatClient.onMicrophoneChanged();
            }
            databaseService.saveAccordSetting(AccordSettingKey.AUDIO_IN_DEVICE, persistenceString(selectedMicrophone));
        }
    }

    public Mixer getSelectedSpeaker() {
        return selectedSpeaker;
    }

    public void setSelectedSpeaker(Mixer selectedSpeaker) {
        if (Objects.isNull(this.selectedSpeaker) || !this.selectedSpeaker.equals(selectedSpeaker)) {
            this.selectedSpeaker = selectedSpeaker;
            if (Objects.nonNull(voiceChatClient)) {
                voiceChatClient.onSpeakerChanged();
            }
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
        voiceChatClient = voiceChatClientFactory.create(this, model);
        voiceChatClient.init();
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
    }

    public int getInputSensitivity() {
        return inputSensitivity;
    }

    public void setInputSensitivity(int newInputSensitivity) {
        this.inputSensitivity = newInputSensitivity;
        databaseService.saveAccordSetting(AccordSettingKey.AUDIO_IN_SENSITIVITY, String.valueOf(newInputSensitivity));
    }

    public int getOutputVolume() {
        return outputVolume;
    }

    public void setOutputVolume(int newOutputVolume) {
        this.outputVolume = newOutputVolume;
        databaseService.saveAccordSetting(AccordSettingKey.AUDIO_OUT_VOLUME, String.valueOf(newOutputVolume));
    }

    public void startMicrophoneTest(JFXSlider inputVolumeSlider, JFXSlider outputVolumeSlider, ProgressBar inputSensitivityBar) {
        if (Objects.isNull(executorService)) {
            executorService = Executors.newCachedThreadPool();
        }
        microphoneTestRunning = true;
        executorService.execute(() -> {
            TargetDataLine audioInDataLine = null;
            SourceDataLine audioOutDataLine = null;
            try {
                audioInDataLine = createUsableTargetDataLine();
                audioOutDataLine = createUsableSourceDataLine();

                byte[] audioBuf = new byte[Constants.AUDIOSTREAM_AUDIO_BUFFER_SIZE];
                final int metadataSize = 0;
                while (microphoneTestRunning) {
                    if (Objects.nonNull(audioInDataLine)) {
                        audioInDataLine.read(audioBuf, metadataSize, Constants.AUDIOSTREAM_AUDIO_BUFFER_SIZE);

                        if (Objects.nonNull(audioOutDataLine)) {
                            // adjust volume once by combining in- and output volume
                            final int inputVolume = (int) inputVolumeSlider.getValue();
                            final int outputVolume = (int) outputVolumeSlider.getValue();
                            int volume = inputVolume * outputVolume / 100;
                            byte[] adjustedAudioBuf = adjustVolume(volume, audioBuf, metadataSize);
                            audioOutDataLine.write(adjustedAudioBuf, metadataSize, Constants.AUDIOSTREAM_AUDIO_BUFFER_SIZE);

                            volume = inputVolume;
                            adjustedAudioBuf = adjustVolume(volume, audioBuf, metadataSize);
                            final int decibel = calculateDecibel(adjustedAudioBuf, metadataSize);
                            Platform.runLater(() -> inputSensitivityBar.setProgress((decibel+100)/100d));
                        }
                    }
                }
            } catch (LineUnavailableException ignored) { }
            finally {
                // Clean up
                stopDataLine(audioInDataLine);
                stopDataLine(audioOutDataLine);
                Platform.runLater(() -> inputSensitivityBar.setProgress(0d));
            }
        });
    }

    /**
     * Calculates the decibel value for the given sample
     *
     * @param audioBuf the sample
     * @return the decibel value
     */
    public int calculateDecibel(byte[] audioBuf) {
        return calculateDecibel(audioBuf, Constants.AUDIOSTREAM_METADATA_BUFFER_SIZE);
    }

    private int calculateDecibel(byte[] audioBuf, int metadataSize) {
        final ByteBuffer wrap = ByteBuffer.wrap(audioBuf).order(ByteOrder.LITTLE_ENDIAN);
        // Skip metadata
        for (int i = 0; i < metadataSize; i++) {
            wrap.get();
        }

        int samplesAmount = 0;
        double sumOfSampleSq = 0d;    // sum of square of normalized samples.
        while (wrap.hasRemaining()) {
            final float normSample = wrap.getShort() / 32767f;  // normalized the sample with maximum value.
            sumOfSampleSq += (normSample * normSample);
            samplesAmount++;
        }

        return (int) (10*Math.log10(sumOfSampleSq / samplesAmount));
    }

    public void stopMicrophoneTest() {
        microphoneTestRunning = false;
        if (Objects.nonNull(executorService)) {
            executorService.shutdown();
            executorService = null;
        }
    }

    public void stopDataLine(DataLine dataLineToBeClosed) {
        if (Objects.nonNull(dataLineToBeClosed)) {
            dataLineToBeClosed.stop();
            dataLineToBeClosed.drain();
            dataLineToBeClosed.close();
        }
    }

    public TargetDataLine createUsableTargetDataLine() throws LineUnavailableException {
        if (isMicrophoneAvailable()) {
            final DataLine.Info infoIn = new DataLine.Info(TargetDataLine.class, getAudioFormat());
            final TargetDataLine dataLine = (TargetDataLine) getSelectedMicrophone().getLine(infoIn);
            dataLine.open(getAudioFormat());
            dataLine.start();
            return dataLine;
        }
        return null;
    }

    public SourceDataLine createUsableSourceDataLine() throws LineUnavailableException {
        if (isSpeakerAvailable()) {
            final DataLine.Info infoIn = new DataLine.Info(SourceDataLine.class, getAudioFormat());
            final SourceDataLine dataLine = (SourceDataLine) getSelectedSpeaker().getLine(infoIn);
            dataLine.open(getAudioFormat());
            dataLine.start();
            return dataLine;
        }
        return null;
    }

    public boolean isInMicrophoneSensitivity(byte[] audioBuf) {
        final int sampleDecibel = calculateDecibel(audioBuf);
        final int minDecibel = getInputSensitivity();
        return sampleDecibel >= minDecibel;
    }
}
