package de.uniks.stp;

import de.uniks.stp.model.Accord;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.view.Languages;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Editor {
    // Connection to model root object
    private Accord accord;

    public Accord getOrCreateAccord() {
        if (Objects.isNull(accord)) {
            accord = new Accord().setLanguage(Languages.GERMAN.key);
        }
        return accord;
    }

    public void setUserKey(String userKey) {
        accord.setUserKey(userKey);
    }

    public User getOrCreateUser(String name, boolean status) {
        User user = new User().setAccord(accord).setName(name).setStatus(status);

        return user;
    }

    public void setCurrentUser(User currentUser) {
        accord.setCurrentUser(currentUser);
    }

    public User getCurrentUser() {
        return accord.getCurrentUser();
    }

    public Server getOrCreateServer(final String id, final String name) {
        final User currentUser = getOrCreateAccord().getCurrentUser();
        final Map<String, Server> serverMap = availableServersAsServerIdMap();

        if(serverMap.containsKey(id)) {
            return serverMap.get(id);
        }
        return new Server().setName(name).setId(id).withUsers(currentUser);
    }

    public Server getServer(final String id) {
        final Map<String, Server> serverMap = availableServersAsServerIdMap();

        if(serverMap.containsKey(id)) {
            return serverMap.get(id);
        }
        return null;
    }

    public User getOrCreateOtherUser(final String userId, final String name) {
        User other = null;
        final User currentUser = getOrCreateAccord().getCurrentUser();

        if (!name.equals(currentUser.getName())) {
            final Map<String, User> userMap = otherUsersAsIdUserMap();

            if (userMap.containsKey(userId)) {
                other = userMap.get(userId);
            } else {
                other = new User()
                    .setId(userId)
                    .setName(name)
                    .setAccordInstance(accord)
                    .setStatus(true);
            }
        }
        return other;
    }

    public List<User> getOtherUsers() {
        LinkedList<User> otherUsers = new LinkedList<>();
        otherUsersAsIdUserMap().forEach(((s, user) -> otherUsers.add(user)));
        return otherUsers;
    }

    public User getOtherUser(String username) {
        List<User> otherUsers = accord.getOtherUsers();
        for(User user: otherUsers){
            if(user.getName().equals(username)){
                return user;
            }
        }
        return null;
    }

    public User getUserById(String userId) {
        final Map<String, User> userMap = otherUsersAsIdUserMap();
        return userMap.get(userId);
    }

    public void removeOtherUserById(String userId) {
        accord.withoutOtherUsers(getUserById(userId));
    }

    private Map<String, User> otherUsersAsIdUserMap() {
        final List<User> otherUsers = accord.getOtherUsers();

        return otherUsers.stream()
            .collect(Collectors.toMap(User::getId, user -> user));
    }

    private Map<String, Server> availableServersAsServerIdMap() {
        final List<Server> servers = getOrCreateAccord().getCurrentUser().getAvailableServers();
        return servers.stream()
            .collect(Collectors.toMap(Server::getId, server -> server));
    }
}
