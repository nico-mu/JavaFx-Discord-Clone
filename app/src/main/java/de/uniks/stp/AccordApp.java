package de.uniks.stp;

import de.uniks.stp.dagger.components.AppComponent;
import de.uniks.stp.dagger.components.DaggerAppComponent;
import de.uniks.stp.dagger.components.SessionComponent;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class AccordApp extends Application {

    private static final Logger log = LoggerFactory.getLogger(AccordApp.class);

    private AppComponent appComponent;
    private SessionComponent sessionComponent;

    @Override
    public void start(Stage primaryStage) throws Exception {
        //create appComponent
        appComponent = DaggerAppComponent.builder()
            .application(this)
            .primaryStage(primaryStage)
            .build();
        //start app using AppController
        appComponent.getAppController().init();
    }

    @Override
    public void stop() {
        try {
            super.stop();

            if(Objects.nonNull(sessionComponent)) {
                sessionComponent.getSessionRestClient().sendLogoutRequest(response -> {});
                sessionComponent.getSessionRestClient().stop();
                sessionComponent.getWebsocketService().stop();
                sessionComponent.getSessionDatabaseService().stop();
                sessionComponent = null;
            }
            appComponent.getAppRestClient().stop();
        } catch (Exception e) {
            log.error("Error while trying to shutdown", e);
        }
    }

    public SessionComponent getSessionComponent() {
        return sessionComponent;
    }

    public void setSessionComponent(SessionComponent sessionComponent) {
        this.sessionComponent = sessionComponent;
    }
}
