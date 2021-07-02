package de.uniks.stp.dagger.modules;

import dagger.Binds;
import dagger.Module;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.network.rest.HttpRequestInterceptor;
import de.uniks.stp.network.rest.ServerInformationHandler;
import de.uniks.stp.network.rest.SessionRestClient;
import de.uniks.stp.network.websocket.WebSocketService;
import de.uniks.stp.notification.NotificationService;

@Module
public interface SessionModule {

    @Binds
    NotificationService bindNotificationService(NotificationService notificationService);

    @Binds
    SessionRestClient bindSessionRestClient(SessionRestClient sessionRestClient);


    @Binds
    HttpRequestInterceptor bindHttpRequestInterceptor(HttpRequestInterceptor interceptor);

    @Binds
    SessionDatabaseService bindSessionDatabaseService(SessionDatabaseService sessionDatabaseService);

    @Binds
    WebSocketService bindWebsocketService(WebSocketService webSocketService);

    @Binds
    ServerInformationHandler bindInformationHandler(ServerInformationHandler informationHandler);
}
