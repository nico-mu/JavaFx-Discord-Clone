package de.uniks.stp.component;

import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.event.NavBarCreateServerClosedEvent;
import de.uniks.stp.event.NavBarElementChangeEvent;
import de.uniks.stp.event.NavBarHomeElementActiveEvent;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class NavBarList extends ScrollPane {

    @FXML
    protected VBox container;

    private final NavBarHomeElement homeElement;
    private NavBarElement currentActiveElement;
    private NavBarElement previousActiveElement;
    private final ObservableList<Node> elements;
    private final ConcurrentHashMap<Server, NavBarServerElement> serverElementHashmap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<User, NavBarUserElement> userElementHashmap = new ConcurrentHashMap<>();

    public NavBarList(Editor editor) {
        FXMLLoader fxmlLoader = ViewLoader.getFXMLComponentLoader(Components.NAV_BAR_LIST);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            elements = container.getChildren();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        //add home element
        homeElement = new NavBarHomeElement();
        elements.add(homeElement);
        this.setActiveElement(homeElement);

        //add create server button
        NavBarElement createServer = new NavBarAddServer(editor);
        elements.add(createServer);

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

    public void setHomeElementActive() {
        setActiveElement(homeElement);
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


    public void addServerElement(Server server, NavBarServerElement serverElement) {
        if(!containsServer(server)) {
            serverElementHashmap.put(server, serverElement);
            elements.add(elements.size() - 1, serverElement);
        }
    }

    public void addUserElement(User user, NavBarUserElement userElement) {
        if(!containsUser(user)) {
            userElementHashmap.put(user, userElement);
            elements.add(1, userElement);
        }
    }

    public NavBarUserElement getUserElement(User user) {
        if(containsUser(user)) {
            return userElementHashmap.get(user);
        }
        return null;
    }

    public NavBarServerElement getServerElement(Server server) {
        if(containsServer(server)) {
            return serverElementHashmap.get(server);
        }
        return null;
    }

    public boolean containsUser(User user) {
        return userElementHashmap.containsKey(user);
    }

    public boolean containsServer(Server server) {
        return serverElementHashmap.containsKey(server);
    }

    public NavBarServerElement removeServerElement(Server server) {
        if(containsServer(server)) {
            NavBarServerElement serverElement = serverElementHashmap.remove(server);
            elements.remove(serverElement);
            return serverElement;
        }
        return null;
    }

    public NavBarUserElement removeUserElement(User user) {
        if(containsUser(user)) {
            NavBarUserElement userElement = userElementHashmap.remove(user);
            elements.remove(userElement);
            return userElement;
        }
        return null;
    }

    public void clear() {
        elements.clear();
        serverElementHashmap.clear();
        userElementHashmap.clear();
    }

}

