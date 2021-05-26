package de.uniks.stp.component;

import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.event.NavBarCreateServerClosedEvent;
import de.uniks.stp.event.NavBarElementChangeEvent;
import de.uniks.stp.event.NavBarHomeElementActiveEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class NavBarList extends ScrollPane {

    private final NavBarHomeElement homeElement;
    @FXML
    protected VBox container;

    private Editor editor;
    private NavBarElement currentActiveElement;
    private NavBarElement previousActiveElement;

    public NavBarList(Editor editor) {
        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.NAV_BAR_LIST);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        this.editor = editor;

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
        homeElement = new NavBarHomeElement();
        this.addElement(homeElement);
        this.setActiveElement(homeElement);

        //add create server button
        NavBarElement createServer = new NavBarAddServer(editor);
        this.addElement(createServer);

        this.addEventFilter(NavBarElementChangeEvent.NAV_BAR_ELEMENT_CHANGE, event -> {
            setActiveElement(event.getParam());
            event.consume();
        });

        this.addEventFilter(NavBarCreateServerClosedEvent.NAV_BAR_CREATE_SERVER_CLOSED, event -> {
            setActiveElement(previousActiveElement);
            event.consume();
        });

        this.addEventFilter(NavBarHomeElementActiveEvent.NAV_BAR_HOME_ELEMENT_ACTIVE, event -> {
            setActiveElement(homeElement);
            event.consume();
        });
    }

    public void setActiveElement(NavBarElement element) {
        if (!element.equals(currentActiveElement)) {
            if (currentActiveElement != null) {
                currentActiveElement.setActive(false);
                previousActiveElement = currentActiveElement;
            }
            element.setActive(true);
            currentActiveElement = element;
        }
    }

    /**
     * adds a NavBarElement to the server section
     * @param element element to add to NavBarList
     */
    public void addServerElement(NavBarElement element) {
        container.getChildren().add(container.getChildren().size() - 1, element);
    }

    /**
     * adds a NavBarElement to the user notification section
     * @param element element to add to NavBarList
     */
    public void addUserElement(NavBarElement element) {
        container.getChildren().add(1, element);
    }

    /**
     * adds a NavBarElement to the NavBar.
     * If you want to add a user element use addUserElement instead
     * @param element element to add to NavBarList
     */
    public void addElement(NavBarElement element) {
        container.getChildren().add(element);
    }

    /**
     * removes a NavBarElement from the NavBar.
     * @param element element to remove from NavBarList
     */
    public void removeElement(NavBarElement element) {
        this.container.getChildren().remove(element);
    }
}
