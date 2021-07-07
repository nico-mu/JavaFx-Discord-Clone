package de.uniks.stp.component;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.event.ChannelChangeEvent;
import de.uniks.stp.modal.EditChannelModal;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.User;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

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
    private Editor editor;
    private final ListComponent<User, VoiceUserListEntry> voiceUserListComponent;

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
        voiceUserListComponent = new ListComponent<>();
        audioMemberContainer.getChildren().add(voiceUserListComponent);

        channelText.setText(model.getName());
        channelText.setFont(Font.font(16));
        channelContainer.setOnMouseClicked(this::onMouseClicked);

        channelContainer.setOnMouseEntered(this::onChannelMouseEntered);
        channelContainer.setOnMouseExited(this::onChannelMouseExited);

        editChannel.setOnMouseClicked(this::onEditChannelClicked);

        channelText.setId(model.getId() + "-ChannelElementVoice");

        model.getAudioMembers().forEach(this::addAudioUser);
    }

    public void removeAudioUser(final User user) {
        if (Objects.nonNull(user)) {
            Platform.runLater(() -> {
                voiceUserListComponent.removeElement(user);
                resizeAudioMemberContainer();
            });
        }
    }

    private void resizeAudioMemberContainer() {
        int size = model.getAudioMembers().size();
        if (size > 5) {
            size = 5;
        }
        audioMemberContainer.setMinHeight(size * 30d);
    }

    public void addAudioUser(final User user) {
        if (Objects.nonNull(user)) {
            Platform.runLater(() -> {
                voiceUserListComponent.addElement(user, new VoiceUserListEntry(user));
                resizeAudioMemberContainer();
            });
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
        this.fireEvent(new ChannelChangeEvent(this));
        RouteArgs args = new RouteArgs();
        args.addArgument(":id", model.getCategory().getServer().getId());
        args.addArgument(":categoryId", model.getCategory().getId());
        args.addArgument(":channelId", model.getId());
        Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER + Constants.ROUTE_CHANNEL, args);
    }

    @Override
    public void setNotificationCount(int notifications) {
    }

    @AssistedFactory
    public interface ServerVoiceChannelElementFactory {
        ServerVoiceChannelElement create(Channel model);
    }
}
