package de.uniks.stp.dagger.components;

import dagger.BindsInstance;
import dagger.Subcomponent;
import de.uniks.stp.controller.MainScreenController;
import de.uniks.stp.dagger.modules.SessionModule;
import de.uniks.stp.dagger.scope.SessionScope;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.model.User;
import de.uniks.stp.network.rest.SessionRestClient;
import de.uniks.stp.network.websocket.WebSocketService;

import javax.inject.Named;

@SessionScope
@Subcomponent(modules = SessionModule.class)
public interface SessionComponent {

    MainScreenController.MainScreenControllerFactory getMainScreenControllerFactory();
    WebSocketService getWebsocketService();
    SessionDatabaseService getSessionDatabaseService();
    SessionRestClient getSessionRestClient();

    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        Builder userKey(@Named("userKey") String userKey);

        @BindsInstance
        Builder currentUser(@Named("currentUser") User currentUser);

        SessionComponent build();
    }
}
