package de.uniks.stp.network.rest;

import de.uniks.stp.Constants;
import de.uniks.stp.model.Channel;
import kong.unirest.Callback;
import kong.unirest.HttpRequest;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import java.util.ArrayList;

public class SessionRestClient extends AppRestClient {

    public SessionRestClient(HttpRequestInterceptor interceptor) {
        super();
        instance.config().interceptor(interceptor);
    }

    public void getServers(Callback<JsonNode> callback) {
        HttpRequest<?> req = instance.get(Constants.REST_SERVER_PATH);
        sendRequest(req, callback);
    }

    public void getServerInformation(String id, Callback<JsonNode> callback) {
        HttpRequest<?> req = instance.get(Constants.REST_SERVER_PATH + "/" + id);
        sendRequest(req, callback);
    }

    public void renameServer(String id, String newName, Callback<JsonNode> callback) {
        HttpRequest<?> req = instance.put(Constants.REST_SERVER_PATH + "/" + id)
            .body(Json.createObjectBuilder().add("name", newName).build().toString());
        sendRequest(req, callback);
    }

    public void deleteServer(String id, Callback<JsonNode> callback) {
        HttpRequest<?> req = instance.delete(Constants.REST_SERVER_PATH + "/" + id);
        sendRequest(req, callback);
    }

    public void createCategory(String id, String name, Callback<JsonNode> callback) {
        HttpRequest<?> req = instance.post(Constants.REST_SERVER_PATH + "/" + id + Constants.REST_CATEGORY_PATH)
            .body(Json.createObjectBuilder().add("name", name).build().toString());
        sendRequest(req, callback);
    }

    public void updateCategory(String serverId, String catId, String name, Callback<JsonNode> callback) {
        HttpRequest<?> req = instance.put(Constants.REST_SERVER_PATH + "/" + serverId + Constants.REST_CATEGORY_PATH + "/" + catId)
            .body(Json.createObjectBuilder().add("name", name).build().toString());
        sendRequest(req, callback);
    }

    public void deleteCategory(String serverId, String categoryId, Callback<JsonNode> callback) {
        HttpRequest<?> req = instance.delete(Constants.REST_SERVER_PATH + "/" + serverId + Constants.REST_CATEGORY_PATH + "/" + categoryId);
        sendRequest(req, callback);
    }

    public void getCategories(String serverId, Callback<JsonNode> callback) {
        HttpRequest<?> request = instance.get(Constants.REST_SERVER_PATH + "/" + serverId + Constants.REST_CATEGORY_PATH);
        sendRequest(request, callback);
    }

    public void getChannels(String serverId, String categoryId, Callback<JsonNode> callback) {
        String requestPath = Constants.REST_SERVER_PATH + "/" + serverId + Constants.REST_CATEGORY_PATH + "/" + categoryId + Constants.REST_CHANNEL_PATH;
        HttpRequest<?> request = instance.get(requestPath);
        sendRequest(request, callback);
    }

    public void sendLogoutRequest(Callback<JsonNode> callback) {
        //
        HttpRequest<?> postUserLogout = instance.post(Constants.REST_USERS_PATH + Constants.REST_LOGOUT_PATH);
        sendRequest(postUserLogout, callback);
    }

    public void requestOnlineUsers(final Callback<JsonNode> callback) {
        sendRequest(instance.get(Constants.REST_USERS_PATH), callback);
    }

    public String buildCreateServerRequest(String name) {
        return Json.createObjectBuilder().add("name", name).build().toString();
    }

    public void createServer(String name, Callback<JsonNode> callback) {
        HttpRequest<?> postCreateServer = instance.post(Constants.SERVERS_PATH)
            .body(buildCreateServerRequest(name));
        sendRequest(postCreateServer, callback);
    }

    /**
     * Gets the last 50 messages of given channel after given timestamp
     *
     * @param serverId
     * @param categoryId
     * @param channelId
     * @param timestamp  Only messages older than this timestamp will be returned
     * @param callback
     */
    public void getServerChannelMessages(String serverId, String categoryId, String channelId, long timestamp, Callback<JsonNode> callback) {
        String requestPath = Constants.REST_SERVER_PATH + "/" + serverId
            + Constants.REST_CATEGORY_PATH + "/" + categoryId
            + Constants.REST_CHANNEL_PATH + "/" + channelId
            + Constants.REST_MESSAGES_PATH + Constants.REST_TIMESTAMP_PATH + timestamp;
        HttpRequest<?> request = instance.get(requestPath);
        sendRequest(request, callback);
    }

    public void createChannel(String serverId, String categoryId, String channelName, String type, Boolean privileged, ArrayList<String> members, Callback<JsonNode> callback) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (String userId : members) {
            arrayBuilder.add(userId);
        }
        HttpRequest<?> req = instance.post(Constants.REST_SERVER_PATH + "/" + serverId + Constants.REST_CATEGORY_PATH + "/" + categoryId + Constants.REST_CHANNEL_PATH)
            .body(Json.createObjectBuilder()
                .add("name", channelName)
                .add("type", type)
                .add("privileged", privileged)
                .add("members", arrayBuilder.build()).build().toString());
        sendRequest(req, callback);
    }

    public void getServerInvitations(String serverId, Callback<JsonNode> callback) {
        HttpRequest<?> req = instance.get(Constants.REST_SERVER_PATH + "/" + serverId + Constants.REST_INVITES_PATH);
        sendRequest(req, callback);
    }

    public void createServerInvitation(String serverId, String type, int max, Callback<JsonNode> callback) {
        HttpRequest<?> req = instance.post(Constants.REST_SERVER_PATH + "/" + serverId + Constants.REST_INVITES_PATH)
            .body(Json.createObjectBuilder()
                .add("type", type)
                .add("max", max).build().toString());
        sendRequest(req, callback);
    }

    public void deleteServerInvitation(String serverId, String invId, Callback<JsonNode> callback) {
        HttpRequest<?> req = instance.delete(Constants.REST_SERVER_PATH + "/" + serverId + Constants.REST_INVITES_PATH + "/" + invId);
        sendRequest(req, callback);
    }

    public void joinServer(String serverId, String invId, String username, String password, Callback<JsonNode> callback) {
        HttpRequest<?> req = instance.post(Constants.REST_SERVER_PATH + "/" + serverId + Constants.REST_INVITES_PATH + "/" + invId)
            .body(Json.createObjectBuilder()
                .add("name", username)
                .add("password", password).build().toString());
        sendRequest(req, callback);
    }

    public void editTextChannel(String serverId, String categoryId, String channelId, String channelName, Boolean privileged, ArrayList<String> members, Callback<JsonNode> callback) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (String userId : members) {
            arrayBuilder.add(userId);
        }
        HttpRequest<?> req = instance.put(Constants.REST_SERVER_PATH + "/" + serverId + Constants.REST_CATEGORY_PATH + "/" + categoryId + Constants.REST_CHANNEL_PATH + "/" + channelId)
            .body(Json.createObjectBuilder()
                .add("name", channelName)
                .add("privileged", privileged)
                .add("members", arrayBuilder.build()).build().toString());
        sendRequest(req, callback);
    }

    public void deleteChannel(String serverId, String categoryId, String channelId, Callback<JsonNode> callback) {
        HttpRequest<?> req = instance.delete(Constants.REST_SERVER_PATH + "/" + serverId + Constants.REST_CATEGORY_PATH + "/" + categoryId + Constants.REST_CHANNEL_PATH + "/" + channelId);
        sendRequest(req, callback);
    }

    public void leaveServer(String serverId, Callback<JsonNode> callback) {
        HttpRequest<?> req = instance.post(Constants.REST_SERVER_PATH + "/" + serverId + "/leave");
        sendRequest(req, callback);
    }

    public void joinAudioChannel(Channel model, Callback<JsonNode> callback) {
        final String serverId = model.getServer().getId();
        final String catId = model.getCategory().getId();
        final String chanId = model.getId();

        final HttpRequest<?> req = instance.post(Constants.REST_SERVER_PATH + "/" + serverId + Constants.REST_CATEGORY_PATH + "/" + catId + Constants.REST_CHANNEL_PATH + "/" + chanId + "/join");
        sendRequest(req, callback);
    }

    public void leaveAudioChannel(Channel model, Callback<JsonNode> callback) {
        final String serverId = model.getServer().getId();
        final String catId = model.getCategory().getId();
        final String chanId = model.getId();

        final HttpRequest<?> req = instance.post(Constants.REST_SERVER_PATH + "/" + serverId + Constants.REST_CATEGORY_PATH + "/" + catId + Constants.REST_CHANNEL_PATH + "/" + chanId + "/leave");
        sendRequest(req, callback);
    }

    public void updateMessage(String serverId, String categoryId, String channelId, String messageId, String text, Callback<JsonNode> callback) {
        HttpRequest<?> req = instance.put(Constants.REST_SERVER_PATH + "/" + serverId + Constants.REST_CATEGORY_PATH + "/" + categoryId + Constants.REST_CHANNEL_PATH + "/" + channelId + Constants.REST_MESSAGES_PATH + "/" + messageId)
            .body(Json.createObjectBuilder().add("text", text).build().toString());
        sendRequest(req, callback);
    }

    public void deleteMessage(String serverId, String categoryId, String channelId, String messageId, Callback<JsonNode> callback) {
        HttpRequest<?> req = instance.delete(Constants.REST_SERVER_PATH + "/" + serverId + Constants.REST_CATEGORY_PATH + "/" + categoryId + Constants.REST_CHANNEL_PATH + "/" + channelId + Constants.REST_MESSAGES_PATH + "/" + messageId);
        sendRequest(req, callback);
    }

    public HttpResponse<JsonNode> updateDescription(String userId, String description) {
        HttpRequest<?> req = instance.post(Constants.REST_USERS_PATH + "/" + userId + Constants.REST_DESCRIPTION_PATH)
            .body(Json.createObjectBuilder().add("text", description).build().toString());
        return sendSyncRequest(req);
    }
}
