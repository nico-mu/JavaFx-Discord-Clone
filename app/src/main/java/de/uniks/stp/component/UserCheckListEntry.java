package de.uniks.stp.component;

import com.jfoenix.controls.JFXCheckBox;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
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

    @AssistedInject
    public UserCheckListEntry(ViewLoader viewLoader,
                              @Assisted final User user) {
        final FXMLLoader fxmlLoader = viewLoader.getFXMLComponentLoader(Components.USER_CHECK_LIST_ENTRY);
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

    public boolean isUserSelected(){
        return checkbox.isSelected();
    }

    public void setSelected(boolean selected) {
        checkbox.setSelected(selected);
    }

    @AssistedFactory
    public interface UserCheckListEntryFactory {
        UserCheckListEntry create(User user);
    }
}
