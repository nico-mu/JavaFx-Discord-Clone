package de.uniks.stp.component;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.modal.EditChannelModal;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.User;
import de.uniks.stp.router.Router;
import de.uniks.stp.util.AnimationUtil;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.checkerframework.checker.units.qual.A;

import java.io.IOException;
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
    private HBox voiceChannelHBox;

    @FXML
    private VBox audioMemberContainer;

    private final Channel model;
    private final ViewLoader viewLoader;
    private final EditChannelModal.EditChannelModalFactory editChannelModalFactory;
    private final ListComponent<User, UserListEntry> voiceUserListComponent;
    private final UserListEntry.UserListEntryFactory userListEntryFactory;
    private final Router router;

    @AssistedInject
    public ServerVoiceChannelElement(ViewLoader viewLoader,
                                     Router router,
                                     EditChannelModal.EditChannelModalFactory editChannelModalFactory,
                                     UserListEntry.UserListEntryFactory userListEntryFactory,
                                     @Assisted Channel model) {
        this.model = model;
        this.viewLoader = viewLoader;
        this.editChannelModalFactory = editChannelModalFactory;
        this.userListEntryFactory = userListEntryFactory;
        this.router = router;

        FXMLLoader fxmlLoader = viewLoader.getFXMLComponentLoader(Components.SERVER_VOICE_CHANNEL_ELEMENT);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        voiceUserListComponent = new ListComponent<>(viewLoader);
        audioMemberContainer.getChildren().add(voiceUserListComponent);

        channelText.setText(model.getName());
        channelText.setFont(Font.font(16));
        voiceChannelHBox.setOnMouseClicked(this::onMouseClicked);

        channelContainer.setOnMouseEntered(this::onChannelMouseEntered);
        channelContainer.setOnMouseExited(this::onChannelMouseExited);

        AnimationUtil animationUtil = new AnimationUtil();
        animationUtil.setIconAnimation(editChannel);
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
        if (size > 3) {
            size = 3;
        }
        audioMemberContainer.setMinHeight(size * 35d);
    }

    public void addAudioUser(final User user) {
        if (Objects.nonNull(user)) {
            Platform.runLater(() -> {
                voiceUserListComponent.addElement(user, userListEntryFactory.create(user));
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
        super.onMouseClicked(model, router);
    }

    @Override
    public void setNotificationCount(int notifications) {
    }

    @AssistedFactory
    public interface ServerVoiceChannelElementFactory {
        ServerVoiceChannelElement create(Channel model);
    }
}
