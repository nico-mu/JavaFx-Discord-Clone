package de.uniks.stp.component;

import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.event.NavBarCreateServerClosedEvent;
import de.uniks.stp.modal.CreateServerModal;
import de.uniks.stp.view.Views;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;

import javax.inject.Inject;

public class NavBarCreateServer extends NavBarElement {

    public static String CREATE_SERVER_ID = "#create-server";
    private final CreateServerModal.CreateServerModalFactory createServerModalFactory;

    @Inject
    public NavBarCreateServer(ViewLoader viewLoader,
                              CreateServerModal.CreateServerModalFactory createServerModalFactory) {
        super(viewLoader);
        this.createServerModalFactory = createServerModalFactory;
        installTooltip(viewLoader.loadLabel(Constants.LBL_CREATE_SERVER));
        imageView.setImage(viewLoader.loadImage("plus.png"));
        this.setId("create-server");
        notificationLabel.setVisible(false);
        circle.setVisible(false);
    }

    @Override
    protected void onMouseClicked(MouseEvent mouseEvent) {
        super.onMouseClicked(mouseEvent);
        Parent addServerModalView = viewLoader.loadView(Views.ADD_SERVER_MODAL);
        CreateServerModal addServerModal = createServerModalFactory.create(addServerModalView);
        addServerModal.showAndWait();
        this.fireEvent(new NavBarCreateServerClosedEvent());
    }
}
