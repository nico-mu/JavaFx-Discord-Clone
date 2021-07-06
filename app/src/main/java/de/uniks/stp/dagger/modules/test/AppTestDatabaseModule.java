package de.uniks.stp.dagger.modules.test;

import dagger.Module;
import dagger.Provides;
import de.uniks.stp.dagger.scope.AppScope;
import de.uniks.stp.jpa.AppDatabaseService;

@Module
public class AppTestDatabaseModule {
    @Provides
    @AppScope
    static AppDatabaseService provideDatabaseService() {
        return new AppDatabaseService(false);
    }
}
