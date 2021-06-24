package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.emote.EmoteRenderer;
import de.uniks.stp.event.ChannelChangeEvent;
import de.uniks.stp.modal.EditChannelModal;
import de.uniks.stp.model.Channel;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;

public class ServerTextChannelElement extends ServerChannelElement implements NotificationComponentInterface {

    @FXML
    TextWithEmoteSupport channelText;

    @FXML
    Pane channelElementMarker;

    @FXML
    VBox channelVBox;

    @FXML
    HBox channelContainer;

    @FXML
    ImageView editChannel;

    Channel model;

    private Font font = null;
    private Font boldFont = null;
    private Editor editor;

    public ServerTextChannelElement(Channel model, Editor editor) {
        this.editor = editor;
        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.SERVER_TEXT_CHANNEL_ELEMENT);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        this.model = model;
        // TODO: Long names create problems
        channelText.setText(model.getName());
        channelText.setFont(Font.font(16));
        channelVBox.setOnMouseClicked(this::onMouseClicked);

        channelContainer.setOnMouseEntered(this::onChannelMouseEntered);
        channelContainer.setOnMouseExited(this::onChannelMouseExited);

        editChannel.setOnMouseClicked(this::onEditChannelClicked);

        for (Node node : channelText.getChildren()) {
            if (node instanceof Text) {
                Text sampleTextNode = ((Text) node);
                font = sampleTextNode.getFont();
                boldFont = Font.font(sampleTextNode.getFont().getFamily(), FontWeight.BOLD, sampleTextNode.getFont().getSize());
                break;
            }
        }
        channelText.setId(model.getId() + "-ChannelElementText");
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

    @Override
    public void setNotificationCount(int notifications) {
        setNotificationVisibility(0 < notifications);
    }

    @Override
    public void setNotificationVisibility(boolean mode) {
        Platform.runLater(() -> {
            if (mode) {
                channelText.setFont(boldFont);
            } else {
                channelText.setFont(font);
            }
        });
    }

    private void onMouseClicked(MouseEvent mouseEvent) {
        this.fireEvent(new ChannelChangeEvent(this));
        RouteArgs args = new RouteArgs();
        args.addArgument(":id", model.getCategory().getServer().getId());
        args.addArgument(":categoryId", model.getCategory().getId());
        args.addArgument(":channelId", model.getId());
        Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER + Constants.ROUTE_CHANNEL, args);
    }
}
