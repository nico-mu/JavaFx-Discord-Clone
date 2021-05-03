package de.uniks.stp.model;

import org.fulib.builder.ClassModelDecorator;
import org.fulib.builder.ClassModelManager;
import org.fulib.builder.reflect.Link;

import java.util.List;

public class GenModel implements ClassModelDecorator {

    @Override
    public void decorate(ClassModelManager mm) {
        mm.haveNestedClasses(GenModel.class);
    }

    class Accord {
        String language;
        String userKey;

        @Link("accord")
        User currentUser;

        @Link("accordInstance")
        List<User> otherUsers;
    }

    class User {
        String id;
        String name;
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
        List<DirectMessage> receivedMessages;

        @Link("sender")
        List<Message> sentMessages;
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
        List<ServerMessage> messages;
    }

    class Message {
        String id;
        int timestamp;
        String message;

        @Link("sentMessages")
        User sender;
    }

    class ServerMessage extends Message {
        @Link("messages")
        Channel channel;
    }

    class DirectMessage extends Message {
        @Link("receivedMessages")
        User receiver;
    }
}
