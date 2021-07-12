package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.event.NavBarCreateServerClosedEvent;
import de.uniks.stp.event.NavBarElementChangeEvent;
import de.uniks.stp.modal.CreateServerModal;
import de.uniks.stp.view.Views;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;

import javax.inject.Inject;

public class NavBarCreateServer extends NavBarElement {

    private final CreateServerModal.CreateServerModalFactory createServerModalFactory;

    @Inject
    public NavBarCreateServer(ViewLoader viewLoader,
                              CreateServerModal.CreateServerModalFactory createServerModalFactory) {
        super(viewLoader);
        this.createServerModalFactory = createServerModalFactory;
        installTooltip(viewLoader.loadLabel(Constants.LBL_CREATE_SERVER));
        imageView.setImage(viewLoader.loadImage("plus.png"));
        notificationLabel.setVisible(false);
        circle.setVisible(false);
    }

    @Override
    protected void onMouseClicked(MouseEvent mouseEvent) {
        this.fireEvent(new NavBarElementChangeEvent(this));
        Parent addServerModalView = viewLoader.loadView(Views.ADD_SERVER_MODAL);
        CreateServerModal addServerModal = createServerModalFactory.create(addServerModalView);
        addServerModal.showAndWait();
        this.fireEvent(new NavBarCreateServerClosedEvent());
    }
}
