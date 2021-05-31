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

        User receiver = new User().setName("test-receiver").setId("test-receiver-id");
        User receiver2 = new User().setName("test-receiver2").setId("test-receiver-id2");
        User sender = new User().setName("test-sender").setId("test-sender-id");

        DatabaseService.clearDirectMessages(receiver);

        DatabaseService.saveDirectMessage((DirectMessage) new DirectMessage()
            .setReceiver(receiver)
            .setSender(sender)
            .setTimestamp(new Date().getTime())
            .setMessage("Test Message")
            .setId(UUID.randomUUID().toString()));
        DatabaseService.saveDirectMessage((DirectMessage) new DirectMessage()
            .setReceiver(receiver)
            .setSender(sender)
            .setTimestamp(new Date().getTime())
            .setMessage("Test Message 2")
            .setId(UUID.randomUUID().toString()));
        DatabaseService.saveDirectMessage((DirectMessage) new DirectMessage()
            .setReceiver(receiver2)
            .setSender(sender)
            .setTimestamp(new Date().getTime())
            .setMessage("Test Message 3")
            .setId(UUID.randomUUID().toString()));

        List<DirectMessageDTO> directMessages = DatabaseService.getDirectMessages(receiver.getId());
        List<Pair<String, String>> directMessageReceiver = DatabaseService.getDirectMessageReceiver();

        Assertions.assertEquals(2, directMessages.size());
        Assertions.assertEquals(2, directMessageReceiver.size());
    }
}
