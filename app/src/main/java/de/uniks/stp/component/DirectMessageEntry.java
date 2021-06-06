package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.User;
import de.uniks.stp.router.RouteArgs;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.IOException;

public class DirectMessageEntry extends HBox implements NotificationComponentInterface {

    @FXML
    private Text userNameText;
    private final User user;
    private final Font font;
    private final Font boldFont;

    public DirectMessageEntry(final User user) {
        this.user = user;
        final FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.USER_LIST_ENTRY);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        setUserName(user.getName());

        // Has to constant, value is located in user-list.scss
        font = Font.font(userNameText.getFont().getFamily(), FontWeight.NORMAL, 16.0);
        boldFont = Font.font(userNameText.getFont().getFamily(), FontWeight.BOLD, 16.0);
        this.setId(user.getId() + "-DirectMessageEntry");

        userNameText.setOnMouseClicked(this::handleClick);
    }

    private void handleClick(MouseEvent mouseEvent) {
        RouteArgs args = new RouteArgs().addArgument(Constants.ROUTE_PRIVATE_CHAT_ARGS, user.getId());
        Router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_PRIVATE_CHAT, args);
    }

    public void setUserName(final String userName) {
        userNameText.setText(userName);
    }

    @Override
    public void setNotificationCount(int notifications) {
        setNotificationVisibility(0 < notifications);
    }

    @Override
    public void setNotificationVisibility(boolean mode) {
        Platform.runLater(() -> {
            if (mode) {
                userNameText.setFont(boldFont);
            } else {
                userNameText.setFont(font);
            }
        });
    }
}
