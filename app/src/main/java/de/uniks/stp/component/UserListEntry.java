package de.uniks.stp.component;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.User;
import de.uniks.stp.network.integration.Integrations;
import de.uniks.stp.util.IntegrationUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Objects;

public class UserListEntry extends VBox {

    @FXML
    private Label userNameLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private ImageView descriptionImageView;

    protected final User user;
    protected final ViewLoader viewLoader;

    @AssistedInject
    public UserListEntry(ViewLoader viewLoader,
                         @Assisted final User user) {
        final FXMLLoader fxmlLoader = viewLoader.getFXMLComponentLoader(Components.USER_LIST_ENTRY);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        this.setId(user.getId() + "-UserListEntry");

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.user = user;
        this.viewLoader = viewLoader;

        setUserName(user.getName());
        setDescription(user.getDescription());
    }

    public void setDescription(final String description) {
        if(Objects.nonNull(description) && !description.isEmpty() && !description.isBlank()) {
            Integrations integration = IntegrationUtil.getIntegrationForToken(description.substring(0, 1));
            if(Objects.nonNull(integration)) {
                String pictureName = IntegrationUtil.getPictureNameForIntegration(integration);
                descriptionImageView.setImage(viewLoader.loadImage(pictureName));
            }
            final String parsedDescription = description.substring(1);

            descriptionLabel.setVisible(true);
            descriptionLabel.setManaged(true);
            descriptionImageView.setVisible(true);
            descriptionImageView.setManaged(true);
            descriptionLabel.setText(parsedDescription);
        }
        else {
            descriptionLabel.setVisible(false);
            descriptionLabel.setManaged(false);
            descriptionImageView.setVisible(false);
            descriptionImageView.setManaged(false);
        }
    }

    public void setUserName(final String userName) {
        userNameLabel.setText(userName);
    }

    @AssistedFactory
    public interface UserListEntryFactory {
        UserListEntry create(User user);
    }
}
