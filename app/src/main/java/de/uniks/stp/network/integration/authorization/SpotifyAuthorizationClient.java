package de.uniks.stp.network.integration.authorization;

import com.sun.net.httpserver.HttpExchange;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.pkce.AuthorizationCodePKCERequest;
import de.uniks.stp.AccordApp;
import de.uniks.stp.network.integration.Credentials;
import de.uniks.stp.network.integration.IntegrationConstants;
import org.apache.hc.core5.http.ParseException;

import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class SpotifyAuthorizationClient extends AbstractAuthorizationClient {

    private SpotifyApi spotifyApi;
    private static String codeVerifier;

    @Inject
    public SpotifyAuthorizationClient(AccordApp app) {
        super(app);
        this.serverPath = this.serverPath + IntegrationConstants.TEMP_SERVER_INTEGRATION_PATH_SPOTIFY;

    }

    @Override
    public void authorize(AuthorizationCallback authorizationCallback) {
        super.authorize(authorizationCallback);

        spotifyApi = new SpotifyApi.Builder()
            .setClientId(IntegrationConstants.SPOTIFY_CLIENT_ID)
            .setRedirectUri(SpotifyHttpManager.makeUri(getRedirectUri()))
            .build();

        codeVerifier = getRandomString(64);

        AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodePKCEUri(generateCodeChallenge())
            .scope(IntegrationConstants.SPOTIFY_AUTHORIZE_SCOPES)
            .show_dialog(true).build();

        //open browser with spotify url, client id and redirectUri
        app.getHostServices().showDocument(authorizationCodeUriRequest.execute().toString());
        stopServerAfterTimeout();
    }

    private String getRedirectUri() {
        return "http://" +
            IntegrationConstants.TEMP_SERVER_HOST + ":" +
            server.getAddress().getPort() + serverPath;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        log.debug(query);
        Map<String, String> splitQueryMap = splitQuery(query);
        String response;
        OutputStream os = exchange.getResponseBody();

        if(splitQueryMap.containsKey("code")) {
            response = "success";
            //make api request here to get spotify api key
            AuthorizationCodePKCERequest authorizationCodePKCERequest =
                spotifyApi.authorizationCodePKCE(splitQueryMap.get("code"), codeVerifier)
                    .build();

            executorService.execute(() -> getCredentials(authorizationCodePKCERequest));
            exchange.sendResponseHeaders(200, response.length());
        }
        else {
            response = "failure";
            executorService.execute(() -> authorizationCallback.onFailure("not authenticated"));
            exchange.sendResponseHeaders(400, response.length());
        }

        os.write(response.getBytes());
        os.close();
        noTimeout();
        stopServer();
    }

    private void getCredentials(AuthorizationCodePKCERequest authorizationCodePKCERequest) {
        try {
            AuthorizationCodeCredentials credentials = authorizationCodePKCERequest.execute();
            Credentials wrappedCredentials = new Credentials();
            wrappedCredentials.setAccessToken(credentials.getAccessToken());
            wrappedCredentials.setRefreshToken(credentials.getRefreshToken());

            authorizationCallback.onSuccess(wrappedCredentials);
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            e.printStackTrace();
            authorizationCallback.onFailure(e.getMessage());
        }
    }

    private String generateCodeChallenge() {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            String codeChallenge = Base64.getUrlEncoder()
                .encodeToString(digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8)));
            return codeChallenge.replaceAll("=", "");
        } catch (NoSuchAlgorithmException ignored) {}
       return null;
    }

    private String getRandomString(int length) {
        List<Character> allowedChars = new ArrayList<>();
        allowedChars.add('-');
        allowedChars.add('_');
        allowedChars.add('.');
        allowedChars.add('~');

        int leftLimit = 32; // space
        int rightLimit = 126; // tilde
        Random random = new Random();

       return random.ints(leftLimit, rightLimit + 1)
            .filter((i) -> (i >= 48 && i <= 57) || (i >= 65 && i <= 90) || (i >= 97 && i <= 122) || allowedChars.contains((char)i))
            .limit(length)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }
}
