package de.uniks.stp.dagger.modules.test;

import dagger.Module;
import dagger.Provides;
import de.uniks.stp.Editor;
import de.uniks.stp.dagger.scope.SessionScope;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.network.rest.HttpRequestInterceptor;
import de.uniks.stp.network.rest.MediaRequestClient;
import de.uniks.stp.network.rest.SessionRestClient;
import de.uniks.stp.network.voice.VoiceChatClientFactory;
import de.uniks.stp.network.voice.test.VoiceChatClientTestFactory;
import de.uniks.stp.network.websocket.WSCallback;
import de.uniks.stp.network.websocket.WebSocketClient;
import de.uniks.stp.network.websocket.WebSocketClientFactory;
import de.uniks.stp.network.websocket.WebSocketService;
import de.uniks.stp.network.websocket.test.WebSocketClientTestFactory;
import de.uniks.stp.notification.NotificationService;
import org.mockito.Mockito;

import javax.inject.Named;

@Module
public class SessionTestNetworkModule {


    @Provides
    @SessionScope
    HttpRequestInterceptor provideHttpRequestInterceptor(@Named("userKey") String userKey) {
        return Mockito.mock(HttpRequestInterceptor.class);
    }


    @Provides
    @SessionScope
    SessionRestClient provideSessionRestClient(HttpRequestInterceptor interceptor) {
        return Mockito.mock(SessionRestClient.class);
    }


    @Provides
    @SessionScope
    WebSocketService provideWebsocketService(Editor editor,
                                             NotificationService notificationService,
                                             WebSocketClientFactory webSocketClientFactory,
                                             SessionDatabaseService databaseService) {
        return Mockito.spy(new WebSocketService(editor, notificationService, webSocketClientFactory, databaseService));
    }

    @Provides
    @SessionScope
    WebSocketClientFactory provideWebSocketClientFactory() {
        return Mockito.spy(new WebSocketClientTestFactory());
    }

    @Provides
    @SessionScope
    MediaRequestClient provideMediaRequestClient() {
        return Mockito.mock(MediaRequestClient.class);
    }

    @Provides
    @SessionScope
    VoiceChatClientFactory provideVoiceChatClientFactory() {
        return Mockito.spy(new VoiceChatClientTestFactory());
    }
}
