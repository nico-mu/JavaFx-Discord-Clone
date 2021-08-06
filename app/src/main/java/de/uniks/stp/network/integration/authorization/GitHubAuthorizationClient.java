package de.uniks.stp.network.integration.authorization;

import com.sun.net.httpserver.HttpExchange;
import de.uniks.stp.AccordApp;
import de.uniks.stp.network.integration.Credentials;
import de.uniks.stp.network.integration.IntegrationConstants;
import kong.unirest.Config;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.UnirestInstance;
import kong.unirest.json.JSONObject;

import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Objects;

public class GitHubAuthorizationClient extends AbstractAuthorizationClient {

    private UnirestInstance instance;

    @Inject
    public GitHubAuthorizationClient(AccordApp app) {
        super(app);
        this.serverPath = this.serverPath + IntegrationConstants.TEMP_SERVER_INTEGRATION_PATH_GITHUB;
        instance = new UnirestInstance(new Config());
    }

    @Override
    public void authorize(AuthorizationCallback authorizationCallback) {
        super.authorize(authorizationCallback);

        String requestUrl = instance.get(IntegrationConstants.GITHUB_AUTH_URL)
            .queryString("client_id", IntegrationConstants.GITHUB_CLIENT_ID)
            .queryString("redirect_uri", getRedirectUri())
            .queryString("scope", IntegrationConstants.GITHUB_SCOPES)
            .getUrl();

        app.showUriInBrowser(requestUrl);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response;
        OutputStream os = exchange.getResponseBody();
        String query = exchange.getRequestURI().getQuery();
        log.debug(query);

        try {
            if(Objects.isNull(query)) {
                throw new MalformedURLException();
            }
            Map<String, String> splitQueryMap = splitQuery(query);

            if(!splitQueryMap.containsKey("code")) {
                throw new MalformedURLException();
            }

            response = "success";
            //make api request here to get github api key
            executorService.execute(() -> getCredentials(splitQueryMap.get("code")));
            exchange.sendResponseHeaders(200, response.length());
        }
        catch (MalformedURLException ex) {
            response = "failure";
            executorService.execute(() -> authorizationCallback.onFailure("not authenticated"));
            exchange.sendResponseHeaders(400, response.length());
        }

        os.write(response.getBytes());
        os.close();
        noTimeout();
        stopServer();
    }

    public void getCredentials(String code) {
        HttpResponse<JsonNode> response = instance.post(IntegrationConstants.GITHUB_OAUTH_URL)
            .field("client_id", IntegrationConstants.GITHUB_CLIENT_ID)
            .field("client_secret", IntegrationConstants.GITHUB_CLIENT_SECRET)
            .field("code", code)
            .field("redirect_uri", getRedirectUri())
            .header("Accept", "application/json")
            .asJson();

        if(response.isSuccess()) {
            JSONObject jsonBody = response.getBody().getObject();
            Credentials credentials = new Credentials();
            credentials.setAccessToken(jsonBody.getString("access_token"));
            authorizationCallback.onSuccess(credentials);
        }
        else {
            authorizationCallback.onFailure("Unknown error in GitHub Authorization");
        }
    }

    @Override
    protected void stopServer() {
        super.stopServer();
        instance.shutDown();
    }
}
