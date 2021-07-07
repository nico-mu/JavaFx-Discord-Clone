package de.uniks.stp.dagger.modules.app;

import dagger.Module;
import dagger.Provides;
import de.uniks.stp.dagger.scope.AppScope;
import de.uniks.stp.network.rest.AppRestClient;

@Module
public class AppNetworkModule {
    @Provides
    @AppScope
    AppRestClient provideRestClient() {
        return new AppRestClient();
    }
}
