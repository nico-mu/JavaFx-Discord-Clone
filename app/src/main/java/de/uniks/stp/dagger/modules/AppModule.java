package de.uniks.stp.dagger.modules;


import dagger.Module;
import dagger.Provides;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.dagger.components.SessionComponent;
import de.uniks.stp.jpa.AppDatabaseService;
import de.uniks.stp.network.rest.AppRestClient;

@Module(subcomponents = SessionComponent.class)
public class AppModule {
    @Provides
    static Editor provideEditor() {
        return new Editor();
    }

    @Provides
    static AppRestClient provideRestClient() {
        return new AppRestClient();
    }

    @Provides
    static ViewLoader provideViewLoader() {
        return new ViewLoader();
    }

    @Provides
    static AppDatabaseService provideDatabaseService() {
        return new AppDatabaseService(true);
    }

}
