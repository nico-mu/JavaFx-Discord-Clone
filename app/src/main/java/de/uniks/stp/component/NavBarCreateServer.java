package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Server;
import de.uniks.stp.network.RestClient;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONObject;

public class NavBarCreateServer extends NavBarElement {

    private RestClient restClient = new RestClient();
    private Editor editor;

    public NavBarCreateServer(Editor editor) {
        this.editor = editor;
        Tooltip.install(navBarElement, new Tooltip(ViewLoader.loadLabel(Constants.LBL_CREATE_SERVER)));
        imageView.setImage(ViewLoader.loadImage("plus.png"));
    }

    @Override
    protected void onMouseClicked(MouseEvent mouseEvent) {
        super.onMouseClicked(mouseEvent);
        openServerNamePopup();
    }

    private void openServerNamePopup() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Server name");
        dialog.setHeaderText("Please enter the servername: ");
        dialog.setContentText("Servername: ");

        dialog.showAndWait().ifPresent((name) -> {
            if (name != null && !name.equals("")) {
                restClient.createServer(name, this::handleCreateServerResponse);
            } else {
                return;
            }
        });
    }

    private void handleCreateServerResponse(HttpResponse<JsonNode> response) {
        System.out.println(response.getBody());
        if (response.isSuccess()) {
            JSONObject jsonObject = response.getBody().getObject().getJSONObject("data");


            String name = jsonObject.getString("name");
            String serverId = jsonObject.getString("id");

            editor.getOrCreateAccord()
                .getCurrentUser()
                .withAvailableServers(new Server().setName(name).setId(serverId));


        } else {
            System.err.println("create server failed!");
        }
    }

}
