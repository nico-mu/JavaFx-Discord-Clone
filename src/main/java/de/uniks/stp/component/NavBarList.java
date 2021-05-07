package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;


import java.io.IOException;

public class NavBarList extends ScrollPane {

    @FXML
    protected VBox container;

    private NavBarElement currentActiveElement;

    public NavBarList() {
        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.NAV_BAR_LIST);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        container.setPadding(new Insets(10.0d, 5.0d, 10.0d, 5.0d));
        container.setSpacing(10.0d);
        container.getStyleClass().add("nav-bar-vbox");
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.getStyleClass().add("nav-bar-vbox");

        //add home element
        NavBarElement homeElement = new NavBarHomeElement();
        this.addElement(homeElement);
        this.setActiveElement(homeElement);

        //add create server button
        NavBarElement createServer = new NavBarCreateServer();
        this.addElement(createServer);
    }

    public void setActiveElement(NavBarElement element) {
        if (!element.equals(currentActiveElement)) {
            if(currentActiveElement != null) {
                currentActiveElement.setActive(false);
            }
            element.setActive(true);
            currentActiveElement = element;
        }
    }

    public void addServerElement(NavBarElement element) {
        element.addOnClickHandler(this::setActiveElement);
        container.getChildren().add(container.getChildren().size() - 1, element);
    }

    public void addElement(NavBarElement element) {
        element.addOnClickHandler(this::setActiveElement);
        container.getChildren().add(element);
    }

    public void removeElement(NavBarElement element) {
        this.container.getChildren().remove(element);
    }
}
