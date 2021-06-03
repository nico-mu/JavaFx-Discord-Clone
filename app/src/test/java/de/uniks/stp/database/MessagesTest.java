package de.uniks.stp.database;

import de.uniks.stp.jpa.DatabaseService;
import de.uniks.stp.jpa.model.DirectMessageDTO;
import de.uniks.stp.model.DirectMessage;
import de.uniks.stp.model.User;
import de.uniks.stp.router.Router;
import javafx.util.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MessagesTest {
    private void sendMessage(User from, User to) {
        DatabaseService.saveDirectMessage((DirectMessage) new DirectMessage()
            .setReceiver(to)
            .setSender(from)
            .setTimestamp(new Date().getTime())
            .setMessage("Test Message")
            .setId(UUID.randomUUID().toString()));
    }

    @Test
    public void testDirectMessagesDatabaseService() {
        DatabaseService.init();

        User currentUser = new User().setName("test-user1").setId("test-user1-id");
        User friend1 = new User().setName("test-user2").setId("test-user2-id");
        User friend2 = new User().setName("test-user3").setId("test-user3-id");

        DatabaseService.clearAllConversations();

        sendMessage(currentUser, friend1);
        sendMessage(friend1, currentUser);
        sendMessage(friend1, currentUser);

        List<DirectMessageDTO> directMessages = DatabaseService.getConversation(currentUser.getName(), friend1.getName());
        List<Pair<String, String>> directMessageReceiver = DatabaseService.getAllConversationPartnerOf(currentUser.getName());
        Assertions.assertEquals(3, directMessages.size());
        Assertions.assertEquals(1, directMessageReceiver.size());

        sendMessage(friend2, currentUser);

        directMessages = DatabaseService.getConversation(currentUser.getName(), friend1.getName());
        directMessageReceiver = DatabaseService.getAllConversationPartnerOf(currentUser.getName());
        Assertions.assertEquals(3, directMessages.size());
        Assertions.assertEquals(2, directMessageReceiver.size());

        sendMessage(friend2, currentUser);
        sendMessage(currentUser, friend2);

        directMessages = DatabaseService.getConversation(currentUser.getName(), friend2.getName());
        directMessageReceiver = DatabaseService.getAllConversationPartnerOf(currentUser.getName());
        Assertions.assertEquals(3, directMessages.size());
        Assertions.assertEquals(2, directMessageReceiver.size());

        DatabaseService.clearConversation(currentUser.getName(), friend2.getName());

        directMessages = DatabaseService.getConversation(currentUser.getName(), friend2.getName());
        directMessageReceiver = DatabaseService.getAllConversationPartnerOf(currentUser.getName());
        Assertions.assertEquals(0, directMessages.size());
        Assertions.assertEquals(1, directMessageReceiver.size());

        // Simulate that current user logs out and friend1 logs in, so friend1 is the new current user
        User temp = currentUser;
        currentUser = friend1;
        friend1 = temp;

        directMessages = DatabaseService.getConversation(currentUser.getName(), friend2.getName());
        directMessageReceiver = DatabaseService.getAllConversationPartnerOf(currentUser.getName());
        Assertions.assertEquals(0, directMessages.size());
        Assertions.assertEquals(1, directMessageReceiver.size());

        sendMessage(currentUser, friend2);
        sendMessage(currentUser, friend1);
        sendMessage(friend1, currentUser);
        sendMessage(friend2, currentUser);

        directMessages = DatabaseService.getConversation(currentUser.getName(), friend2.getName());
        directMessageReceiver = DatabaseService.getAllConversationPartnerOf(currentUser.getName());
        System.out.println(directMessageReceiver);
        Assertions.assertEquals(2, directMessages.size());
        Assertions.assertEquals(2, directMessageReceiver.size());

        DatabaseService.clearAllConversations();
    }
}
