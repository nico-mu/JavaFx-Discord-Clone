package de.uniks.stp.dagger.components;

import de.uniks.stp.controller.AppController;
import de.uniks.stp.jpa.AppDatabaseService;
import de.uniks.stp.network.rest.AppRestClient;

public interface BaseAppComponent {
    AppController getAppController();
    AppRestClient getAppRestClient();
    AppDatabaseService getAppDataBaseService();
}
