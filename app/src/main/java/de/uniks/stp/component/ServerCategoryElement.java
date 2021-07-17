package de.uniks.stp.component;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.modal.CreateChannelModal;
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

import java.io.IOException;

public class ServerCategoryElement extends VBox {

    @FXML
    private ImageView categoryHeadArrow;

    @FXML
    private TextWithEmoteSupport categoryHeadLabel;

    @FXML
    private HBox categoryHeadPane;

    @FXML
    private ImageView addServerPlus;

    @FXML
    private ImageView editCatGear;

    @FXML
    private VBox categoryChannelList;

    private boolean channelListCollapsed = false;
    private final Category model;
    private final ViewLoader viewLoader;
    private final CreateChannelModal.CreateChannelModalFactory createChannelModalFactory;
    private final EditCategoryModal.EditCategoryModalFactory editCategoryModalFactory;

    @AssistedInject
    public ServerCategoryElement(ViewLoader viewLoader,
                                 CreateChannelModal.CreateChannelModalFactory createChannelModalFactory,
                                 EditCategoryModal.EditCategoryModalFactory editCategoryModalFactory,
                                 @Assisted Category model) {
        this.model = model;
        this.viewLoader = viewLoader;
        this.createChannelModalFactory = createChannelModalFactory;
        this.editCategoryModalFactory = editCategoryModalFactory;
        FXMLLoader fxmlLoader = viewLoader.getFXMLComponentLoader(Components.SERVER_CATEGORY_ELEMENT);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        categoryHeadLabel.getRenderer().setScalingFactor(2).setSize(18);
        categoryHeadLabel.setText(model.getName());
        categoryHeadLabel.setOnMouseClicked(this::onCategoryArrowClicked);
        categoryHeadLabel.setId(model.getId() + "-ServerCategoryElementLabel");
        categoryHeadArrow.setOnMouseClicked(this::onCategoryArrowClicked);

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
        Parent addChannelModalView = viewLoader.loadView(Views.ADD_CHANNEL_MODAL);
        CreateChannelModal addChannelModal = createChannelModalFactory.create(addChannelModalView, model);
        addChannelModal.show();
    }

    private void onEditCatGearClicked(MouseEvent mouseEvent) {
        Parent editChannelModelView = viewLoader.loadView(Views.EDIT_CATEGORY_MODAL);
        EditCategoryModal editChannelModal = editCategoryModalFactory.create(editChannelModelView, model);
        editChannelModal.show();
    }

    private void onCategoryArrowClicked(MouseEvent mouseEvent) {
        channelListCollapsed = !channelListCollapsed;
        if (channelListCollapsed) {
            categoryHeadArrow.setImage(viewLoader.loadImage("up-arrow.png"));
            categoryChannelList.setVisible(false);
            categoryChannelList.setManaged(false);
        } else {
            categoryHeadArrow.setImage(viewLoader.loadImage("down-arrow.png"));
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
            categoryHeadLabel.setText(text);
        });
    }

    @AssistedFactory
    public interface ServerCategoryElementFactory {
        ServerCategoryElement create(Category model);
    }
}


