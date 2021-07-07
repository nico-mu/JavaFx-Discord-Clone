package de.uniks.stp.component;

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
import java.util.Date;

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
        Platform.runLater(() -> messageText.setText(newText));
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

    @AssistedFactory
    public interface ChatMessageFactory {
        ChatMessage create(Message message, boolean editable);
    }
}
