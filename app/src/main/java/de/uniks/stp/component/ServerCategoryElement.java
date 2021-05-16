package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import de.uniks.stp.model.Category;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ServerCategoryElement extends VBox {

    @FXML
    ImageView categoryHeadArrow;

    @FXML
    Label categoryHeadLabel;

    @FXML
    VBox categoryChannelList;
    boolean channelListCollapsed = false;

    public ServerCategoryElement(Category model) {
        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.SERVER_CATEGORY_ELEMENT);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        categoryHeadLabel.setText(model.getName());
        categoryHeadArrow.setOnMouseClicked(this::onCategoryArrowClicked);
        categoryHeadLabel.setOnMouseClicked(this::onCategoryArrowClicked);
    }

    private void onCategoryArrowClicked(MouseEvent mouseEvent) {
        channelListCollapsed = !channelListCollapsed;
        if(channelListCollapsed) {
            categoryHeadArrow.setImage(ViewLoader.loadImage("right-arrow.png"));
            categoryChannelList.setVisible(false);
            categoryChannelList.setManaged(false);
        }
        else {
            categoryHeadArrow.setImage(ViewLoader.loadImage("down-arrow.png"));
            categoryChannelList.setVisible(true);
            categoryChannelList.setManaged(true);
        }
    }

    public void addChannelElement(ServerChannelElement serverChannelElement) {
        categoryChannelList.getChildren().add(serverChannelElement);
    }

    public void removeChannelElement(ServerChannelElement serverChannelElement) {
        categoryChannelList.getChildren().remove(serverChannelElement);
    }
}


