package de.uniks.stp.dagger.modules;

import dagger.Module;
import dagger.Provides;
import de.uniks.stp.AudioService;
import de.uniks.stp.Editor;
import de.uniks.stp.dagger.scope.SessionScope;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.model.User;
import de.uniks.stp.network.rest.HttpRequestInterceptor;
import de.uniks.stp.network.rest.ServerInformationHandler;
import de.uniks.stp.network.rest.SessionRestClient;
import de.uniks.stp.network.websocket.WebSocketClient;
import de.uniks.stp.network.websocket.WebSocketService;
import de.uniks.stp.notification.NotificationService;
import de.uniks.stp.router.Router;

import javax.inject.Named;

@Module
public class SessionModule {

    @Provides
    @SessionScope
    static NotificationService provideNotificationService(Router router,
                                                          SessionDatabaseService databaseService,
                                                          AudioService audioService) {
        return new NotificationService(router, databaseService, audioService);
    }

    @Provides
    @SessionScope
    static SessionRestClient provideSessionRestClient(HttpRequestInterceptor interceptor) {
        return new SessionRestClient(interceptor);
    }


    @Provides
    @SessionScope
     static HttpRequestInterceptor provideHttpRequestInterceptor(@Named("userKey") String userKey) {
        return new HttpRequestInterceptor(userKey);
    }

    @Provides
    @SessionScope
    static SessionDatabaseService provideSessionDatabaseService(@Named("currentUser") User currentUser) {
        return new SessionDatabaseService(currentUser);
    }

    @Provides
    @SessionScope
    static WebSocketService provideWebsocketService(Editor editor,
                                                    NotificationService notificationService,
                                                    WebSocketClient.WebSocketClientFactory webSocketClientFactory,
                                                    SessionDatabaseService databaseService) {
        return new WebSocketService(editor, notificationService, webSocketClientFactory, databaseService);
    }

    @Provides
    @SessionScope
    static ServerInformationHandler provideInformationHandler(Editor editor,
                                                              SessionRestClient restClient,
                                                              NotificationService notificationService) {
        return new ServerInformationHandler(editor, restClient, notificationService);
    }
}
