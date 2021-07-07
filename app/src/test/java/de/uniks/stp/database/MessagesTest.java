package de.uniks.stp.database;

import de.uniks.stp.AccordApp;
import de.uniks.stp.dagger.components.test.AppTestComponent;
import de.uniks.stp.dagger.components.test.DaggerAppTestComponent;
import de.uniks.stp.dagger.components.test.SessionTestComponent;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.jpa.model.DirectMessageDTO;
import de.uniks.stp.model.DirectMessage;
import de.uniks.stp.model.User;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MessagesTest {
    private SessionDatabaseService sessionDatabaseService;

    private void sendMessage(User from, User to) {
        sessionDatabaseService.saveDirectMessage((DirectMessage) new DirectMessage()
            .setReceiver(to)
            .setSender(from)
            .setTimestamp(new Date().getTime())
            .setMessage("Test Message")
            .setId(UUID.randomUUID().toString()));
    }

    @Test
    public void testDirectMessagesDatabaseService() {
        AppTestComponent appTestComponent = DaggerAppTestComponent
            .builder()
            .application(Mockito.mock(AccordApp.class))
            .primaryStage(Mockito.mock(Stage.class))
            .build();

        User currentUser = new User().setName("test-user1").setId("test-user1-id");

        SessionTestComponent sessionTestComponent = appTestComponent
            .sessionTestComponentBuilder()
            .currentUser(currentUser)
            .userKey("123-45")
            .build();

        sessionDatabaseService = sessionTestComponent.getSessionDatabaseService();

        User friend1 = new User().setName("test-user2").setId("test-user2-id");
        User friend2 = new User().setName("test-user3").setId("test-user3-id");

        sessionDatabaseService.clearAllConversations();

        sendMessage(currentUser, friend1);
        sendMessage(friend1, currentUser);
        sendMessage(friend1, currentUser);

        List<DirectMessageDTO> directMessages = sessionDatabaseService.getConversation(currentUser.getName(), friend1.getName());
        List<Pair<String, String>> directMessageReceiver = sessionDatabaseService.getAllConversationPartnerOf(currentUser.getName());
        Assertions.assertEquals(3, directMessages.size());
        Assertions.assertEquals(1, directMessageReceiver.size());

        sendMessage(friend2, currentUser);

        directMessages = sessionDatabaseService.getConversation(currentUser.getName(), friend1.getName());
        directMessageReceiver = sessionDatabaseService.getAllConversationPartnerOf(currentUser.getName());
        Assertions.assertEquals(3, directMessages.size());
        Assertions.assertEquals(2, directMessageReceiver.size());

        sendMessage(friend2, currentUser);
        sendMessage(currentUser, friend2);

        directMessages = sessionDatabaseService.getConversation(currentUser.getName(), friend2.getName());
        directMessageReceiver = sessionDatabaseService.getAllConversationPartnerOf(currentUser.getName());
        Assertions.assertEquals(3, directMessages.size());
        Assertions.assertEquals(2, directMessageReceiver.size());

        sessionDatabaseService.clearConversation(currentUser.getName(), friend2.getName());

        directMessages = sessionDatabaseService.getConversation(currentUser.getName(), friend2.getName());
        directMessageReceiver = sessionDatabaseService.getAllConversationPartnerOf(currentUser.getName());
        Assertions.assertEquals(0, directMessages.size());
        Assertions.assertEquals(1, directMessageReceiver.size());

        // Simulate that current user logs out and friend1 logs in, so friend1 is the new current user
        User temp = currentUser;
        currentUser = friend1;
        friend1 = temp;

        directMessages = sessionDatabaseService.getConversation(currentUser.getName(), friend2.getName());
        directMessageReceiver = sessionDatabaseService.getAllConversationPartnerOf(currentUser.getName());
        Assertions.assertEquals(0, directMessages.size());
        Assertions.assertEquals(1, directMessageReceiver.size());

        sendMessage(currentUser, friend2);
        sendMessage(currentUser, friend1);
        sendMessage(friend1, currentUser);
        sendMessage(friend2, currentUser);

        directMessages = sessionDatabaseService.getConversation(currentUser.getName(), friend2.getName());
        directMessageReceiver = sessionDatabaseService.getAllConversationPartnerOf(currentUser.getName());
        Assertions.assertEquals(2, directMessages.size());
        Assertions.assertEquals(2, directMessageReceiver.size());

        sessionDatabaseService.clearAllConversations();
    }

    @AfterEach
    void tear(){
        sessionDatabaseService = null;
    }
}
