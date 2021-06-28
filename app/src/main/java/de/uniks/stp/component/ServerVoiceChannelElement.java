package de.uniks.stp.component;

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
import java.util.Objects;

public class ServerVoiceChannelElement extends ServerChannelElement {

    @FXML
    TextWithEmoteSupport channelText;

    @FXML
    Pane channelElementMarker;

    @FXML
    HBox channelContainer;

    @FXML
    ImageView editChannel;

    @FXML
    VBox audioMemberContainer;

    Channel model;
    private Editor editor;
    private final ListComponent<User, VoiceUserListEntry> voiceUserListComponent;

    public ServerVoiceChannelElement(Channel model, Editor editor) {
        this.editor = editor;
        this.model = model;

        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.SERVER_VOICE_CHANNEL_ELEMENT);
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
            Platform.runLater(() -> voiceUserListComponent.removeElement(user));
        }
    }

    public void addAudioUser(final User user) {
        if (Objects.nonNull(user)) {
            Platform.runLater(() -> voiceUserListComponent.addElement(user, new VoiceUserListEntry(user)));
        }
    }

    private void onEditChannelClicked(MouseEvent mouseEvent) {
        Parent editChannelModalView = ViewLoader.loadView(Views.EDIT_CHANNEL_MODAL);
        EditChannelModal editChannelModal = new EditChannelModal(editChannelModalView, model, editor);
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
}
