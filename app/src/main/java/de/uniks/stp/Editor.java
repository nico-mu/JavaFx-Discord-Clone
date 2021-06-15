package de.uniks.stp;

import de.uniks.stp.model.*;
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

    public Server getOrCreateServer(final String id, final String name) {
        final User currentUser = getOrCreateAccord().getCurrentUser();
        final Map<String, Server> serverMap = availableServersAsServerIdMap();

        if (serverMap.containsKey(id)) {
            return serverMap.get(id);
        }
        return new Server().setName(name).setId(id).withUsers(currentUser);
    }

    public List<Server> getAvailableServers() {
        return accord.getCurrentUser().getAvailableServers();
    }

    public Server getServer(final String id) {
        final Map<String, Server> serverMap = availableServersAsServerIdMap();

        if (serverMap.containsKey(id)) {
            return serverMap.get(id);
        }
        return null;
    }

    public void removeServer(final String id) {
        final Map<String, Server> serverMap = availableServersAsServerIdMap();

        if(serverMap.containsKey(id)) {
            accord.getCurrentUser().withoutAvailableServers(serverMap.get(id));
        }
    }

    public boolean serverAdded(String serverId) {
        List<Server> availableServers = accord.getCurrentUser().getAvailableServers();
        for(Server server: availableServers){
            if(server.getId().equals(serverId)){
                return true;
            }
        }
        return false;
    }

    public User getOrCreateOtherUser(final String userId, final String name) {
        User other = null;
        final User currentUser = getOrCreateAccord().getCurrentUser();

        if (Objects.nonNull(currentUser) && name.equals(currentUser.getName())) {
            currentUser.setId(userId);
        }

        if (Objects.nonNull(currentUser) && !name.equals(currentUser.getName())) {
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
        } else if (Objects.nonNull(currentUser) && name.equals(currentUser.getName())) {
            currentUser.setId(userId);
        }
        return other;
    }

    public User getOtherUser(String username) {
        List<User> otherUsers = accord.getOtherUsers();
        for (User user : otherUsers) {
            if (user.getName().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public User getUserById(String userId) {
        final Map<String, User> userMap = otherUsersAsIdUserMap();
        return userMap.get(userId);
    }

    public void removeOtherUserById(String userId) {
        accord.withoutOtherUsers(getUserById(userId));
    }

    private Map<String, User> otherUsersAsIdUserMap() {
        final List<User> otherUsers = accord.getOtherUsers();

        return otherUsers.stream()
            .collect(Collectors.toMap(User::getId, user -> user));
    }

    private Map<String, Server> availableServersAsServerIdMap() {
        final List<Server> servers = getOrCreateAccord().getCurrentUser().getAvailableServers();
        return servers.stream()
            .collect(Collectors.toMap(Server::getId, server -> server));
    }

    public Category getOrCreateCategory(final String categoryId, final String name, final Server server) {
        if (Objects.nonNull(server)) {
            for (Category category : server.getCategories()) {
                if (category.getId().equals(categoryId)) {
                    return category;
                }
            }
            Category newCategory = new Category().setName(name).setId(categoryId);
            server.withCategories(newCategory);
            return newCategory;
        }
        return null;
    }

    public Category getCategory(final String categoryId, final Server server) {
        if (Objects.nonNull(server)) {
            for (Category category : server.getCategories()) {
                if (category.getId().equals(categoryId)) {
                    return category;
                }
            }
        }
        return null;
    }

    public void deleteCategory(String serverId, String categoryId){
        Server server = getServer(serverId);
        Category delCat = getCategory(categoryId, server);
        server.withoutCategories(delCat);
    }

    public Channel getOrCreateChannel(final String channelId, final String name, final Category category) {
        if (Objects.nonNull(category)) {
            for (Channel channel : category.getChannels()) {
                if (channel.getId().equals(channelId)) {
                    return channel;
                }
            }
            Channel newChannel = new Channel().setId(channelId).setName(name);
            category.withChannels(newChannel);
            return newChannel;
        }
        return null;
    }

    public Channel getChannel(final String channelId, final Category category) {
        if (Objects.nonNull(category)) {
            for (Channel channel : category.getChannels()) {
                if (channel.getId().equals(channelId)) {
                    return channel;
                }
            }
        }
        return null;
    }

    // channelId is unique for complete server, not just for a category
    public Channel getChannel(final String channelId, final Server server) {
        if (Objects.nonNull(server)) {
            for (Category category : server.getCategories()) {
                for (Channel channel : category.getChannels()) {
                    if (channel.getId().equals(channelId)) {
                        return channel;
                    }
                }
            }
            for (Channel channel : server.getChannels()) {
                if (channel.getId().equals(channelId)) {
                    return channel;
                }
            }
        }
        return null;
    }

    public Channel getChannelById(String id) {
        for (Server server : getOrCreateAccord().getCurrentUser().getAvailableServers()) {
            for (Category category : server.getCategories()) {
                for (Channel channel : category.getChannels()) {
                    if (channel.getId().equals(id)) {
                        return channel;
                    }
                }
            }
        }
        return null;
    }

    public User getOrCreateServerMember(String userId, String name, boolean status, Server server) {
        for (User user : server.getUsers()) {
            if (user.getName().equals(name)) {
                return user;
            }
        }

        User user = new User().setId(userId).setName(name).setStatus(status);
        server.withUsers(user);

        return user;
    }

    public void setServerMemberStatus(String userId, String name, boolean status, Server server) {
        if (Objects.isNull(server)) {
            return;
        }

        for (User user : server.getUsers()) {
            if (user.getName().equals(name)) {
                user.setStatus(status);
                server.firePropertyChange(Server.PROPERTY_USERS, null, user);
                return;
            }
        }

        getOrCreateServerMember(userId, name, status, server);
    }

    public void setServerMemberOnline(String userId, String userName, Server server) {
        setServerMemberStatus(userId, userName, true, server);
    }

    public void setServerMemberOffline(String userId, String userName, Server server) {
        setServerMemberStatus(userId, userName, false, server);
    }

    public User getOrCreateChatPartnerOfCurrentUser(String id, String name) {
        User currentUser = accord.getCurrentUser();

        if (Objects.isNull(currentUser) || name.equals(currentUser.getName())) {
            return null;
        }

        for (User user : currentUser.getChatPartner()) {
            if (user.getId().equals(id)) {
                user.setName(name).setId(id);
                return user;
            }
        }

        User user = new User().setId(id).setName(name).setStatus(false);
        currentUser.withChatPartner(user);

        return user;
    }

    public User getChatPartnerOfCurrentUserById(String userId) {
        User currentUser = accord.getCurrentUser();

        for (User user : currentUser.getChatPartner()) {
            if (user.getId().equals(userId)) {
                return user;
            }
        }

        return null;
    }

    public boolean isChatPartnerOfCurrentUser(String userId) {
        return Objects.nonNull(getChatPartnerOfCurrentUserById(userId));
    }

    public void prepareLogout() {
        accord.setUserKey("");
        accord = new Accord().setLanguage(accord.getLanguage());
    }

    public ServerInvitation getOrCreateServerInvitation(String invId, String link, String type, int max, int current, String serverId) {
        Server server = getServer(serverId);
        for (ServerInvitation serverInvitation : server.getInvitations()) {
            if (serverInvitation.getId().equals(invId)) {
                serverInvitation.setCurrent(current);
                return serverInvitation;
            }
        }
        ServerInvitation newInvite = new ServerInvitation().setId(invId).setLink(link).setType(type).setMax(max).setCurrent(current).setServer(server);
        return newInvite;
    }

    public void deleteChannel(String channelId) {
        Channel channel = getChannelById(channelId);
        channel.setServer(null);
        channel.getCategory().withoutChannels(channel);
    }
}
