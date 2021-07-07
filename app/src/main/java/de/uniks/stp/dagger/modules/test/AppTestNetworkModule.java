package de.uniks.stp.dagger.modules.test;

import dagger.Module;
import dagger.Provides;
import de.uniks.stp.dagger.scope.AppScope;
import de.uniks.stp.network.rest.AppRestClient;
import org.mockito.Mockito;

@Module
public class AppTestNetworkModule {
    @AppScope
    @Provides
    AppRestClient provideRestClient() {
        return Mockito.mock(AppRestClient.class);
    }
}
