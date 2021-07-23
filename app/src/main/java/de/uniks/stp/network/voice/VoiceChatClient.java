package de.uniks.stp.network.voice;

import de.uniks.stp.AudioService;
import de.uniks.stp.Constants;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonParsingException;
import javax.sound.sampled.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.StringReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VoiceChatClient {
    private static final Logger log = LoggerFactory.getLogger(VoiceChatClient.class);
    private static final AudioFormat audioFormat = getAudioFormat();
    private final Object audioInLock = new Object();
    private final Object audioOutLock = new Object();
    private Mixer speaker;
    private Mixer microphone;
    private final Map<String, SourceDataLine> userSourceDataLineMap = new HashMap<>();

    private final User currentUser;
    private final Channel channel;
    private final PropertyChangeListener currentUserMutePropertyChangeListener = this::onCurrentUserMutePropertyChange;
    private final PropertyChangeListener currentUserAudioOffPropertyChangeListener = this::onCurrentUserAudioOffPropertyChange;
    private final PropertyChangeListener audioMembersPropertyChangeListener = this::onAudioMembersPropertyChange;
    private boolean running;
    private InetAddress address;
    private List<User> filteredUsers = new ArrayList<>();
    private final PropertyChangeListener userMutePropertyChangeListener = this::onUserMutePropertyChange;
    private DatagramSocket datagramSocket;
    private TargetDataLine audioInDataLine;
    private ExecutorService executorService;
    public VoiceChatClient(Channel channel,
                           User currentUser,
                           Mixer speaker,
                           Mixer microphone) {
        this.channel = channel;
        this.currentUser = currentUser;
        this.speaker = speaker;
        this.microphone = microphone;
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

    public void init() {
        try {
            initMicrophone();
        } catch (LineUnavailableException e) {
            log.error("The microphone could not be initialized", e);
        }

        try {
            address = InetAddress.getByName(Constants.AUDIOSTREAM_BASE_URL);
        } catch (UnknownHostException e) {
            log.error("Host could not be resolved", e);
        }

        try {
            datagramSocket = new DatagramSocket();
            datagramSocket.connect(address, Constants.AUDIOSTREAM_PORT);
        } catch (SocketException e) {
            log.error("The socket could not be set up", e);
        }

        final List<User> audioMembers = channel.getAudioMembers();
        audioMembers.forEach(this::userJoined);
        channel.listeners().addPropertyChangeListener(Channel.PROPERTY_AUDIO_MEMBERS, audioMembersPropertyChangeListener);

        final PropertyChangeSupport currentUserListeners = currentUser.listeners();
        currentUserListeners.addPropertyChangeListener(User.PROPERTY_MUTE, currentUserMutePropertyChangeListener);
        currentUserListeners.addPropertyChangeListener(User.PROPERTY_AUDIO_OFF, currentUserAudioOffPropertyChangeListener);

        running = true;
        executorService = Executors.newCachedThreadPool();
        executorService.execute(this::receiveAndPlayAudio);
        executorService.execute(this::recordAndSendAudio);
    }

    private void receiveAndPlayAudio() {
        while (running) {
            if (currentUser.isAudioOff()) {
                try {
                    synchronized (audioOutLock) {
                        audioOutLock.wait(60000);
                    }
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
                final JsonObject metadataJson = Json.createReader(new StringReader(metadataString)).readObject();
                final String userName = metadataJson.getString("name");
                final SourceDataLine audioOutDataLine = userSourceDataLineMap.get(userName);
                if (Objects.nonNull(userName) && Objects.nonNull(audioOutDataLine) && filteredUsers.stream().map(User::getName).noneMatch(userName::equals)) {
                    audioOutDataLine.write(audioBuf, Constants.AUDIOSTREAM_METADATA_BUFFER_SIZE, Constants.AUDIOSTREAM_AUDIO_BUFFER_SIZE);
                }
            } catch (SocketException | JsonParsingException ignored) {
            } catch (IOException e) {
                log.error("Failed to receive an audio packet.", e);
            }
        }
    }

    public void changeSpeaker(Mixer speaker) {
        if (!this.speaker.equals(speaker)){
            this.speaker = speaker;
            userSourceDataLineMap.forEach((userName, oldSourceDataLine) -> {
                try {
                    initSpeakerForUser(userName);
                    oldSourceDataLine.stop();
                    oldSourceDataLine.drain();
                    oldSourceDataLine.close();
                } catch (LineUnavailableException e) {
                    log.error("The speaker could not be initialized", e);
                }
            });
        }
    }

    public void changeMicrophone(Mixer microphone) {
        if (!this.microphone.equals(microphone)){
            this.microphone = microphone;
            if (Objects.nonNull(audioInDataLine)) {
                final TargetDataLine oldAudioinDataLine = audioInDataLine;
                try {
                    initMicrophone();
                } catch (LineUnavailableException e) {
                    log.error("The microphone could not be initialized", e);
                }
                oldAudioinDataLine.stop();
                oldAudioinDataLine.drain();
                oldAudioinDataLine.close();
            }
        }

    }

    private void recordAndSendAudio() {
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

        while (running) {
            if (currentUser.isMute()) {
                try {
                    synchronized (audioInLock) {
                        audioInLock.wait(60000);
                    }
                } catch (InterruptedException e) {
                    log.error("Thread interrupted while sleeping", e);
                }
                continue;
            }
            audioInDataLine.read(audioBuf, Constants.AUDIOSTREAM_METADATA_BUFFER_SIZE, Constants.AUDIOSTREAM_AUDIO_BUFFER_SIZE);
            final DatagramPacket audioInDatagramPacket = new DatagramPacket(audioBuf, audioBuf.length, address, Constants.AUDIOSTREAM_PORT);
            try {
                datagramSocket.send(audioInDatagramPacket);
            } catch (IOException e) {
                log.error("Failed to send an audio packet.", e);
            }
        }
    }

    private void userJoined(User user) {
        final String userName = user.getName();
        try {
            initSpeakerForUser(userName);

            user.listeners().addPropertyChangeListener(User.PROPERTY_MUTE, userMutePropertyChangeListener);
        } catch (LineUnavailableException e) {
            log.error("Failed to get line for user {}. Cleaning up..", userName);
            final SourceDataLine audioOutDataLine = userSourceDataLineMap.remove(user.getName());
            if (Objects.nonNull(audioOutDataLine)) {
                audioOutDataLine.stop();
                audioOutDataLine.close();
            }
        }
    }

    private void initSpeakerForUser(String userName) throws LineUnavailableException {
        final DataLine.Info infoOut = new DataLine.Info(SourceDataLine.class, audioFormat);

        final SourceDataLine audioOutDataLine = (SourceDataLine) speaker.getLine(infoOut);
        userSourceDataLineMap.put(userName, audioOutDataLine);

        audioOutDataLine.open(audioFormat);
        audioOutDataLine.start();
    }

    private void userLeft(User user) {
        final SourceDataLine audioOutDataLine = userSourceDataLineMap.remove(user.getName());
        user.listeners().removePropertyChangeListener(User.PROPERTY_MUTE, userMutePropertyChangeListener);

        if (Objects.nonNull(audioOutDataLine)) {
            audioOutDataLine.stop();
            audioOutDataLine.drain();
            audioOutDataLine.close();
        }
    }

    private void onAudioMembersPropertyChange(PropertyChangeEvent propertyChangeEvent) {
        final User oldValue = (User) propertyChangeEvent.getOldValue();
        final User newValue = (User) propertyChangeEvent.getNewValue();

        if (Objects.isNull(oldValue)) {
            userJoined(newValue);
        } else if (Objects.isNull(newValue)) {
            userLeft(oldValue);
        }
    }

    private void onCurrentUserMutePropertyChange(PropertyChangeEvent propertyChangeEvent) {
        final boolean isMute = (boolean) propertyChangeEvent.getNewValue();
        if (isMute) {
            audioInDataLine.stop();
            audioInDataLine.drain();
        } else {
            audioInDataLine.start();
            synchronized (audioInLock) {
                audioInLock.notifyAll();
            }
        }
    }

    private void onCurrentUserAudioOffPropertyChange(PropertyChangeEvent propertyChangeEvent) {
        final boolean isAudioOff = (boolean) propertyChangeEvent.getNewValue();
        if (isAudioOff) {
            userSourceDataLineMap.forEach((s, sourceDataLine) -> {
                sourceDataLine.stop();
                sourceDataLine.drain();
            });
        } else {
            userSourceDataLineMap.forEach((s, sourceDataLine) -> sourceDataLine.start());
            synchronized (audioOutLock) {
                audioOutLock.notifyAll();
            }
        }
    }

    private void onUserMutePropertyChange(PropertyChangeEvent propertyChangeEvent) {
        final User user = (User) propertyChangeEvent.getSource();
        if (!currentUser.equals(user)) {
            boolean isMute = (boolean) propertyChangeEvent.getNewValue();
            if (isMute) {
                withFilteredUsers(user);
            } else {
                withoutFilteredUsers(user);
            }
        }
    }

    private void initMicrophone() throws LineUnavailableException {
        final DataLine.Info infoIn = new DataLine.Info(TargetDataLine.class, audioFormat);

        audioInDataLine = (TargetDataLine) microphone.getLine(infoIn);
        audioInDataLine.open(audioFormat);
        audioInDataLine.start();
    }

    public void stop() {
        running = false;

        synchronized (audioOutLock) {
            audioOutLock.notifyAll();
        }

        synchronized (audioInLock) {
            audioInLock.notifyAll();
        }
        if (Objects.nonNull(audioInDataLine)) {
            audioInDataLine.stop();
            audioInDataLine.drain();
            audioInDataLine.close();
        }

        final PropertyChangeSupport currentUserListeners = currentUser.listeners();
        currentUserListeners.removePropertyChangeListener(User.PROPERTY_MUTE, currentUserMutePropertyChangeListener);
        currentUserListeners.removePropertyChangeListener(User.PROPERTY_AUDIO_OFF, currentUserAudioOffPropertyChangeListener);

        channel.getAudioMembers().forEach(user -> {
            userLeft(user);
            withoutFilteredUsers(user);
        });
        channel.listeners().removePropertyChangeListener(Channel.PROPERTY_AUDIO_MEMBERS, audioMembersPropertyChangeListener);

        if (Objects.nonNull(executorService)) {
            executorService.shutdown();
        }

        if (Objects.nonNull(datagramSocket)) {
            datagramSocket.close();
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

    public VoiceChatClient withoutFilteredUsers(User value) {
        if (this.filteredUsers != null) {
            this.filteredUsers.remove(value);
        }
        return this;
    }
}
