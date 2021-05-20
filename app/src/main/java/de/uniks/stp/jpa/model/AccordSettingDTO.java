package de.uniks.stp.jpa.model;

import de.uniks.stp.jpa.AccordSettingKey;

import javax.persistence.*;

@Entity
@Table(name = "ACCORD_SETTING")
public class AccordSettingDTO {
    @Id
    @Column(name = "SETTING_KEY", updatable = false, nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private AccordSettingKey key;

    @Column(name = "SETTING_VALUE")
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
