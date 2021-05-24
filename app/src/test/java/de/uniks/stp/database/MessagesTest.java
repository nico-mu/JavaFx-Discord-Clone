package de.uniks.stp.database;

import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.jpa.model.DirectMessageDTO;
import de.uniks.stp.model.DirectMessage;
import de.uniks.stp.model.User;
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
        User sender = new User().setName("test-sender").setId("test-sender-id");

        DatabaseService.clearDirectMessages(receiver);

        DatabaseService.saveDirectMessage((DirectMessage) new DirectMessage()
            .setReceiver(receiver)
            .setSender(sender)
            .setTimestamp(new Date().getTime())
            .setMessage("Test Message")
            .setId(UUID.randomUUID().toString()));

        List<DirectMessageDTO> directMessages = DatabaseService.getDirectMessages(receiver);

        Assertions.assertEquals(1, directMessages.size());

    }
}
