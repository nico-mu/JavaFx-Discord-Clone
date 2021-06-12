package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import de.uniks.stp.emote.EmoteRenderer;
import de.uniks.stp.modal.AddChannelModal;
import de.uniks.stp.modal.EditCategoryModal;
import de.uniks.stp.model.Category;
import de.uniks.stp.view.Views;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;

import java.io.IOException;

public class ServerCategoryElement extends VBox {

    @FXML
    ImageView categoryHeadArrow;

    @FXML
    TextFlow categoryHeadLabel;

    @FXML
    HBox categoryHeadPane;

    @FXML
    ImageView addServerPlus;

    @FXML
    ImageView editCatGear;

    @FXML
    VBox categoryChannelList;
    boolean channelListCollapsed = false;
    Category model;
    private final EmoteRenderer renderer = new EmoteRenderer().setScalingFactor(2).setSize(18);

    public ServerCategoryElement(Category model) {
        this.model = model;
        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.SERVER_CATEGORY_ELEMENT);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        renderer.renderInto(model.getName(), categoryHeadLabel);
        categoryHeadArrow.setOnMouseClicked(this::onCategoryArrowClicked);
        categoryHeadLabel.setOnMouseClicked(this::onCategoryArrowClicked);
        categoryHeadLabel.setId(model.getId() + "-ServerCategoryElementLabel");

        categoryHeadPane.setOnMouseEntered(this::onCategoryMouseEntered);
        categoryHeadPane.setOnMouseExited(this::onCategoryMouseExited);

        addServerPlus.setOnMouseClicked(this::onAddServerPlusClicked);
        editCatGear.setOnMouseClicked(this::onEditCatGearClicked);

        this.setId(model.getId() + "-ServerCategoryElement");
    }

    private void onCategoryMouseEntered(MouseEvent mouseEvent) {
        addServerPlus.setVisible(true);
        editCatGear.setVisible(true);
    }

    private void onCategoryMouseExited(MouseEvent mouseEvent) {
        addServerPlus.setVisible(false);
        editCatGear.setVisible(false);
    }

    private void onAddServerPlusClicked(MouseEvent mouseEvent) {
        Parent addChannelModalView = ViewLoader.loadView(Views.ADD_CHANNEL_MODAL);
        AddChannelModal addChannelModal = new AddChannelModal(addChannelModalView, model);
        addChannelModal.show();
    }

    private void onEditCatGearClicked(MouseEvent mouseEvent) {
        Parent editChannelModelView = ViewLoader.loadView(Views.EDIT_CATEGORY_MODAL);
        EditCategoryModal editChannelModal = new EditCategoryModal(editChannelModelView, model);
        editChannelModal.show();
    }

    private void onCategoryArrowClicked(MouseEvent mouseEvent) {
        channelListCollapsed = !channelListCollapsed;
        if (channelListCollapsed) {
            categoryHeadArrow.setImage(ViewLoader.loadImage("right-arrow.png"));
            categoryChannelList.setVisible(false);
            categoryChannelList.setManaged(false);
        } else {
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

    public void updateText(String text) {
        Platform.runLater(() -> {
            categoryHeadLabel.getChildren().clear();
            renderer.renderInto(text, categoryHeadLabel);
        });
    }
}


