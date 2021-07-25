package de.uniks.stp.component;

import com.jfoenix.controls.JFXSpinner;
import com.sun.javafx.webkit.Accessor;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.modal.DeleteMessageModal;
import de.uniks.stp.modal.EditMessageModal;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.ServerMessage;
import de.uniks.stp.util.DateUtil;
import de.uniks.stp.util.InviteInfo;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

public class ChatMessage extends HBox {

    @FXML
    private TextWithEmoteSupport messageText;

    @FXML
    private Text nameText;

    @FXML
    private Text timestampText;

    @FXML
    private VBox textVBox;

    @FXML
    private VBox editMessage;

    @FXML
    private VBox deleteMessage;

    private final Message model;
    private final ViewLoader viewLoader;
    private final DeleteMessageModal.DeleteMessageModalFactory deleteMessageModalFactory;
    private final EditMessageModal.EditMessageModalFactory editMessageModalFactory;
    private final JoinServerButton.JoinServerButtonFactory joinServerButtonFactory;

    @AssistedInject
    public ChatMessage(ViewLoader viewLoader,
                       DeleteMessageModal.DeleteMessageModalFactory deleteMessageModalFactory,
                       EditMessageModal.EditMessageModalFactory editMessageModalFactory,
                       JoinServerButton.JoinServerButtonFactory joinServerButtonFactory,
                       @Assisted Message message,
                       @Assisted boolean editable) {
        FXMLLoader fxmlLoader = viewLoader.getFXMLComponentLoader(Components.CHAT_MESSAGE);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.model = message;
        this.viewLoader = viewLoader;
        this.deleteMessageModalFactory = deleteMessageModalFactory;
        this.editMessageModalFactory = editMessageModalFactory;
        this.joinServerButtonFactory = joinServerButtonFactory;

        timestampText.setText(getTimestampText());
        nameText.setText(message.getSender().getName());
        messageText.setText(message.getMessage());

        this.setId("message-" + model.getId());
        messageText.setId("message-text-" + model.getId());
        editMessage.setId("edit-message-" + model.getId());
        deleteMessage.setId("delete-message-" + model.getId());

        if (editable) {
            textVBox.setOnMouseEntered(this::onMouseEntered);
            textVBox.setOnMouseExited(this::onMouseExited);
            editMessage.setOnMouseClicked(this::onMessageEdited);
            deleteMessage.setOnMouseClicked(this::onMessageDelete);
        }
        editMessage.setVisible(false);
        deleteMessage.setVisible(false);
    }

    private String getTimestampText() {
        long timestamp = model.getTimestamp();
        Date date = new Date();
        date.setTime(timestamp);
        String dateString = DateUtil.formatTimeWithLocale(timestamp, viewLoader.getCurrentLocale());
        StringBuilder builder = new StringBuilder();

        if (DateUtil.isToday(date)) {
            return builder
                .append(viewLoader.loadLabel(Constants.LBL_TIME_FORMATTING_TODAY))
                .append(", ")
                .append(dateString)
                .toString();
        } else if (DateUtil.isYesterday(date)) {
            return builder
                .append(viewLoader.loadLabel(Constants.LBL_TIME_FORMATTING_YESTERDAY))
                .append(", ")
                .append(dateString)
                .toString();
        }
        return dateString;
    }

    public void addJoinButtonButton(InviteInfo inviteInfo, EventHandler<ActionEvent> onButtonPressed){
        JoinServerButton button = joinServerButtonFactory.create(inviteInfo, onButtonPressed);
        Platform.runLater(()-> textVBox.getChildren().add(button));
    }

    public void setMessageText(String newText) {
        Platform.runLater(() -> {
            messageText.setText(newText);
            textVBox.getChildren().removeIf(node -> node instanceof WebView);
        });
    }

    private void onMouseExited(MouseEvent mouseEvent) {
        editMessage.setVisible(false);
        deleteMessage.setVisible(false);
    }

    private void onMouseEntered(MouseEvent mouseEvent) {
        editMessage.setVisible(true);
        deleteMessage.setVisible(true);
    }

    private void onMessageEdited(MouseEvent mouseEvent) {
        Parent editMessageModalView = viewLoader.loadView(Views.EDIT_MESSAGE_MODAL);
        EditMessageModal editMessageModal = editMessageModalFactory.create(editMessageModalView, (ServerMessage) model);
        editMessageModal.show();
    }

    private void onMessageDelete(MouseEvent mouseEvent) {
        deleteMessageModalFactory.create((ServerMessage) model);
    }

    public void addVideo(String content, String url) {
        loadContent(content, false, url);
    }

    public void addImage(String content, String url) {
        loadContent(content, true, url);
    }

    private void loadContent(String content, boolean notIntractable, String url) {
        Platform.runLater(() -> {

            WebView webView = new WebView();
            if (content.isEmpty()) {
                if (url.isEmpty()) {
                    return;
                }
                webView.getEngine().loadContent("<video width=\"320\" height=\"240\" controls >\n" +
                    "  <source src=\"" + url + "\" type=\"video/mp4\">\n" +
                    "</video>", "text/html");
            } else {
                webView.getEngine().loadContent(content, "text/html");
            }
            webView.setMaxHeight(250);
            webView.setMaxWidth(500);
            Accessor.getPageFor(webView.getEngine()).setBackgroundColor(0);
            webView.setMouseTransparent(notIntractable);
            webView.addEventFilter(ScrollEvent.SCROLL, Event::consume);
            // disables scrollbars on images and videos:
            webView.getChildrenUnmodifiable().addListener((ListChangeListener<Node>) change -> {
                Set<Node> scrollBars = webView.lookupAll(".scroll-bar");
                for (Node scroll : scrollBars) {
                    scroll.setVisible(false);
                }
            });
            textVBox.getChildren().add(webView);
        });
    }

    @AssistedFactory
    public interface ChatMessageFactory {
        ChatMessage create(Message message, boolean editable);
    }

    public void cleanUp() {
        for (Node node : textVBox.getChildren()) {
            if (node instanceof WebView) {
                WebView webView = (WebView) node;
                webView.getEngine().load(null);
            }
        }
        textVBox.getChildren().clear();
    }
}
