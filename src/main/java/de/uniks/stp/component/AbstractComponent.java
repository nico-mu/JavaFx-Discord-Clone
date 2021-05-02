package de.uniks.stp.component;

import javafx.scene.Node;

public abstract class AbstractComponent<R extends Node> {
    protected R rootElement;

    public R getRootElement() {
        return rootElement;
    }

    public void setRootElement(final R rootElement) {
        this.rootElement = rootElement;
    }
}
