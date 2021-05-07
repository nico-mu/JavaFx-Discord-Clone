package de.uniks.stp;

import de.uniks.stp.model.Accord;
import de.uniks.stp.model.User;
import de.uniks.stp.view.Languages;

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

    public void removeOtherUserById(String userId) {
        final Map<String, User> userMap = otherUsersAsIdUserMap();
        final User userToBeRemoved = userMap.get(userId);

        accord.withoutOtherUsers(userToBeRemoved);
    }

    private Map<String, User> otherUsersAsIdUserMap() {
        final List<User> otherUsers = accord.getOtherUsers();

        return otherUsers.stream()
            .collect(Collectors.toMap(User::getId, user -> user));
    }
}
