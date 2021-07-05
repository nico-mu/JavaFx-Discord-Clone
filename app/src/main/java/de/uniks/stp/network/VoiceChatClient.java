package de.uniks.stp.network;

import de.uniks.stp.Constants;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.User;
import org.glassfish.grizzly.utils.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonParsingException;
import javax.sound.sampled.*;
import java.io.IOException;
import java.io.StringReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VoiceChatClient {
    private static final Logger log = LoggerFactory.getLogger(VoiceChatClient.class);
    private static final AudioFormat audioFormat = getAudioFormat();

    private final User currentUser;
    private final Channel channel;
    private ExecutorService executorService;

    private boolean stopping = false;

    private DatagramSocket datagramSocket;

    private TargetDataLine audioInDataLine;

    private SourceDataLine audioOutDataLine;

    private InetAddress address;
    private List<User> filteredUsers;

    public VoiceChatClient(User currentUser, Channel channel) {
        this.channel = channel;
        this.currentUser = currentUser;
        withFilteredUsers(currentUser);
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

    private boolean initAudioInDataLine() {
        final DataLine.Info infoIn = new DataLine.Info(TargetDataLine.class, audioFormat);
        if (AudioSystem.isLineSupported(infoIn)) {
            try {
                audioInDataLine = (TargetDataLine) AudioSystem.getLine(infoIn);
                audioInDataLine.open(audioFormat);
                audioInDataLine.start();
            } catch (LineUnavailableException e) {
                log.error("Audio input is currently unavailable on this system.");
                return false;
            }
        } else {
            log.error("Audio input is not supported on this system.");
            return false;
        }
        return true;
    }

    private boolean initAudioOutDataLine() {
        final DataLine.Info infoOut = new DataLine.Info(SourceDataLine.class, audioFormat);
        if (AudioSystem.isLineSupported(infoOut)) {
            try {
                audioOutDataLine = (SourceDataLine) AudioSystem.getLine(infoOut);
                audioOutDataLine.open(audioFormat);
                audioOutDataLine.start();
            } catch (LineUnavailableException e) {
                log.error("Audio output is currently unavailable on this system.");
                return false;
            }
        } else {
            log.error("Audio output is not supported on this system.");
            return false;
        }
        return true;
    }

    public boolean isAudioOutUnavailable() {
        return Objects.isNull(audioOutDataLine);
    }

    public boolean isAudioInUnavailable() {
        return Objects.isNull(audioInDataLine);
    }

    public void init() throws UnknownHostException, SocketException {
        address = InetAddress.getByName(Constants.AUDIOSTREAM_BASE_URL);
        datagramSocket = new DatagramSocket();
        executorService = Executors.newCachedThreadPool();

        currentUser.setAudioOff(!initAudioOutDataLine());
        currentUser.setMute(!initAudioInDataLine());

        datagramSocket.connect(address, Constants.AUDIOSTREAM_PORT);

        executorService.execute(this::receiveAndPlayAudio);
        executorService.execute(this::recordAndSendAudio);
    }

    private void receiveAndPlayAudio() {
        if (Objects.isNull(audioOutDataLine)) {
            log.error("Audio playback not started. The dataLine is not set up properly.");
            return;
        }

        while (!stopping) {
            if (currentUser.isAudioOff()) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    log.error("Thread interrupted while sleeping", e);
                }
                continue;
            }
            byte[] audioBuf = new byte[Constants.AUDIOSTREAM_METADATA_BUFFER_SIZE + Constants.AUDIOSTREAM_AUDIO_BUFFER_SIZE];
            final DatagramPacket audioOutDatagramPacket = new DatagramPacket(audioBuf, audioBuf.length);
            final byte[] metadataBuf = new byte[Constants.AUDIOSTREAM_METADATA_BUFFER_SIZE];
            try {
                datagramSocket.receive(audioOutDatagramPacket);

                System.arraycopy(audioBuf, 0, metadataBuf, 0, Constants.AUDIOSTREAM_METADATA_BUFFER_SIZE);
                final String metadataString = new String(metadataBuf, StandardCharsets.UTF_8).replaceAll("\0+$", "");
                log.debug(metadataString);
                final JsonObject metadataJson = Json.createReader(new StringReader(metadataString)).readObject();
                final String username = metadataJson.getString("name");
                if (Objects.nonNull(username) && filteredUsers.stream().map(User::getName).noneMatch(username::equals)) {
                    audioOutDataLine.write(audioBuf, Constants.AUDIOSTREAM_METADATA_BUFFER_SIZE, Constants.AUDIOSTREAM_AUDIO_BUFFER_SIZE);
                }
            } catch (IOException e) {
                log.error("Failed to receive an audio packet.", e);
            }
        }
    }

    private void recordAndSendAudio() {
        if (Objects.isNull(audioInDataLine)) {
            log.error("Audio recording not startet. The dataLine is not set up properly.");
            return;
        }

        final JsonObject metadataObject = Json.createObjectBuilder()
            .add("channel", channel.getId())
            .add("name", currentUser.getName())
            .build();
        byte[] metadataBytes = metadataObject.toString().getBytes(StandardCharsets.UTF_8);
        if (metadataBytes.length > Constants.AUDIOSTREAM_METADATA_BUFFER_SIZE) {
            log.error("There are more bytes required for metadata than the package allows. Cancelling..");
            return;
        }
        byte[] audioBuf = new byte[Constants.AUDIOSTREAM_METADATA_BUFFER_SIZE + Constants.AUDIOSTREAM_AUDIO_BUFFER_SIZE];
        System.arraycopy(metadataBytes, 0, audioBuf, 0, metadataBytes.length);

        while (!stopping) {
            if (currentUser.isMute()) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    log.error("Thread interrupted while sleeping", e);
                }
                continue;
            }
            audioInDataLine.read(audioBuf, Constants.AUDIOSTREAM_METADATA_BUFFER_SIZE, Constants.AUDIOSTREAM_AUDIO_BUFFER_SIZE);
            final DatagramPacket audioInDatagramPacket = new DatagramPacket(audioBuf, audioBuf.length);
            try {
                datagramSocket.send(audioInDatagramPacket);
            } catch (IOException e) {
                log.error("Failed to send an audio packet.", e);
            }
        }
    }


    public void inject() {
        //needed for testing purposes
    }

    public void stop() {
        stopping = true;

        if (Objects.nonNull(executorService)) {
            executorService.shutdown();
            executorService = null;
        }

        if (Objects.nonNull(audioInDataLine)) {
            audioInDataLine.close();
            audioInDataLine.drain();
            audioInDataLine.stop();
            audioInDataLine = null;
        }
        if (Objects.nonNull(audioOutDataLine)) {
            audioOutDataLine.close();
            audioOutDataLine.drain();
            audioOutDataLine.stop();
            audioOutDataLine = null;
        }

        if (Objects.nonNull(datagramSocket)) {
            datagramSocket.disconnect();
            datagramSocket.close();
            datagramSocket = null;
        }
    }

    public VoiceChatClient withFilteredUsers(User value) {
        if (this.filteredUsers == null) {
            this.filteredUsers = new ArrayList<>();
        }
        if (!this.filteredUsers.contains(value)) {
            this.filteredUsers.add(value);
        }
        return this;
    }

    public VoiceChatClient withFilteredUsers(User... value) {
        for (final User item : value) {
            this.withFilteredUsers(item);
        }
        return this;
    }

    public VoiceChatClient withFilteredUsers(Collection<? extends User> value) {
        for (final User item : value) {
            this.withFilteredUsers(item);
        }
        return this;
    }

    public VoiceChatClient withoutFilteredUsers(User value) {
        if (this.filteredUsers != null) {
            this.filteredUsers.remove(value);
        }
        return this;
    }

    public VoiceChatClient withoutFilteredUsers(User... value) {
        for (final User item : value) {
            this.withoutFilteredUsers(item);
        }
        return this;
    }

    public VoiceChatClient withoutFilteredUsers(Collection<? extends User> value) {
        for (final User item : value) {
            this.withoutFilteredUsers(item);
        }
        return this;
    }
}
