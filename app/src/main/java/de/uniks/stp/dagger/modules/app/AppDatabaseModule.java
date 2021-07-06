package de.uniks.stp.dagger.modules.app;

import dagger.Module;
import dagger.Provides;
import de.uniks.stp.dagger.scope.AppScope;
import de.uniks.stp.jpa.AppDatabaseService;

@Module
public class AppDatabaseModule {
    @Provides
    @AppScope
    static AppDatabaseService provideDatabaseService() {
        return new AppDatabaseService(true);
    }
}
