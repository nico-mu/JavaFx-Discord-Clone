package de.uniks.stp.jpa.model;

import de.uniks.stp.jpa.AccordSettingKey;

import javax.persistence.*;

@Entity
@Table(name = "accord_setting")
public class AccordSettingDTO {
    @Id
    @Column(name = "setting_key", updatable = false, nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private AccordSettingKey key;

    @Column(name = "setting_value")
    private String value;

    public AccordSettingKey getKey() {
        return key;
    }

    public AccordSettingDTO setKey(AccordSettingKey key) {
        this.key = key;
        return this;
    }

    public String getValue() {
        return value;
    }

    public AccordSettingDTO setValue(String value) {
        this.value = value;
        return this;
    }
}
