package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import de.uniks.stp.modal.EditMessageModal;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.ServerMessage;
import de.uniks.stp.util.DateUtil;
import de.uniks.stp.util.InviteInfo;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.Locale;

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

        if (editable) {
            textVBox.setOnMouseEntered(this::onMouseEntered);
            textVBox.setOnMouseExited(this::onMouseExited);
            editMessage.setOnMouseClicked(this::onMessageEdited);
        }
        editMessage.setVisible(false);
    }

    public void addJoinButtonButton(InviteInfo inviteInfo, EventHandler<ActionEvent> onButtonPressed){
        JoinServerButton button = new JoinServerButton(inviteInfo, onButtonPressed);
        Platform.runLater(()-> textVBox.getChildren().add(button));
    }

    public void setMessageText(String newText) {
        Platform.runLater(() -> messageText.setText(newText));
    }

    private void onMouseExited(MouseEvent mouseEvent) {
        editMessage.setVisible(false);
    }

    private void onMouseEntered(MouseEvent mouseEvent) {
        editMessage.setVisible(true);
    }

    private void onMessageEdited(MouseEvent mouseEvent) {
        Parent editMessageModalView = ViewLoader.loadView(Views.EDIT_MESSAGE_MODAL);
        EditMessageModal editMessageModal = new EditMessageModal(editMessageModalView, (ServerMessage) model);
        editMessageModal.show();
    }
}
