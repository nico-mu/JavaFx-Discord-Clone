package de.uniks.stp.controller;

import de.uniks.stp.Editor;
import de.uniks.stp.network.RestClient;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;

public class TestScreenController implements ControllerInterface {

    private final Editor editor;
    private final RestClient restClient;

    public TestScreenController(Editor editor) {
        this.editor = editor;
        this.restClient = new RestClient();
    }

    @Override
    public void init() {
        System.out.println("testscreencntrl init called");
        restClient.createServer("GUAVE_TEST_SERVER", editor.getOrCreateAccord().getUserKey(), this::handleCreateServerResponse);
    }

    private void handleCreateServerResponse(HttpResponse<JsonNode> response) {
        System.out.println("handleCreateServerResponse called");
        System.out.println(response.getBody());
        if (response.isSuccess()) {
            System.out.println("handle method called: server created");
        } else {
            System.out.println("create server failed");
        }
    }

    @Override
    public void stop() {

    }
}
