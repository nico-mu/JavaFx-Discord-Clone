package de.uniks.stp.component;

import com.jfoenix.controls.JFXCheckBox;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class UserCheckListEntry extends HBox {

    @FXML
    private JFXCheckBox checkbox;
    private final User user;

    public UserCheckListEntry(final User user) {
        final FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.USER_CHECK_LIST_ENTRY);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.user = user;

        setUserName(user.getName());
    }



    public void setUserName(final String userName) {
        checkbox.setText(userName);
    }

    public String getUserName(){
        return checkbox.getText();
    }

    public String getUserId(){
        return user.getId();
    }

    public Boolean isUserSelected(){
        return checkbox.isSelected();
    }
}
