package de.uniks.stp.database;

import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.jpa.model.DirectMessageDTO;
import de.uniks.stp.model.DirectMessage;
import de.uniks.stp.model.User;
import javafx.util.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MessagesTest {
    @Test
    public void testDirectMessagesDatabaseService() {
        DatabaseService.init();

        User user1 = new User().setName("test-user1").setId("test-user1-id");
        User user2 = new User().setName("test-user2").setId("test-user2-id");
        User user3 = new User().setName("test-user3").setId("test-user3-id");

        DatabaseService.clearDirectMessages();

        DatabaseService.saveDirectMessage((DirectMessage) new DirectMessage()
            .setReceiver(user2)
            .setSender(user1)
            .setTimestamp(new Date().getTime())
            .setMessage("Test Message")
            .setId(UUID.randomUUID().toString()));
        DatabaseService.saveDirectMessage((DirectMessage) new DirectMessage()
            .setReceiver(user1)
            .setSender(user2)
            .setTimestamp(new Date().getTime())
            .setMessage("Test Message 2")
            .setId(UUID.randomUUID().toString()));
        DatabaseService.saveDirectMessage((DirectMessage) new DirectMessage()
            .setReceiver(user3)
            .setSender(user1)
            .setTimestamp(new Date().getTime())
            .setMessage("Test Message 3")
            .setId(UUID.randomUUID().toString()));

        List<DirectMessageDTO> directMessages = DatabaseService.getDirectMessages(user1.getName(), user2.getName());
        List<Pair<String, String>> directMessageReceiver = DatabaseService.getDirectMessageReceiver(user1.getName());

        Assertions.assertEquals(2, directMessages.size());
        Assertions.assertEquals(3, directMessageReceiver.size());

        DatabaseService.clearDirectMessages();
    }
}
