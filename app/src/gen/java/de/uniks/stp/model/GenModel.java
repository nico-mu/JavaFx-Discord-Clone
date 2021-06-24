package de.uniks.stp.model;

import org.fulib.builder.ClassModelDecorator;
import org.fulib.builder.ClassModelManager;
import org.fulib.builder.reflect.Link;

import java.util.List;
import java.util.SortedSet;

public class GenModel implements ClassModelDecorator {

    @Override
    public void decorate(ClassModelManager mm) {
        mm.haveNestedClasses(GenModel.class);
    }

    class Accord {
        String language;
        String userKey;
        String notificationSound;

        @Link("accord")
        User currentUser;

        @Link("accordInstance")
        List<User> otherUsers;
    }

    class User {
        String id;
        String name;
        String password;
        boolean status;

        @Link("currentUser")
        Accord accord;

        @Link("otherUsers")
        Accord accordInstance;

        @Link("users")
        List<Server> availableServers;

        @Link("owner")
        List<Server> ownedServers;

        @Link("receiver")
        List<DirectMessage> privateChatMessages;

        @Link("sender")
        List<Message> sentMessages;

        @Link("chatPartner")
        List<User> chatPartner;

        @Link("channelMembers")
        List<Channel> availableChannels;

        @Link("audioMembers")
        Channel audioChannel;
    }

    class Server {
        String id;
        String name;

        @Link("availableServers")
        List<User> users;

        @Link("ownedServers")
        User owner;

        @Link("server")
        List<Category> categories;

        @Link("server")
        List<Channel> channels;

        @Link("server")
        List<ServerInvitation> invitations;
    }

    class Category {
        String id;
        String name;

        @Link("categories")
        Server server;

        @Link("category")
        List<Channel> channels;
    }

    class Channel {
        String id;
        String name;
        String type;
        boolean privileged;

        @Link("channels")
        Category category;

        @Link("channel")
        SortedSet<ServerMessage> messages;

        @Link("availableChannels")
        List<User> channelMembers;

        @Link("audioChannel")
        List<User> audioMembers;

        @Link("channels")
        Server server;
    }

    class Message {
        String id;
        long timestamp;
        String message;

        @Link("sentMessages")
        User sender;
    }

    class ServerMessage extends Message {
        @Link("messages")
        Channel channel;
    }

    class DirectMessage extends Message {
        @Link("privateChatMessages")
        User receiver;
    }

    class ServerInvitation {
        String id;
        String link;
        String type;
        int max;
        int current;
        @Link("invitations")
        Server server;
    }
}
