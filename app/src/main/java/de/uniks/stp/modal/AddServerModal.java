package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import de.uniks.stp.notification.NotificationService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Objects;

public class AddServerModal extends AbstractModal {
    public static final String ADD_SERVER_CREATE_BUTTON = "#add-server-create-button";
    public static final String ADD_SERVER_CANCEL_BUTTON = "#add-server-cancel-button";
    public static final String ADD_SERVER_TEXT_FIELD_SERVERNAME = "#servername-text-field";
    public static final String ADD_SERVER_ERROR_LABEL = "#error-message-label";
    public static final String ADD_SERVER_SPINNER = "#spinner";

    private final Editor editor;
    private final JFXButton createButton;
    private final JFXButton cancelButton;
    private final JFXTextField servernameTextField;
    private final Label errorLabel;
    private final JFXSpinner spinner;
    private final RestClient restClient;

    private static final Logger log = LoggerFactory.getLogger(AddServerModal.class);

    public AddServerModal(Parent root, Editor editor) {
        super(root);
        this.editor = editor;
        restClient = NetworkClientInjector.getRestClient();

        setTitle(ViewLoader.loadLabel(Constants.LBL_ADD_SERVER_TITLE));

        createButton = (JFXButton) view.lookup(ADD_SERVER_CREATE_BUTTON);
        cancelButton = (JFXButton) view.lookup(ADD_SERVER_CANCEL_BUTTON);
        servernameTextField = (JFXTextField) view.lookup(ADD_SERVER_TEXT_FIELD_SERVERNAME);
        errorLabel = (Label) view.lookup(ADD_SERVER_ERROR_LABEL);
        spinner = (JFXSpinner) view.lookup(ADD_SERVER_SPINNER);

        createButton.setOnAction(this::onApplyButtonClicked);
        createButton.setDefaultButton(true);  // use Enter in order to press button
        cancelButton.setOnAction(this::onCancelButtonClicked);
        cancelButton.setCancelButton(true);  // use Escape in order to press button
    }

    @Override
    public void close() {
        createButton.setOnAction(null);
        cancelButton.setOnAction(null);
        super.close();
    }

    private void onCancelButtonClicked(ActionEvent actionEvent) {
        this.close();
    }

    /**
     * If TextField is not empty, disables TextField and Button, shows Spinner and sends rest call to create server
     * If TextField is empty, it shows an error message
     * @param actionEvent
     */
    private void onApplyButtonClicked(ActionEvent actionEvent) {
        String name = servernameTextField.getText();
        if (! name.isEmpty()){
            setErrorMessage(null);
            servernameTextField.setDisable(true);
            createButton.setDisable(true);
            cancelButton.setDisable(true);
            spinner.setVisible(true);

            RestClient restClient = NetworkClientInjector.getRestClient();
            restClient.createServer(name, this::handleCreateServerResponse);
        }
        else{
            setErrorMessage(Constants.LBL_MISSING_NAME);
        }
    }

    /**
     * If successful, it creates new Server Object, inserts it in model and closes this Modal Window.
     * If not successful, it shows an error message, hides the spinner and enables the TextField and Buttons.
     * @param response
     */
    private void handleCreateServerResponse(HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toPrettyString());

        if (response.isSuccess()) {
            JSONObject jsonObject = response.getBody().getObject().getJSONObject("data");
            String name = jsonObject.getString("name");
            String serverId = jsonObject.getString("id");

            Server server = new Server().setName(name).setId(serverId);
            editor.getOrCreateAccord()
                .getCurrentUser()
                .withAvailableServers(server);
            Platform.runLater(this::close);
            restClient.getServerInformation(serverId, this::handleServerInformationRequest);
            restClient.getCategories(server.getId(), (msg) -> handleCategories(msg, server));
        } else {
            log.error("create server failed!");
            setErrorMessage(Constants.LBL_CREATE_SERVER_FAILED);

            Platform.runLater(() -> {
                servernameTextField.setDisable(false);
                createButton.setDisable(false);
                cancelButton.setDisable(false);
                spinner.setVisible(false);
            });
        }
    }

    private void handleServerInformationRequest(HttpResponse<JsonNode> response) {
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

    private void handleCategories(HttpResponse<JsonNode> response, Server server) {
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

    private void handleChannels(HttpResponse<JsonNode> response, Server server) {
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

                Category categoryModel = editor.getCategory(categoryId, server);
                Channel channelModel = editor.getChannel(channelId, server);
                if (Objects.nonNull(channelModel)) {
                    // Channel is already in model because it got added by a notification
                    channelModel.setCategory(categoryModel).setName(name);
                } else {
                    channelModel = editor.getOrCreateChannel(channelId, name, categoryModel);
                    channelModel.setServer(server);
                }
                channelModel.setType(type);
                channelModel.setPrivileged(privileged);
                for(User user : server.getUsers()) {
                    if(memberIds.contains(user.getId())) {
                        channelModel.withChannelMembers(user);
                    }
                }
                NotificationService.register(channelModel);
            }
        } else {
            //TODO: show error message
        }
    }

    /**
     * Changes ErrorLabel to display given error.
     *
     * @param label Constant of resource label
     */
    private void setErrorMessage(String label) {
        if (Objects.isNull(label)) {
            Platform.runLater(() -> {
                errorLabel.setText("");
            });
            return;
        }
        String message = ViewLoader.loadLabel(label);
        Platform.runLater(() -> {
            errorLabel.setText(message);
        });
    }
}
