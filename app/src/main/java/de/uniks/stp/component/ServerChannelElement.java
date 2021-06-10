package de.uniks.stp.component;

import de.uniks.stp.Constants;
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
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;

public class ServerChannelElement extends HBox implements NotificationComponentInterface {

    @FXML
    HBox channelText;

    @FXML
    Pane channelElementMarker;

    @FXML
    VBox channelVBox;

    @FXML
    HBox channelContainer;

    @FXML
    ImageView editChannel;

    Channel model;

    private final Font font = null;
    private final Font boldFont = null;
    private final EmoteRenderer renderer = new EmoteRenderer();

    public ServerChannelElement(Channel model) {
        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.SERVER_CHANNEL_ELEMENT);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        this.model = model;
        // TODO: Long names make problems
        renderer.setEmoteRenderStrategy(renderer::imageEmoteRenderStrategy).setScalingFactor(2);
        channelText.maxHeight(100);
        channelText.getChildren().clear();
        channelText.maxWidth(20);
        channelText.setPrefWidth(20);
        this.setMaxWidth(30);
        renderer.renderInto(model.getName(), channelText);
        channelVBox.setOnMouseClicked(this::onMouseClicked);

        channelContainer.setOnMouseEntered(this::onChannelMouseEntered);
        channelContainer.setOnMouseExited(this::onChannelMouseExited);

        editChannel.setOnMouseClicked(this::onEditChannelClicked);

        // font = channelText.getFont();
        // boldFont = Font.font(channelText.getFont().getFamily(), FontWeight.BOLD, channelText.getFont().getSize());
        channelText.setId(model.getId() + "-ChannelElementText");
    }

    private void onEditChannelClicked(MouseEvent mouseEvent) {
        Parent editChannelModalView = ViewLoader.loadView(Views.EDIT_CHANNEL_MODAL);
        EditChannelModal editChannelModal = new EditChannelModal(editChannelModalView, model);
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
            channelText.getChildren().clear();
            renderer.renderInto(newName, channelText);
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
                // channelText.setFont(boldFont);
            } else {
                // channelText.setFont(font);
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
