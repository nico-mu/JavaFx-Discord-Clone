package de.uniks.stp.component;

import com.jfoenix.controls.JFXSpinner;
import com.sun.javafx.webkit.Accessor;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
    private ImageView editMessage;

    @FXML
    private ImageView deleteMessage;

    private Message model;

    public ChatMessage(Message message, String language, boolean editable) {
        this.model = message;
        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.CHAT_MESSAGE);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        timestampText.setText(DateUtil.formatTime(message.getTimestamp(), Locale.forLanguageTag(language)));
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

    public void addJoinButtonButton(InviteInfo inviteInfo, EventHandler<ActionEvent> onButtonPressed) {
        JoinServerButton button = new JoinServerButton(inviteInfo, onButtonPressed);
        Platform.runLater(() -> textVBox.getChildren().add(button));
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
        Parent editMessageModalView = ViewLoader.loadView(Views.EDIT_MESSAGE_MODAL);
        EditMessageModal editMessageModal = new EditMessageModal(editMessageModalView, (ServerMessage) model);
        editMessageModal.show();
    }

    private void onMessageDelete(MouseEvent mouseEvent) {
        new DeleteMessageModal((ServerMessage) model);
    }

    public void addSpinner() {
        Platform.runLater(() -> {
            VBox vBox = new VBox();
            vBox.setPrefHeight(100);
            vBox.setPrefWidth(100);
            vBox.setAlignment(Pos.CENTER);
            JFXSpinner spinner = new JFXSpinner();
            vBox.getChildren().add(spinner);
            textVBox.getChildren().add(vBox);
        });
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
            if (content.equals("")) {
                String media = "<head>\n" +
                    "    <link href=\"http://vjs.zencdn.net/c/video-js.css\" rel=\"stylesheet\">\n" +
                    "    <script src=\"http://vjs.zencdn.net/c/video.js\"></script>\n" +
                    "</head>\n" +
                    "<body overflow=\"hidden\">\n" +
                    "    <video id=\"video1\" class=\"video-js vjs-default-skin\"\n" +
                    "        data-setup='{\"controls\" : true, \"autoplay\" : false, \"preload\" : \"auto\"}'>\n" +
                    "        <source src=\"" + url + "\" type=\"video/mp4\">\n" +
                    "    </video>\n" +
                    "</body>";
                webView.getEngine().loadContent(media, "text/html");
            }else {
                webView.getEngine().loadContent(content, "text/html");
            }
            webView.setMaxHeight(240);
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

    public void stop() {
        List<Node> toRemove = new ArrayList<>();
        for (Node node : textVBox.getChildren()) {
            if (node instanceof WebView) {
                WebView webView = (WebView) node;
                webView.getEngine().load(null);
                toRemove.add(node);
            }
        }
        for (Node node : toRemove) {
            textVBox.getChildren().remove(node);
        }
    }
}
