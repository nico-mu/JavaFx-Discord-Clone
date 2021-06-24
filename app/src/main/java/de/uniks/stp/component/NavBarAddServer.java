package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.StageManager;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.event.NavBarCreateServerClosedEvent;
import de.uniks.stp.modal.AddServerModal;
import de.uniks.stp.view.Views;
import javafx.scene.Parent;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NavBarAddServer extends NavBarElement {
    private static final Logger log = LoggerFactory.getLogger(NavBarAddServer.class);

    private final Editor editor;

    public NavBarAddServer(Editor editor) {
        this.editor = editor;
        Tooltip.install(navBarElement, new Tooltip(ViewLoader.loadLabel(Constants.LBL_CREATE_SERVER)));
        imageView.setImage(ViewLoader.loadImage("plus.png"));
        notificationLabel.setVisible(false);
        circle.setVisible(false);
    }

    @Override
    protected void onMouseClicked(MouseEvent mouseEvent) {
        super.onMouseClicked(mouseEvent);
        Parent addServerModalView = ViewLoader.loadView(Views.ADD_SERVER_MODAL);
        Stage stage = StageManager.getStage();
        double x = stage.getX();
        double y = stage.getY();
        AddServerModal addServerModal = new AddServerModal(addServerModalView, editor, StageManager.getStage());
        addServerModal.showAndWait();
        this.fireEvent(new NavBarCreateServerClosedEvent());
    }
}
