package de.uniks.stp.network.integration.api;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.exceptions.detailed.BadRequestException;
import com.wrapper.spotify.exceptions.detailed.UnauthorizedException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.requests.authorization.authorization_code.pkce.AuthorizationCodePKCERefreshRequest;
import com.wrapper.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.jpa.model.ApiIntegrationSettingDTO;
import de.uniks.stp.model.User;
import de.uniks.stp.network.integration.Credentials;
import de.uniks.stp.network.integration.IntegrationConstants;
import de.uniks.stp.network.integration.Integrations;
import de.uniks.stp.network.rest.SessionRestClient;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.json.Json;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.*;

public class SpotifyApiClient implements IntegrationApiClient {

    private final static Logger log = LoggerFactory.getLogger(SpotifyApiClient.class);
    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutorService;
    private ScheduledFuture<?> scheduledFuture;
    private final User currentUser;
    private final SessionDatabaseService databaseService;
    private final ViewLoader viewLoader;
    private final SessionRestClient restClient;
    private SpotifyApi spotifyApi;
    private final PropertyChangeListener currentUserDescriptionChangeListener;

    @Inject
    public SpotifyApiClient(@Named("currentUser") User currentUser,
                            ViewLoader viewLoader,
                            SessionRestClient restClient,
                            SessionDatabaseService sessionDatabaseService) {
        this.databaseService = sessionDatabaseService;
        this.executorService = Executors.newCachedThreadPool();
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        this.currentUser = currentUser;
        this.viewLoader = viewLoader;
        this.currentUserDescriptionChangeListener = this::onCurrentUserDescriptionChanged;
        this.restClient = restClient;
    }

    @Override
    public void start(Credentials credentials) {
        currentUser.listeners()
            .addPropertyChangeListener(User.PROPERTY_DESCRIPTION, currentUserDescriptionChangeListener);

        databaseService.addApiIntegrationSetting(Integrations.SPOTIFY.key, credentials.getRefreshToken());
        spotifyApi = new SpotifyApi.Builder()
            .setAccessToken(credentials.getAccessToken()).setRefreshToken(credentials.getRefreshToken()).build();

        //start api requests to playing api
        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(() -> {
            GetUsersCurrentlyPlayingTrackRequest getUsersCurrentlyPlayingTrackRequest =
                getUsersCurrentlyPlayingTrackRequest();

            try {
                CurrentlyPlaying playing = getUsersCurrentlyPlayingTrackRequest.execute();
                if(Objects.nonNull(playing) && playing.getIs_playing()) {
                    String jsonData = Json.createObjectBuilder()
                        .add("desc", viewLoader.loadLabel("LBL_LISTENING_TO_SPOTIFY")).build().toString();
                    currentUser.setDescription("#" + jsonData);
                }
                else {
                    currentUser.setDescription(" ");
                }

            } catch (IOException | SpotifyWebApiException | ParseException e) {
                if(e instanceof UnauthorizedException && e.getMessage().equals("The access token expired")) {
                    log.debug("The access token expired");
                    refresh();
                }
                else {
                    log.error("Unknown exception in spotify api client", e);
                    stop();
                }
            }
        }, 0, IntegrationConstants.SPOTIFY_POLL_INTERVAL, TimeUnit.MILLISECONDS);
    }

    @Override
    public void refresh() {
        this.stop();
        //check if refresh token is in db
        ApiIntegrationSettingDTO integrationSetting = databaseService.getApiIntegrationSetting(Integrations.SPOTIFY.key);

        if(Objects.nonNull(integrationSetting) && !integrationSetting.getRefreshToken().isEmpty()) {
            String refreshToken = integrationSetting.getRefreshToken();
            spotifyApi = new SpotifyApi.Builder()
                .setRefreshToken(refreshToken)
                .setClientId(IntegrationConstants.SPOTIFY_CLIENT_ID)
                .build();

            executorService.execute(this::getRefreshedToken);
        }
    }

    public GetUsersCurrentlyPlayingTrackRequest getUsersCurrentlyPlayingTrackRequest() {
        return spotifyApi.getUsersCurrentlyPlayingTrack().build();
    }

    public AuthorizationCodePKCERefreshRequest getAuthorizationCodePKCERefreshRequest() {
        return spotifyApi.authorizationCodePKCERefresh().build();
    }

    private void getRefreshedToken() {
        AuthorizationCodePKCERefreshRequest authorizationCodePKCERefreshRequest = getAuthorizationCodePKCERefreshRequest();
        try {
            final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodePKCERefreshRequest.execute();
            log.debug("Access token expires in {}", authorizationCodeCredentials.getExpiresIn().toString());
            Credentials wrappedCredentials = new Credentials();
            wrappedCredentials.setAccessToken(authorizationCodeCredentials.getAccessToken());
            wrappedCredentials.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            this.start(wrappedCredentials);

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            if(e instanceof BadRequestException && e.getMessage().equals("Refresh token revoked")) {
                log.debug("Refresh token revoked");
                databaseService.deleteApiIntegrationSetting(Integrations.SPOTIFY.key);
            }
        }
    }

    private void onCurrentUserDescriptionChanged(PropertyChangeEvent propertyChangeEvent) {
        String newValue = (String)propertyChangeEvent.getNewValue();
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
        //stop polling timer
        if(Objects.nonNull(scheduledFuture)) {
            scheduledFuture.cancel(true);
        }
        currentUser.listeners()
            .removePropertyChangeListener(User.PROPERTY_DESCRIPTION, currentUserDescriptionChangeListener);
    }

    public void shutdown() {
        this.stop();
        scheduledExecutorService.shutdown();
        executorService.shutdown();
    }
}
