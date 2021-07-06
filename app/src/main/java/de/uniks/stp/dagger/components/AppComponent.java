package de.uniks.stp.dagger.components;


import dagger.BindsInstance;
import dagger.Component;
import de.uniks.stp.AccordApp;
import de.uniks.stp.dagger.modules.app.AppDatabaseModule;
import de.uniks.stp.dagger.modules.app.AppModule;
import de.uniks.stp.dagger.modules.app.AppNetworkModule;
import de.uniks.stp.dagger.scope.AppScope;
import javafx.stage.Stage;

import javax.inject.Named;

@AppScope
@Component(modules = {AppModule.class, AppNetworkModule.class, AppDatabaseModule.class})
public interface AppComponent extends BaseAppComponent {

    SessionComponent.Builder sessionComponentBuilder();

    @Component.Builder
    interface Builder {

        @BindsInstance
        AppComponent.Builder application(AccordApp application);

        @BindsInstance
        AppComponent.Builder primaryStage(@Named("primaryStage") Stage primaryStage);

        AppComponent build();
    }
}
