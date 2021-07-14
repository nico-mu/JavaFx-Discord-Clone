package de.uniks.stp.controller;

import java.util.Objects;

public abstract class BaseController implements ControllerInterface {

    protected Runnable onStop;

    public void setOnStop(Runnable onStop) {
        this.onStop = onStop;
    }

    @Override
    public void stop() {
        if(Objects.nonNull(onStop)) {
            onStop.run();
            onStop = null;
        }
    }
}
