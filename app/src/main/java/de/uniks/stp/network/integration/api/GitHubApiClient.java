package de.uniks.stp.network.integration.api;

import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.jpa.model.ApiIntegrationSettingDTO;
import de.uniks.stp.model.User;
import de.uniks.stp.network.integration.Credentials;
import de.uniks.stp.network.integration.IntegrationConstants;
import de.uniks.stp.network.integration.Integrations;
import de.uniks.stp.network.rest.SessionRestClient;
import de.uniks.stp.util.IntegrationUtil;
import kong.unirest.*;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.json.Json;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import java.util.concurrent.*;

public class GitHubApiClient implements IntegrationApiClient {

    private final static Logger log = LoggerFactory.getLogger(GitHubApiClient.class);
    private ScheduledExecutorService scheduledExecutorService;
    private ScheduledFuture<?> scheduledFuture;
    private final User currentUser;
    private final SessionDatabaseService databaseService;
    private final SessionRestClient restClient;
    private UnirestInstance instance;
    private final PropertyChangeListener currentUserDescriptionChangeListener;

    @Inject
    public GitHubApiClient(@Named("currentUser") User currentUser,
                            SessionRestClient restClient,
                            SessionDatabaseService sessionDatabaseService) {
        this.databaseService = sessionDatabaseService;
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        this.currentUser = currentUser;
        this.restClient = restClient;
        this.instance = new UnirestInstance(new Config());
        this.currentUserDescriptionChangeListener = this::onCurrentUserDescriptionChanged;
    }

    @Override
    public void start(Credentials credentials) {
        currentUser.listeners()
            .addPropertyChangeListener(User.PROPERTY_DESCRIPTION, currentUserDescriptionChangeListener);
        databaseService.addApiIntegrationSetting(Integrations.GITHUB.key, credentials.getAccessToken());

        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(() -> {
            HttpResponse<JsonNode> response = instance.get(IntegrationConstants.GITHUB_API_URL + IntegrationConstants.GITHUB_API_USER_URL)
                .header("Accept", IntegrationConstants.GITHUB_API_ACCEPT_HEADER)
                .header("Authorization", "token " + credentials.getAccessToken())
                .asJson();

            JSONObject jsonBody = response.getBody().getObject();
            if(response.isSuccess()) {
                String name = jsonBody.getString("name");
                if(Objects.isNull(name) || name.isEmpty() || name.isBlank()) {
                    name = jsonBody.getString("login");
                    //TODO: insert graphql query here
                }
                String jsonData = Json.createObjectBuilder()
                    .add("desc", IntegrationUtil.getTrimmedDescription(name)).build().toString();
                currentUser.setDescription("%" + jsonData);
            }
            else if(response.getStatus() == 401 && jsonBody.get("message").equals("Bad credentials")) {
                databaseService.deleteApiIntegrationSetting(Integrations.GITHUB.key);
                stop();
            }

        }, 0, IntegrationConstants.GITHUB_POLL_INTERVAL, TimeUnit.MILLISECONDS);
    }

    @Override
    public void refresh() {
        this.stop();
        //check if refresh token is in db
        ApiIntegrationSettingDTO integrationSetting = databaseService.getApiIntegrationSetting(Integrations.GITHUB.key);

        if(Objects.nonNull(integrationSetting) && !integrationSetting.getRefreshToken().isEmpty()) {
            Credentials credentials = new Credentials();
            credentials.setAccessToken(integrationSetting.getRefreshToken());
            this.start(credentials);
        }
    }

    private void onCurrentUserDescriptionChanged(PropertyChangeEvent propertyChangeEvent) {
        String newValue = (String) propertyChangeEvent.getNewValue();
        //make rest call with new description
        // note that is a synchronous call here, because we are already in another thread
        currentUserDescriptionCallback(restClient.updateDescriptionSync(currentUser.getId(), newValue));
    }

    private void currentUserDescriptionCallback(HttpResponse<JsonNode> response) {
        if(response.isSuccess()) {
            log.debug("description for user {} changed successfully", currentUser.getName());
        }
        else {
            log.error("Could not change description of user {}", currentUser.getName());
        }
    }

    @Override
    public void stop() {
        if(Objects.nonNull(scheduledFuture)) {
            scheduledFuture.cancel(true);
        }
        currentUser.listeners()
            .removePropertyChangeListener(User.PROPERTY_DESCRIPTION, currentUserDescriptionChangeListener);
    }

    @Override
    public void shutdown() {
        this.stop();
        scheduledExecutorService.shutdown();
        instance.shutDown();
    }
}
