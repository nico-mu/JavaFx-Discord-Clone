package de.uniks.stp;

import de.uniks.stp.model.Accord;
import de.uniks.stp.model.Server;
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

    public void setUserKey(String userKey){
        accord.setUserKey(userKey);
    }

    public User getOrCreateUser(String name, boolean status) {
        User user = new User().setAccord(accord).setName(name).setStatus(status);

        return user;
    }

    public void setCurrentUser(User currentUser){
        accord.setCurrentUser(currentUser);
    }

    public Server getServer(String id) {
        for(Server server : accord.getCurrentUser().getAvailableServers()) {
            if(server.getId().equals(id)) {
                return server;
            }
        }
        return null;
    }
}
