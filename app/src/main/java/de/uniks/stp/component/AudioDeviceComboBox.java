package de.uniks.stp.component;

import com.jfoenix.controls.JFXComboBox;
import javafx.util.StringConverter;

import javax.sound.sampled.Mixer;
import java.util.Collection;
import java.util.HashMap;

public class AudioDeviceComboBox extends JFXComboBox<Mixer> {
    private HashMap<String, Mixer> options;
    private StringConverter<Mixer> converter;

    public void init() {
        options = new HashMap<>();
        converter = new MixerToStringConverter();
        setConverter(converter);
    }

    public AudioDeviceComboBox withOptions(Collection<Mixer> mixers) {
        mixers.forEach(this::withOption);
        return this;
    }

    public AudioDeviceComboBox withOption(Mixer mixer) {
        final String mixerDescription = converter.toString(mixer);
        options.putIfAbsent(mixerDescription, mixer);
        getItems().add(mixer);
        return this;
    }

    public void setSelection(Mixer mixer) {
        setValue(mixer);
    }

    private class MixerToStringConverter extends StringConverter<Mixer> {
        @Override
        public String toString(Mixer mixer) {
            return mixer.getMixerInfo().getName();
        }

        @Override
        public Mixer fromString(String string) {
            return AudioDeviceComboBox.this.options.get(string);
        }
    }
}
