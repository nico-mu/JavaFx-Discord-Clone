package de.uniks.stp.dagger.modules.session;

import dagger.Module;
import dagger.Provides;
import de.uniks.stp.AudioService;
import de.uniks.stp.Editor;
import de.uniks.stp.dagger.scope.SessionScope;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.model.User;
import de.uniks.stp.network.rest.ServerInformationHandler;
import de.uniks.stp.network.rest.SessionRestClient;
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
    static SessionDatabaseService provideSessionDatabaseService(@Named("currentUser") User currentUser) {
        return new SessionDatabaseService(currentUser);
    }

    @Provides
    @SessionScope
    static ServerInformationHandler provideInformationHandler(Editor editor,
                                                              SessionRestClient restClient,
                                                              NotificationService notificationService) {
        return new ServerInformationHandler(editor, restClient, notificationService);
    }
}
