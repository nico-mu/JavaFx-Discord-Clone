package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.controller.LoginScreenController;
import de.uniks.stp.model.Category;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.User;
import de.uniks.stp.network.NetworkClientInjector;
import de.uniks.stp.network.RestClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AddChannelModal extends AbstractModal{
    private static final Logger log = LoggerFactory.getLogger(AddChannelModal.class);

    public static final String ADD_CHANNEL_NAME_TEXTFIELD = "#add-channel-name-textfield";
    public static final String PRIVILEGED_CHECKBOX = "#privileged-checkbox";
    public static final String FILTER_USER_TEXTFIELD = "#filter-user-textfield";
    public static final String ADD_CHANNEL_USER_LIST_SCROLLPANE = "#add-channel-user-list-scrollpane";
    public static final String ADD_CHANNEL_USER_LIST = "#add-channel-user-list";
    public static final String ADD_CHANNEL_CREATE_BUTTON = "#add-channel-create-button";
    public static final String ADD_CHANNEL_CANCEL_BUTTON = "#add-channel-cancel-button";
    public static final String ADD_CHANNEL_ERROR_LABEL = "#add-channel-error";
    private JFXTextField channelName;
    private JFXCheckBox privileged;
    private JFXTextField filter;
    private ScrollPane scrollPane;
    private VBox userCheckList;
    private JFXButton createButton;
    private JFXButton cancelButton;
    private Label errorLabel;
    private Category category;
    private Editor editor;
    private HashMap<String, HBox> serverUserElementsMap;
    private RestClient restClient;

    public AddChannelModal(Parent root, Category category, Editor editor){
        super(root);
        this.category = category;
        this.editor = editor;
        this.restClient = NetworkClientInjector.getRestClient();

        setTitle(ViewLoader.loadLabel(Constants.LBL_CREATE_CHANNEL));
        channelName = (JFXTextField) view.lookup(ADD_CHANNEL_NAME_TEXTFIELD);
        privileged = (JFXCheckBox) view.lookup(PRIVILEGED_CHECKBOX);
        filter = (JFXTextField) view.lookup(FILTER_USER_TEXTFIELD);
        scrollPane = (ScrollPane) view.lookup(ADD_CHANNEL_USER_LIST_SCROLLPANE);
        createButton = (JFXButton) view.lookup(ADD_CHANNEL_CREATE_BUTTON);
        cancelButton = (JFXButton) view.lookup(ADD_CHANNEL_CANCEL_BUTTON);
        errorLabel = (Label) view.lookup(ADD_CHANNEL_ERROR_LABEL);

        userCheckList = new VBox();
        scrollPane.setContent(userCheckList);

        createButton.setOnAction(this::onCreatButtonClicked);
        cancelButton.setOnAction(this::onCancelButtonClicked);
        cancelButton.setCancelButton(true);  // use Escape in order to press button
        filter.textProperty().addListener(((observable, oldValue, newValue) -> {
            filterUsers(newValue);
        }));

        serverUserElementsMap = new HashMap();

        for(User user : category.getServer().getUsers()){
            /*if(user.getName().equals(editor.getOrCreateAccord().getCurrentUser().getName())){
                continue;
            }*/
            JFXCheckBox checkBox = new JFXCheckBox();
            checkBox.setId("checkbox");

            Label userName = new Label();
            userName.setId("user-name");
            userName.setText(user.getName());

            HBox listElement = new HBox();
            listElement.setSpacing(12);
            listElement.getChildren().addAll(checkBox, userName);

            serverUserElementsMap.put(user.getName(), listElement);
        }

        userCheckList.getChildren().addAll(serverUserElementsMap.values());
    }

    private void filterUsers(String newValue) {
        userCheckList.getChildren().clear();
        for(String name : serverUserElementsMap.keySet()){
            if(name.contains(newValue)){
                userCheckList.getChildren().add(serverUserElementsMap.get(name));
            }
        }
    }

    private void onCreatButtonClicked(ActionEvent actionEvent) {
        setErrorMessage(null);
        String chName = channelName.getText();
        Boolean priv = privileged.isSelected();
        String type = "text";
        String serverId = category.getServer().getId();
        String categoryId = category.getId();

        ArrayList<String> selectedUserNames = new ArrayList<>();
        //selectedUserNames.add(editor.getOrCreateAccord().getCurrentUser().getName());
        for(HBox userElement : serverUserElementsMap.values()){
            JFXCheckBox checkBox = (JFXCheckBox) userElement.lookup("#checkbox");
            if(checkBox.isSelected()){
                Label name = (Label) userElement.lookup("#user-name");
                selectedUserNames.add(name.getText());
            }
        }
        restClient.createTextChannel(serverId,categoryId,chName,priv, selectedUserNames, this::handleCreateChannelResponse);
    }

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

    private void handleCreateChannelResponse(HttpResponse<JsonNode> jsonNodeHttpResponse) {
        log.debug("Received create server response: " + jsonNodeHttpResponse.getBody().toPrettyString());

        if(jsonNodeHttpResponse.isSuccess()){
            JSONObject data = jsonNodeHttpResponse.getBody().getObject().getJSONObject("data");
            String chId = data.getString("id");
            String chName = data.getString("name");
            String type = data.getString("type");
            Boolean privileged = data.getBoolean("privileged");

            Channel channel = new Channel().setId(chId).setName(chName).setType(type).setPrivileged(privileged);
            category.withChannels(channel);

            if(privileged){
                JSONArray membersArray = data.getJSONArray("members");
                ArrayList<String> memberNames = (ArrayList<String>) membersArray.toList();
                //TODO add members to channel
            }

        }else{
            log.error("Create server failed: " + jsonNodeHttpResponse.getBody().toPrettyString());
            String errorMessage = jsonNodeHttpResponse.getBody().getObject().getString("message");
            if(errorMessage.equals("Missing name")){
                setErrorMessage(Constants.LBL_MISSING_NAME);
            }else if (errorMessage.equals("Missing members")){
                setErrorMessage(Constants.LBL_MISSING_MEMBERS);
            }else{
                log.error("Unhandled create server response: " + jsonNodeHttpResponse.getBody().toPrettyString());
            }
        }
    }

    private void onCancelButtonClicked(ActionEvent actionEvent) {
        this.close();
    }

    @Override
    public void close(){
        createButton.setOnAction(null);
        cancelButton.setOnAction(null);
        super.close();
    }
}
