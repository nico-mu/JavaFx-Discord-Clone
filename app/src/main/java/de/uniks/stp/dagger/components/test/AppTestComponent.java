package de.uniks.stp.dagger.components.test;

import dagger.BindsInstance;
import dagger.Component;
import de.uniks.stp.AccordApp;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.dagger.components.BaseAppComponent;
import de.uniks.stp.dagger.modules.app.AppModule;
import de.uniks.stp.dagger.modules.test.AppTestDatabaseModule;
import de.uniks.stp.dagger.modules.test.AppTestNetworkModule;
import de.uniks.stp.dagger.scope.AppScope;
import de.uniks.stp.router.Router;
import javafx.stage.Stage;

import javax.inject.Named;

@AppScope
@Component(modules = {AppModule.class, AppTestNetworkModule.class, AppTestDatabaseModule.class})
public interface AppTestComponent extends BaseAppComponent {

    SessionTestComponent.Builder sessionTestComponentBuilder();
    ViewLoader getViewLoader();
    Router getRouter();
    Editor getEditor();

    @Component.Builder
    interface Builder {

        @BindsInstance
        AppTestComponent.Builder application(AccordApp application);

        @BindsInstance
        AppTestComponent.Builder primaryStage(@Named("primaryStage") Stage primaryStage);

        AppTestComponent build();
    }
}
