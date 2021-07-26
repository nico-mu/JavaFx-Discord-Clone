package de.uniks.stp.network.integration.api;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.exceptions.detailed.BadRequestException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.requests.authorization.authorization_code.pkce.AuthorizationCodePKCERefreshRequest;
import com.wrapper.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;
import de.uniks.stp.Editor;
import de.uniks.stp.jpa.SessionDatabaseService;
import de.uniks.stp.jpa.model.ApiIntegrationSettingDTO;
import de.uniks.stp.network.integration.Credentials;
import de.uniks.stp.network.integration.IntegrationConstants;
import de.uniks.stp.network.integration.Integrations;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpotifyApiClient implements IntegrationApiClient {

    private final static Logger log = LoggerFactory.getLogger(SpotifyApiClient.class);
    private final ExecutorService executorService;
    private Editor editor;
    private final SessionDatabaseService databaseService;
    private SpotifyApi spotifyApi;
    private Timer timer;

    @Inject
    SpotifyApiClient(Editor editor,
                     SessionDatabaseService sessionDatabaseService) {
        this.editor = editor;
        this.databaseService = sessionDatabaseService;
        this.executorService = Executors.newCachedThreadPool();
        timer = new Timer();
    }

    @Override
    public void start(Credentials credentials) {
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

                    if(Objects.nonNull(playing) && playing.getIs_playing()) {
                        log.debug("playing:" + playing.getIs_playing());
                    }
                    else {
                        //remove description
                        log.debug("not playing");
                    }

                } catch (IOException | SpotifyWebApiException | ParseException e) {
                    e.printStackTrace();
                    timer.cancel();
                }
            }
        }, 0, 5000);
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
            Credentials wrappedCredentials = new Credentials();
            wrappedCredentials.setAccessToken(authorizationCodeCredentials.getAccessToken());
            wrappedCredentials.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            this.start(wrappedCredentials);

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            if(e instanceof BadRequestException && e.getMessage().equals("Refresh token revoked")) {
                log.error("Refresh token revoked");
                databaseService.deleteApiIntegrationSetting(Integrations.SPOTIFY.key);
            }
        }
    }


    @Override
    public void stop() {
        //stop polling timer
        timer.cancel();
        timer.purge();
    }
}
