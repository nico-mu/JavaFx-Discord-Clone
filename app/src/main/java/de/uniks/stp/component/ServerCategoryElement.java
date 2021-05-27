package de.uniks.stp.component;

import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.modal.AddChannelModal;
import de.uniks.stp.modal.AddServerModal;
import de.uniks.stp.model.Category;
import de.uniks.stp.view.Views;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ServerCategoryElement extends VBox {

    @FXML
    ImageView categoryHeadArrow;

    @FXML
    Label categoryHeadLabel;

    @FXML
    HBox categoryHeadPane;

    @FXML
    ImageView addServerPlus;

    @FXML
    VBox categoryChannelList;
    boolean channelListCollapsed = false;
    Editor editor;
    Category model;

    public ServerCategoryElement(Category model, Editor editor) {
        this.editor = editor;
        this.model = model;
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
        categoryHeadPane.setOnMouseEntered(event -> {
            addServerPlus.setVisible(true);
        });
        categoryHeadPane.setOnMouseExited(event -> {
            addServerPlus.setVisible(false);
        });
        addServerPlus.setOnMouseClicked(this::onAddServerPlusClicked);

    }

    private void onAddServerPlusClicked(MouseEvent mouseEvent) {
        //TODO load Modal
        Parent addChannelModalView = ViewLoader.loadView(Views.ADD_CHANNEL_MODAL);
        AddChannelModal addServerModal = new AddChannelModal(addChannelModalView, model, editor);
        addServerModal.showAndWait();
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


