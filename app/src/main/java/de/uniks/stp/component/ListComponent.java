package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ListComponent<Model, Element extends Node> extends ScrollPane {

    @FXML
    private VBox container;

    private final ConcurrentHashMap<Model, Element> elementHashMap = new ConcurrentHashMap<>();
    private final ObservableList<Node> elements;

    public ListComponent() {
        final FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.LIST_COMPONENT);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            elements = container.getChildren();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public boolean contains(Model model) {
        return elementHashMap.containsKey(model);
    }

    public Element getElement(Model model) {
        if(contains(model)) {
            return elementHashMap.get(model);
        }
        return null;
    }

    public void addElement(Model model, Element element) {
        if(!contains(model)) {
           elementHashMap.put(model, element);
           elements.add(element);
        }
    }

    public boolean removeElement(Model model) {
        if(contains(model)) {
            Element removedElement = elementHashMap.remove(model);
            elements.remove(removedElement);
            return true;
        }
        return false;
    }
}
