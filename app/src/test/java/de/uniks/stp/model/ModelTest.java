package de.uniks.stp.model;

import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
        Category category = new Category();
        category.setId("1");
        category.setId("1");

        category.setName("name1");
        category.setName("name1");

        Channel channel1 = new Channel();
        Channel channel2 = new Channel();
        Channel channel3 = new Channel();

        List<Channel> channels = new ArrayList<>();
        channels.add(channel1);
        channels.add(channel2);
        channels.add(channel3);

        category.withChannels(channels);
        category.withoutChannels(channels);

        category.withChannels(channel1, channel2);
        category.withoutChannels(channel1, channel2);

        category.toString();
        category.removeYou();
    }

    @Test
    public void channelModelTest() {
        Channel channel = new Channel();

        channel.setId("1");
        channel.setId("1");

        channel.setName("name1");
        channel.setName("name1");

        ServerMessage serverMessage1 = new ServerMessage();
        serverMessage1.setTimestamp(1);
        serverMessage1.setId("1");
        ServerMessage serverMessage2 = new ServerMessage();
        serverMessage2.setTimestamp(2);
        serverMessage2.setId("2");
        ServerMessage serverMessage3 = new ServerMessage();
        serverMessage3.setTimestamp(3);
        serverMessage3.setId("3");
        List<ServerMessage> serverMessages = new ArrayList<>();
        serverMessages.add(serverMessage1);
        serverMessages.add(serverMessage2);
        serverMessages.add(serverMessage3);

        channel.withMessages(serverMessages);
        channel.withoutMessages(serverMessages);

        channel.withMessages(serverMessage1, serverMessage2);
        channel.withoutMessages(serverMessage1, serverMessage2);

        User user1 = new User();
        User user2 = new User();
        User user3 = new User();
        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
        users.add(user3);

        channel.withChannelMembers(users);
        channel.withoutChannelMembers(users);

        channel.withChannelMembers(user1, user2);
        channel.withoutChannelMembers(user1, user2);

        channel.withAudioMembers(users);
        channel.withoutAudioMembers(users);

        channel.withAudioMembers(user1, user2);
        channel.withoutAudioMembers(user1, user2);

        channel.toString();
        channel.removeYou();
    }

    @Test
    public void messageModelTest() {
        Message message = new Message();

        message.setTimestamp(1);
        message.setTimestamp(1);

        message.toString();

        message.removeYou();
    }

    @Test
    public void serverModelTest() {
        Server server =  new Server();

        server.setId("1");
        server.setId("1");

        ServerInvitation serverInvitation1 = new ServerInvitation();
        ServerInvitation serverInvitation2 = new ServerInvitation();
        List<ServerInvitation> serverInvitations = new ArrayList<>();
        serverInvitations.add(serverInvitation1);
        serverInvitations.add(serverInvitation2);

        server.withInvitations(serverInvitations);
        server.withoutInvitations(serverInvitations);

        server.withInvitations(serverInvitation1, serverInvitation2);
        server.withoutInvitations(serverInvitation1, serverInvitation2);

        Channel channel1 = new Channel();
        Channel channel2 = new Channel();
        List<Channel> channels = new ArrayList<>();
        channels.add(channel1);
        channels.add(channel2);

        server.withChannels(channels);
        server.withoutChannels(channels);

        server.withChannels(channel1, channel2);
        server.withoutChannels(channel1, channel2);

        Category category1 = new Category();
        Category category2 = new Category();
        List<Category> categories = new ArrayList<>();
        categories.add(category1);
        categories.add(category2);

        server.withCategories(categories);
        server.withoutCategories(categories);

        server.withCategories(category1, category2);
        server.withoutCategories(category1, category2);

        User user1 = new User();
        User user2 = new User();

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        server.withUsers(users);
        server.withoutUsers(users);

        server.withUsers(user1, user2);
        server.withoutUsers(user1, user2);

        server.setOwner(user1);
        server.setOwner(user2);

        server.toString();

        server.removeYou();
    }

    @Test
    public void serverInvitationModelTest() {
        ServerInvitation serverInvitation = new ServerInvitation();

        serverInvitation.firePropertyChange(ServerInvitation.PROPERTY_ID, null, null);
        serverInvitation.listeners();

        serverInvitation.setId("1");
        serverInvitation.setId("1");

        serverInvitation.setLink("link");
        serverInvitation.setLink("link");

        serverInvitation.setType("type");
        serverInvitation.setType("type");

        serverInvitation.setMax(1);
        serverInvitation.setMax(1);

        serverInvitation.setCurrent(1);

        serverInvitation.toString();

        serverInvitation.removeYou();
    }

    @Test
    public void serverMessageModelTest() {
        ServerMessage serverMessage = new ServerMessage();

        serverMessage.removeYou();
    }

    @Test
    public void userModelTest() {
        User user = new User();

        user.setPassword("password");
        user.setPassword("password");

        user.getPrivateChatMessages();

        DirectMessage directMessage1 = new DirectMessage();
        DirectMessage directMessage2 = new DirectMessage();
        List<DirectMessage> directMessages = new ArrayList<>();
        directMessages.add(directMessage1);
        directMessages.add(directMessage2);

        user.withPrivateChatMessages(directMessages);
        user.withoutPrivateChatMessages(directMessages);

        user.withPrivateChatMessages(directMessage1, directMessage2);
        user.withoutPrivateChatMessages(directMessage1, directMessage2);

        user.getSentMessages();

        Message message1 = new Message();
        Message message2 = new Message();
        List<Message> messages = new ArrayList<>();
        messages.add(message1);
        messages.add(message2);

        user.withSentMessages(messages);
        user.withoutSentMessages(messages);

        user.withSentMessages(message1, message2);
        user.withoutSentMessages(message1, message2);

        user.getAvailableChannels();

        Channel channel1 = new Channel();
        Channel channel2 = new Channel();
        List<Channel> channels = new ArrayList<>();
        channels.add(channel1);
        channels.add(channel2);

        user.withAvailableChannels(channels);
        user.withoutAvailableChannels(channels);

        user.withAvailableChannels(channel1, channel2);
        user.withoutAvailableChannels(channel1, channel2);

        Server server1 = new Server();
        Server server2 = new Server();
        List<Server> servers = new ArrayList<>();
        servers.add(server1);
        servers.add(server2);

        user.withAvailableServers(servers);
        user.withoutAvailableServers(servers);

        user.withAvailableServers(server1, server2);
        user.withoutAvailableServers(server1, server2);

        user.getOwnedServers();

        user.withOwnedServers(servers);
        user.withoutOwnedServers(servers);

        user.withOwnedServers(server1, server2);
        user.withoutOwnedServers(server1, server2);

        user.getAccord();

        user.getAccordInstance();

        User user1 = new User();
        User user2 = new User();
        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        user.withChatPartner(users);
        user.withoutChatPartner(users);

        user.withChatPartner(user1, user2);
        user.withoutChatPartner(user1, user2);

        user.getAudioChannel();

        user.setAudioChannel(channel1);
        user.setAudioChannel(channel1);

        user.setMute(true);
        user.setMute(true);

        user.setAudioOff(true);
        user.setAudioOff(true);

        user.toString();

        user.removeYou();
    }

    public void handlePropertyChange(PropertyChangeEvent propertyChangeEvent) {
        return;
    }
}
