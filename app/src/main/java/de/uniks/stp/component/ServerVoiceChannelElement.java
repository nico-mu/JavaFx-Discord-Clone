package de.uniks.stp.component;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.modal.EditChannelModal;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.User;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.io.IOException;
import java.util.HashMap;

public class ServerVoiceChannelElement extends ServerChannelElement {

    @FXML
    private TextWithEmoteSupport channelText;

    @FXML
    private Pane channelElementMarker;

    @FXML
    private HBox channelContainer;

    @FXML
    private ImageView editChannel;

    @FXML
    private VBox audioMemberContainer;

    private Channel model;
    private HashMap<String, UserListEntry> userListEntryHashMap;
    private final ViewLoader viewLoader;
    private final EditChannelModal.EditChannelModalFactory editChannelModalFactory;

    @AssistedInject
    public ServerVoiceChannelElement(ViewLoader viewLoader,
                                     EditChannelModal.EditChannelModalFactory editChannelModalFactory,
                                     UserListEntry.UserListEntryFactory userListEntryFactory,
                                     @Assisted Channel model) {
        this.model = model;
        userListEntryHashMap = new HashMap<>();
        this.viewLoader = viewLoader;
        this.editChannelModalFactory = editChannelModalFactory;

        FXMLLoader fxmlLoader = viewLoader.getFXMLComponentLoader(Components.SERVER_VOICE_CHANNEL_ELEMENT);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        channelText.setText(model.getName());
        channelText.setFont(Font.font(16));
        channelContainer.setOnMouseClicked(this::onMouseClicked);

        channelContainer.setOnMouseEntered(this::onChannelMouseEntered);
        channelContainer.setOnMouseExited(this::onChannelMouseExited);

        editChannel.setOnMouseClicked(this::onEditChannelClicked);

        channelText.setId(model.getId() + "-ChannelElementText");
        for(User user : model.getAudioMembers()) {
            UserListEntry userListEntry = userListEntryFactory.create(user);
            userListEntry.setOnMouseClicked(null);
            userListEntry.setPadding(new Insets(0,0,0,12));
            userListEntryHashMap.put(user.getId(), userListEntry);
            audioMemberContainer.getChildren().add(userListEntry);
        }
    }

    private void onEditChannelClicked(MouseEvent mouseEvent) {
        Parent editChannelModalView = viewLoader.loadView(Views.EDIT_CHANNEL_MODAL);
        EditChannelModal editChannelModal = editChannelModalFactory.create(editChannelModalView, model);
        editChannelModal.show();
    }

    private void onChannelMouseExited(MouseEvent mouseEvent) {
        editChannel.setVisible(false);
    }

    private void onChannelMouseEntered(MouseEvent mouseEvent) {
        editChannel.setVisible(true);
    }

    public void updateText(String newName) {
        Platform.runLater(() -> {
            channelText.setText(newName);
        });
    }

    public void setActive(boolean active) {
        channelElementMarker.setVisible(active);
    }

    public String getChannelTextId() {
        return channelText.getId();
    }

    private void onMouseClicked(MouseEvent mouseEvent) {
        //TODO join voice channel
    }

    @Override
    public void setNotificationCount(int notifications) {
    }

    @AssistedFactory
    public interface ServerVoiceChannelElementFactory {
        ServerVoiceChannelElement create(Channel model);
    }
}
