package de.uniks.stp.modal;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import de.uniks.stp.Constants;
import de.uniks.stp.ViewLoader;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.function.Consumer;

public class AddServerModal extends AbstractModal<VBox> {

    public static final String ADD_SERVER_CREATE_BUTTON = "#add-server-create-button";
    public static final String ADD_SERVER_CANCEL_BUTTON = "#add-server-cancel-button";
    public static final String ADD_SERVER_TEXT_FIELD_SERVERNAME = "#text-field-servername";
    private final JFXButton createButton;
    private final JFXButton cancelButton;
    private final JFXTextField servernameTextField;
    private final Consumer<String> createServerMethod;
    private static final Logger log = LoggerFactory.getLogger(AddServerModal.class);

    public AddServerModal(Parent root, Consumer<String> createServerMethod) {
        super(root);

        setTitle(ViewLoader.loadLabel(Constants.LBL_ADD_SERVER));

        this.createServerMethod = createServerMethod;
        createButton = (JFXButton) view.lookup(ADD_SERVER_CREATE_BUTTON);
        cancelButton = (JFXButton) view.lookup(ADD_SERVER_CANCEL_BUTTON);

        servernameTextField = (JFXTextField) view.lookup(ADD_SERVER_TEXT_FIELD_SERVERNAME);

        createButton.setOnAction(this::onApplyButtonClicked);
        createButton.setDefaultButton(true);  // use Enter in order to press button
        cancelButton.setOnAction(this::onCancelButtonClicked);
        cancelButton.setCancelButton(true);  // use Escape in order to press button
    }

    private void onCancelButtonClicked(ActionEvent actionEvent) {
        this.close();
    }

    private void onApplyButtonClicked(ActionEvent actionEvent) {
        String name = servernameTextField.getText();
        if (! name.isEmpty()){
            createServerMethod.accept(name);
        }
        this.close();
    }

    @Override
    public void close() {
        createButton.setOnAction(null);
        cancelButton.setOnAction(null);
        super.close();
    }
}
