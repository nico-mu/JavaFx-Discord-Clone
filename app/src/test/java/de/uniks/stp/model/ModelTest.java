package de.uniks.stp.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ModelTest {

    @Test
    public void accordModelTest() {
        Accord accord = new Accord();
        User user1 = new User();
        User user2 = new User();
        User user3 = new User();

        accord.setCurrentUser(user1);
        accord.setCurrentUser(user2);
        accord.setCurrentUser(user3);

        List<User> users = new ArrayList();
        users.add(user1);
        users.add(user2);
        users.add(user3);
        accord.withOtherUsers(users);
        accord.withoutOtherUsers(users);

        accord.withOtherUsers(users);
        accord.withoutOtherUsers(user1,user2,user3);

        accord.removeYou();
    }

    @Test
    public void categoryModelTest() {

    }
}
