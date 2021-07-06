package de.uniks.stp;

import de.uniks.stp.dagger.components.BaseAppComponent;
import de.uniks.stp.dagger.components.DaggerAppComponent;
import de.uniks.stp.dagger.components.SessionComponent;
import de.uniks.stp.dagger.components.test.DaggerAppTestComponent;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class AccordApp extends Application {

    private static final Logger log = LoggerFactory.getLogger(AccordApp.class);

    private BaseAppComponent appComponent;
    private SessionComponent sessionComponent;
    private boolean testMode = false;

    @Override
    public void start(Stage primaryStage) {
        //create appComponent
        if(testMode) {
            appComponent = DaggerAppTestComponent.builder()
                .application(this)
                .primaryStage(primaryStage)
                .build();
        } else {
            appComponent = DaggerAppComponent.builder()
                .application(this)
                .primaryStage(primaryStage)
                .build();
        }
        //start app using AppController
        appComponent.getAppController().init();
    }

    @Override
    public void stop() {
        try {
            super.stop();
            appComponent.getAppController().stop();

            if(Objects.nonNull(sessionComponent)) {
                sessionComponent.getSessionRestClient().sendLogoutRequest(response -> {});
                sessionComponent.getSessionRestClient().stop();
                sessionComponent.getWebsocketService().stop();
                sessionComponent.getSessionDatabaseService().stop();
                sessionComponent = null;
            }
            appComponent.getAppRestClient().stop();
            appComponent = null;
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

    public BaseAppComponent getAppComponent() {
        return appComponent;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }
}
