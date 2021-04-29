package de.uniks.stp;

import de.uniks.stp.model.Accord;
import de.uniks.stp.model.User;
import de.uniks.stp.view.Languages;

import java.util.Objects;

public class Editor {
    // Connection to model root object
    private Accord accord;

    public Accord getOrCreateAccord() {
        if (Objects.isNull(accord)) {
            accord = new Accord().setLanguage(Languages.GERMAN.key);
        }
        return accord;
    }

    public User getOrCreateUser(String name, boolean status) {
        User user = new User().setAccord(accord).setName(name).setStatus(status);

        return user;
    }
}
