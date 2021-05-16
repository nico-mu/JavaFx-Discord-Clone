package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Server;
import de.uniks.stp.network.RestClient;
import javafx.application.Platform;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class NavBarCreateServer extends NavBarElement {
    private static final Logger log = LoggerFactory.getLogger(NavBarCreateServer.class);

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
        String title = ViewLoader.loadLabel(Constants.LBL_SERVERNAME_TITLE);
        String headerText = ViewLoader.loadLabel(Constants.LBL_ENTER_SERVERNAME_PROMPT);
        String contentText = ViewLoader.loadLabel(Constants.LBL_SERVERNAME);
        Platform.runLater(() -> {
            dialog.setWidth(280);
            dialog.setTitle(title);
            dialog.setHeaderText(headerText);
            dialog.setContentText(contentText);
        });

        dialog.showAndWait().ifPresent((name) -> {
            if (name != null && !name.equals("")) {
                restClient.createServer(name, this::handleCreateServerResponse);
            } else {
                return;
            }
        });
    }

    private void handleCreateServerResponse(HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toPrettyString());

        if (response.isSuccess()) {
            JSONObject jsonObject = response.getBody().getObject().getJSONObject("data");
            String name = jsonObject.getString("name");
            String serverId = jsonObject.getString("id");

            editor.getOrCreateAccord()
                .getCurrentUser()
                .withAvailableServers(new Server().setName(name).setId(serverId));
        } else {
            log.error("create server failed!");
        }
    }

}
