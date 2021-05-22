package de.uniks.stp.component;

import com.jfoenix.controls.JFXComboBox;

import java.util.HashMap;
import java.util.Map;

public class KeyBasedComboBox extends JFXComboBox<String> {

    HashMap<String, String> options = new HashMap<>();

    public void addOptions(HashMap<String, String> options) {
        for(String key : options.keySet()) {
            addOption(key, options.get(key));
        }
    }

    public void addOption(String key, String value) {
        options.putIfAbsent(key,value);
        getItems().add(value);

    }

    public void setSelection(String key) {
        setValue(options.get(key));
    }

    public String getSelection() {
        for(Map.Entry<String, String> entry : options.entrySet()) {
            if(entry.getValue().equals(getValue())) {
                return entry.getKey();
            }
        }
        return "";
    }
}
