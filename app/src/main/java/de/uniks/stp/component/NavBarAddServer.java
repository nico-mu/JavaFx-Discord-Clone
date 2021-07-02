package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.event.NavBarCreateServerClosedEvent;
import de.uniks.stp.modal.CreateServerModal;
import de.uniks.stp.view.Views;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;

public class NavBarAddServer extends NavBarElement {

    private final Editor editor;

    public NavBarAddServer(Editor editor) {
        this.editor = editor;
        installTooltip(ViewLoader.loadLabel(Constants.LBL_CREATE_SERVER));
        imageView.setImage(ViewLoader.loadImage("plus.png"));
        notificationLabel.setVisible(false);
        circle.setVisible(false);
    }

    @Override
    protected void onMouseClicked(MouseEvent mouseEvent) {
        super.onMouseClicked(mouseEvent);
        Parent addServerModalView = ViewLoader.loadView(Views.ADD_SERVER_MODAL);
        CreateServerModal addServerModal = new CreateServerModal(addServerModalView, editor);
        addServerModal.showAndWait();
        this.fireEvent(new NavBarCreateServerClosedEvent());
    }
}
