package de.uniks.stp.dagger.components;


import dagger.BindsInstance;
import dagger.Component;
import de.uniks.stp.AccordApp;
import de.uniks.stp.controller.AppController;
import de.uniks.stp.dagger.modules.AppModule;
import de.uniks.stp.dagger.scope.AppScope;
import de.uniks.stp.jpa.AppDatabaseService;
import de.uniks.stp.network.rest.AppRestClient;
import javafx.stage.Stage;

@AppScope
@Component(modules = AppModule.class)
public interface AppComponent {

    AppController getAppController();
    AppRestClient getAppRestClient();
    AppDatabaseService getAppDataBaseService();

    @Component.Builder
    interface Builder {

        @BindsInstance
        AppComponent.Builder application(AccordApp application);

        @BindsInstance
        AppComponent.Builder primaryStage(Stage primaryStage);

        AppComponent build();
    }
}
