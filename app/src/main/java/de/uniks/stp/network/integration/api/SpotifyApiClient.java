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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpotifyApiClient implements IntegrationApiClient {

    private final static Logger log = LoggerFactory.getLogger(SpotifyApiClient.class);
    private final ExecutorService executorService;
    private final User currentUser;
    private final SessionDatabaseService databaseService;
    private final ViewLoader viewLoader;
    private final SessionRestClient restClient;
    private SpotifyApi spotifyApi;
    private Timer timer;
    private final PropertyChangeListener currentUserPlayingChangeListener;

    @Inject
    SpotifyApiClient(@Named("currentUser") User currentUser,
                     ViewLoader viewLoader,
                     SessionRestClient restClient,
                     SessionDatabaseService sessionDatabaseService) {
        this.databaseService = sessionDatabaseService;
        this.executorService = Executors.newCachedThreadPool();
        this.currentUser = currentUser;
        this.viewLoader = viewLoader;
        timer = new Timer();
        this.currentUserPlayingChangeListener = this::onCurrentUserPlayingChanged;
        this.restClient = restClient;
    }

    @Override
    public void start(Credentials credentials) {
        currentUser.listeners()
            .addPropertyChangeListener(User.PROPERTY_SPOTIFY_PLAYING, currentUserPlayingChangeListener);

        databaseService.addApiIntegrationSetting(Integrations.SPOTIFY.key, credentials.getRefreshToken());
        spotifyApi = new SpotifyApi.Builder()
            .setAccessToken(credentials.getAccessToken()).setRefreshToken(credentials.getRefreshToken()).build();

        //start api requests to playing api
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                GetUsersCurrentlyPlayingTrackRequest getUsersCurrentlyPlayingTrackRequest =
                    spotifyApi.getUsersCurrentlyPlayingTrack().build();

                try {
                    CurrentlyPlaying playing = getUsersCurrentlyPlayingTrackRequest.execute();
                    currentUser.setSpotifyPlaying(Objects.nonNull(playing) && playing.getIs_playing());

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
            }
        }, 0, IntegrationConstants.SPOTIFY_POLL_INTERVAL);
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

    private void getRefreshedToken() {
        AuthorizationCodePKCERefreshRequest authorizationCodePKCERefreshRequest =
            spotifyApi.authorizationCodePKCERefresh().build();
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

    private void onCurrentUserPlayingChanged(PropertyChangeEvent propertyChangeEvent) {
        boolean newValue = (boolean)propertyChangeEvent.getNewValue();

        if(newValue) {
            currentUser.setDescription("#" + viewLoader.loadLabel("LBL_LISTENING_TO_SPOTIFY"));
        }
        else {
            currentUser.setDescription(" ");
        }
        //make rest call with new description
        // note that is a synchronous call here, because we are already in another thread
        currentUserDescriptionCallback(restClient.updateDescription(currentUser.getId(), currentUser.getDescription()));
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
        timer.cancel();
        timer.purge();
        currentUser.listeners()
            .removePropertyChangeListener(User.PROPERTY_SPOTIFY_PLAYING, currentUserPlayingChangeListener);
    }
}
