package de.uniks.stp.dagger.components.test;

import dagger.BindsInstance;
import dagger.Subcomponent;
import de.uniks.stp.dagger.components.SessionComponent;
import de.uniks.stp.dagger.modules.session.SessionModule;
import de.uniks.stp.dagger.modules.test.SessionTestNetworkModule;
import de.uniks.stp.dagger.scope.SessionScope;
import de.uniks.stp.model.User;
import de.uniks.stp.network.websocket.WebSocketClientFactory;
import de.uniks.stp.notification.NotificationService;

import javax.annotation.Nullable;
import javax.inject.Named;

@SessionScope
@Subcomponent(modules = {SessionModule.class, SessionTestNetworkModule.class})
public interface SessionTestComponent extends SessionComponent {

    WebSocketClientFactory getWebSocketClientFactory();
    NotificationService getNotificationService();

    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        SessionTestComponent.Builder userKey(@Named("userKey") String userKey);

        @BindsInstance
        SessionTestComponent.Builder currentUser(@Named("currentUser") User currentUser);

        SessionTestComponent build();
    }
}
