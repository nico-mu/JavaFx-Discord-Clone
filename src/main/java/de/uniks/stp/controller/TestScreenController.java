package de.uniks.stp.controller;

import de.uniks.stp.Editor;
import de.uniks.stp.network.RestClient;
import javafx.scene.control.TextInputDialog;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;

public class TestScreenController implements ControllerInterface {

    private final Editor editor;
    private final RestClient restClient;
    private String serverName;

    public TestScreenController(Editor editor) {
        this.editor = editor;
        this.restClient = new RestClient();
    }

    @Override
    public void init() {
        //openServerNamePopup();
        restClient.createServer("GUAVE_TEST_SERVER", editor.getOrCreateAccord().getUserKey(), this::handleCreateServerResponse);
    }

    //FIXME: doesn't work because fxml file is missing
    private void openServerNamePopup() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Server name");
        dialog.setHeaderText("Please the servername: ");
        dialog.setContentText("Servername: ");

        dialog.showAndWait().ifPresent((name) -> {
            //do it like this?
            if (name != null && !name.equals("")) {
                restClient.createServer(name, editor.getOrCreateAccord().getUserKey(), this::handleCreateServerResponse);
            } else {
                return;
            }
        });
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
