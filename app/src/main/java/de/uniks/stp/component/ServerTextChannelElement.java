package de.uniks.stp.component;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
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

import java.io.IOException;

public class ServerTextChannelElement extends ServerChannelElement implements NotificationComponentInterface {

    @FXML
    private TextWithEmoteSupport channelText;

    @FXML
    private Pane channelElementMarker;

    @FXML
    private VBox channelVBox;

    @FXML
    private HBox channelContainer;

    @FXML
    private ImageView editChannel;

    private final Channel model;

    private Font font = null;
    private Font boldFont = null;
    private final Router router;
    private final ViewLoader viewLoader;
    private final EditChannelModal.EditChannelModalFactory editChannelModalFactory;

    @AssistedInject
    public ServerTextChannelElement(ViewLoader viewLoader,
                                    Router router,
                                    EditChannelModal.EditChannelModalFactory editChannelModalFactory,
                                    @Assisted Channel model) {
        FXMLLoader fxmlLoader = viewLoader.getFXMLComponentLoader(Components.SERVER_TEXT_CHANNEL_ELEMENT);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        this.model = model;
        this.router = router;
        this.viewLoader = viewLoader;
        this.editChannelModalFactory = editChannelModalFactory;
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
        router.route(Constants.ROUTE_MAIN + Constants.ROUTE_SERVER + Constants.ROUTE_CHANNEL, args);
    }

    @AssistedFactory
    public interface ServerTextChannelElementFactory {
        ServerTextChannelElement create(Channel model);
    }
}
