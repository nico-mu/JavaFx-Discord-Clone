package de.uniks.stp.network.rest;

import de.uniks.stp.Editor;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.notification.NotificationService;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Objects;

public class ServerInformationHandler {
    private final Editor editor;
    private final SessionRestClient restClient;
    private final NotificationService notificationService;

    @Inject
    public ServerInformationHandler(Editor editor,
                                    SessionRestClient restClient,
                                    NotificationService notificationService) {
        this.editor = editor;
        this.restClient = restClient;
        this.notificationService = notificationService;
    }

    public void handleServerInformationRequest(HttpResponse<JsonNode> response) {
        if (response.isSuccess()) {
            final JSONObject data = response.getBody().getObject().getJSONObject("data");
            final JSONArray member = data.getJSONArray("members");
            final String serverId = data.getString("id");
            final String serverName = data.getString("name");
            final String serverOwner = data.getString("owner");

            // add server to model -> to NavBar List
            if (serverOwner.equals(editor.getOrCreateAccord().getCurrentUser().getId())) {
                editor.getOrCreateServer(serverId, serverName).setOwner(editor.getOrCreateAccord().getCurrentUser());
            } else {
                editor.getOrCreateServer(serverId, serverName);
            }

            member.forEach(o -> {
                JSONObject jsonUser = (JSONObject) o;
                String userId = jsonUser.getString("id");
                String name = jsonUser.getString("name");
                boolean status = Boolean.parseBoolean(jsonUser.getString("online"));

                User serverMember = editor.getOrCreateServerMember(userId, name, editor.getServer(serverId));
                serverMember.setStatus(status);
            });
        }
    }

    public void handleCategories(HttpResponse<JsonNode> response, Server server) {
        if (response.isSuccess()) {
            JSONArray categoriesJson = response.getBody().getObject().getJSONArray("data");
            for (Object category : categoriesJson) {
                JSONObject categoryJson = (JSONObject) category;
                final String name = categoryJson.getString("name");
                final String categoryId = categoryJson.getString("id");

                Category categoryModel = editor.getOrCreateCategory(categoryId, name, server);
                restClient.getChannels(server.getId(), categoryId, (msg) -> handleChannels(msg, server));
            }
        } else {
            //TODO: show error message
        }
    }

    public void handleChannels(HttpResponse<JsonNode> response, Server server) {
        if (response.isSuccess()) {
            JSONArray channelsJson = response.getBody().getObject().getJSONArray("data");
            for (Object channel : channelsJson) {
                JSONObject channelJson = (JSONObject) channel;
                final String name = channelJson.getString("name");
                final String channelId = channelJson.getString("id");
                final String categoryId = channelJson.getString("category");
                String type = channelJson.getString("type");
                boolean privileged = channelJson.getBoolean("privileged");
                JSONArray jsonMemberIds = channelJson.getJSONArray("members");
                ArrayList<String> memberIds = (ArrayList<String>) jsonMemberIds.toList();
                JSONArray jsonAudioMemberIds = channelJson.getJSONArray("audioMembers");
                ArrayList<String> audioMemberIds = (ArrayList<String>) jsonAudioMemberIds.toList();

                Category categoryModel = editor.getCategory(categoryId, server);
                Channel channelModel = editor.getChannel(channelId, server);
                if (Objects.nonNull(channelModel)) {
                    // Channel is already in model because it got added by a notification
                    channelModel.setCategory(categoryModel).setName(name);
                } else {
                    channelModel = editor.getOrCreateChannel(channelId, name, type, categoryModel);
                    channelModel.setServer(server);
                }
                channelModel.setType(type);
                channelModel.setPrivileged(privileged);
                for(User user : server.getUsers()) {
                    if(memberIds.contains(user.getId())) {
                        channelModel.withChannelMembers(user);
                    }
                    if(audioMemberIds.contains(user.getId())) {
                        channelModel.withAudioMembers(user);
                    }
                }
                notificationService.register(channelModel);
            }
        } else {
            //TODO: show error message
        }
    }
}
