package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ListComponent<Model, Element extends Node> extends ScrollPane {

    @FXML
    private VBox container;

    private final ConcurrentHashMap<Model, Element> elementHashMap = new ConcurrentHashMap<>();
    private final ObservableList<Node> elements;
    private final InvalidationListener heightChangedListener = this::onHeightChanged;

    private void onHeightChanged(Observable observable) {
        this.setVvalue(1.0d);
    }

    public ListComponent(ViewLoader viewLoader) {
        final FXMLLoader fxmlLoader = viewLoader.getFXMLComponentLoader(Components.LIST_COMPONENT);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            elements = container.getChildren();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        setIsScrollAware(false);
        setFitToWidth(true);
    }

    public ListComponent(ViewLoader viewLoader, String id) {
        this(viewLoader);
        this.setId(id);
    }

    public void setIsScrollAware(boolean mode) {
        if(mode) {
            container.heightProperty().addListener(heightChangedListener);
        }
        else {
            container.heightProperty().removeListener(heightChangedListener);
        }

    }

    public void clear() {
        elementHashMap.clear();
        elements.clear();
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

    public Collection<Element> getElements() {
        return elementHashMap.values();
    }

    public List<Model> getModels() {
        return new ArrayList<>(elementHashMap.keySet());
    }

    public void addElement(Model model, Element element) {
        if(!contains(model)) {
           elementHashMap.put(model, element);
           elements.add(element);
        }
    }

    public void insertElement(int pos, Model model, Element element) {
        if(!contains(model)) {
            elementHashMap.put(model, element);
            elements.add(pos, element);
        }
    }

    public Element removeElement(Model model) {
        if(contains(model)) {
            Element removedElement = elementHashMap.remove(model);
            elements.remove(removedElement);
            return removedElement;
        }
        return null;
    }
}
