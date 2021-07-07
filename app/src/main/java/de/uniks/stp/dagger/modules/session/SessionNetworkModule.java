package de.uniks.stp.dagger.modules.session;

import dagger.Module;
import dagger.Provides;
import de.uniks.stp.Editor;
import de.uniks.stp.dagger.scope.SessionScope;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.model.User;
import de.uniks.stp.network.rest.HttpRequestInterceptor;
import de.uniks.stp.network.rest.SessionRestClient;
import de.uniks.stp.network.voice.VoiceChatClient;
import de.uniks.stp.network.voice.VoiceChatClientFactory;
import de.uniks.stp.network.voice.VoiceChatClientFactoryImpl;
import de.uniks.stp.network.websocket.WebSocketClientFactory;
import de.uniks.stp.network.websocket.WebSocketFactory;
import de.uniks.stp.network.websocket.WebSocketService;
import de.uniks.stp.notification.NotificationService;
import org.mockito.Mockito;

import javax.inject.Named;

@Module
public class SessionNetworkModule {
    @Provides
    @SessionScope
    SessionRestClient provideSessionRestClient(HttpRequestInterceptor interceptor) {
        return new SessionRestClient(interceptor);
    }


    @Provides
    @SessionScope
    HttpRequestInterceptor provideHttpRequestInterceptor(@Named("userKey") String userKey) {
        return new HttpRequestInterceptor(userKey);
    }

    @Provides
    @SessionScope
    WebSocketService provideWebsocketService(Editor editor,
                                                    NotificationService notificationService,
                                                    WebSocketClientFactory webSocketClientFactory,
                                                    SessionDatabaseService databaseService) {
        return new WebSocketService(editor, notificationService, webSocketClientFactory, databaseService);
    }

    @Provides
    @SessionScope
    WebSocketClientFactory provideWebSocketClientFactory(@Named("userKey") String userKey) {
        return new WebSocketFactory(userKey);
    }

    @Provides
    @SessionScope
    VoiceChatClientFactory provideVoiceChatClientFactory(@Named("currentUser") User currentUser) {
        return new VoiceChatClientFactoryImpl(currentUser);
    }
}
