package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.controller.MiniGameController;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.User;
import de.uniks.stp.util.DateUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.io.IOException;
import java.util.Locale;

public class PrivateChatMessage extends HBox {
    @FXML
    private TextWithEmoteSupport messageText;
    @FXML
    private Text nameText;
    @FXML
    private Text timestampText;
    @FXML
    private VBox textVBox;

    private String language;
    private User currentUser;

    public PrivateChatMessage(String language, User currentUser) {
        this.language = language;
        this.currentUser = currentUser;
        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.CHAT_MESSAGE);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void loadMessage(Message message) {
        timestampText.setText(DateUtil.formatTime(message.getTimestamp(), Locale.forLanguageTag(language)));
        timestampText.setText(DateUtil.formatTime(message.getTimestamp(), Locale.forLanguageTag(language)));
        nameText.setText(message.getSender().getName());

        // check for game invitation (easter egg)
        if(MiniGameController.isPlayMessage(message.getMessage())){
            if(message.getSender().getName().equals(currentUser.getName())){
                messageText.setText(ViewLoader.loadLabel(Constants.LBL_GAME_WAIT));
            } else{
                messageText.setText(ViewLoader.loadLabel(Constants.LBL_GAME_CHALLENGE));
            }
        } else{
            messageText.setText(message.getMessage());
        }
    }

    public void addButton(Pair<String, String> inviteIds, EventHandler<ActionEvent> onButtonPressed){
        JoinServerButton button = new JoinServerButton(inviteIds, onButtonPressed);
        Platform.runLater(()-> textVBox.getChildren().add(button));
    }
}
