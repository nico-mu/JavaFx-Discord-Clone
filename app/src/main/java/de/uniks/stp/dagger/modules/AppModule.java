package de.uniks.stp.dagger.modules;


import dagger.Module;
import dagger.Provides;
import de.uniks.stp.AccordApp;
import de.uniks.stp.AudioService;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.controller.AppController;
import de.uniks.stp.controller.LoginScreenController;
import de.uniks.stp.dagger.components.SessionComponent;
import de.uniks.stp.dagger.scope.AppScope;
import de.uniks.stp.jpa.AppDatabaseService;
import de.uniks.stp.network.rest.AppRestClient;
import de.uniks.stp.router.Router;
import javafx.stage.Stage;

@Module(subcomponents = SessionComponent.class)
public class AppModule {

    @Provides
    @AppScope
    static Editor provideEditor() {
        return new Editor();
    }

    @Provides
    @AppScope
    static AppRestClient provideRestClient() {
        return new AppRestClient();
    }

    @Provides
    @AppScope
    static ViewLoader provideViewLoader() {
        return new ViewLoader();
    }

    @Provides
    @AppScope
    static AppDatabaseService provideDatabaseService() {
        return new AppDatabaseService(true);
    }

    @Provides
    @AppScope
    static AudioService provideAudioService(AppDatabaseService databaseService) {
        return new AudioService(databaseService);
    }

    @Provides
    @AppScope
    static Router provideRouter(AppController appController) {
        return new Router(appController);
    }

    @Provides
    @AppScope
    static AppController provideAppController(AccordApp app,
                                       ViewLoader viewLoader,
                                       Stage primaryStage,
                                       LoginScreenController.LoginScreenControllerFactory loginScreenControllerFactory,
                                       AppDatabaseService databaseService) {
        return new AppController(app, viewLoader, primaryStage, loginScreenControllerFactory, databaseService);
    }

}
