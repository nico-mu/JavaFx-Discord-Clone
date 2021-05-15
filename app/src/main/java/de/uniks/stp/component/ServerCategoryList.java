package de.uniks.stp.component;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.event.AbstractEvent;
import de.uniks.stp.event.ChannelChangeEvent;
import de.uniks.stp.event.CustomEventHandler;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ServerCategoryList extends ScrollPane {
    @FXML
    protected VBox container;
    private ServerChannelElement currentActiveElement;

    public ServerCategoryList() {
        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.SERVER_CATEGORY_LIST);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        container.setPadding(new Insets(10.0d, 5.0d, 0.0d, 5.0d));
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        EventHandler<AbstractEvent> filter = new CustomEventHandler() {
            @Override
            public void onChannelChanged(ServerChannelElement element) {
                 setActiveElement(element);
            }
        };

        this.addEventFilter(ChannelChangeEvent.CHANNEL_CHANGE_EVENT_EVENT_TYPE, filter);
    }

    public void setActiveElement(ServerChannelElement element) {
        if (!element.equals(currentActiveElement)) {
            if(currentActiveElement != null) {
                currentActiveElement.setActive(false);
            }
            element.setActive(true);
            currentActiveElement = element;
        }
    }

    public void addElement(ServerCategoryElement element) {
        container.getChildren().add(element);
    }

    public void removeElement(ServerCategoryElement element) {
        this.container.getChildren().remove(element);
    }
}
