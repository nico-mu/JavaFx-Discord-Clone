package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.modal.AddServerModal;
import de.uniks.stp.model.Server;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.view.Views;
import javafx.scene.Parent;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NavBarCreateServer extends NavBarElement {
    private static final Logger log = LoggerFactory.getLogger(NavBarCreateServer.class);

    private final Editor editor;

    public NavBarCreateServer(Editor editor) {
        this.editor = editor;
        Tooltip.install(navBarElement, new Tooltip(ViewLoader.loadLabel(Constants.LBL_CREATE_SERVER)));
        imageView.setImage(ViewLoader.loadImage("plus.png"));
    }

    @Override
    protected void onMouseClicked(MouseEvent mouseEvent) {
        super.onMouseClicked(mouseEvent);
        Parent addServerModalView = ViewLoader.loadView(Views.ADD_SERVER_MODAL);
        AddServerModal addServerModal = new AddServerModal(addServerModalView, this::createServer);
        addServerModal.showAndWait();
    }

    private void createServer(String name){
        RestClient restClient = NetworkClientInjector.getRestClient();
        restClient.createServer(name, this::handleCreateServerResponse);
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
