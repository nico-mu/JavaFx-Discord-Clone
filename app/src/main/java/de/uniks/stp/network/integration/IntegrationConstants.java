package de.uniks.stp.network.integration;

import javax.json.Json;

public class IntegrationConstants {

    public static final String TEMP_SERVER_HOST = "localhost";
    public static final String TEMP_SERVER_INTEGRATION_PATH = "/integration";
    public static final String TEMP_SERVER_INTEGRATION_PATH_SPOTIFY = "/spotify";
    public static final String TEMP_SERVER_INTEGRATION_PATH_GITHUB = "/github";
    public static final int TEMP_SERVER_PORT = 8001;
    public static final int AUTHORIZATION_TIMEOUT = 7200000;
    public static final String EMPTY_DESCRIPTION_BODY = Json.createObjectBuilder().add("desc", "").build().toString();
    public static final int DESCRIPTION_MAX_LENGTH = 120;
    public static final String DESCRIPTION_SPOILER = "...";

    /*** SPOTIFY ***/
    public static final String SPOTIFY_CLIENT_ID = "53e68e034d0b4f3781483049367ca88c";
    public static final String SPOTIFY_AUTHORIZE_SCOPES = "user-read-currently-playing";
    public static final int SPOTIFY_POLL_INTERVAL = 5000;

    /*** GITHUB ***/
    public static final String GITHUB_CLIENT_ID = "4d3d325ecc248e6322d8";
    public static final String GITHUB_CLIENT_SECRET = "6c732e53490472a5f57a81b6935c839332d42b0c";
    public static final int GITHUB_POLL_INTERVAL = 1800000;
    public static final String GITHUB_AUTH_URL = "https://github.com/login/oauth/authorize";
    public static final String GITHUB_OAUTH_URL = "https://github.com/login/oauth/access_token";
    public static final String GITHUB_SCOPES = "read:user";
    public static final String GITHUB_API_URL = "https://api.github.com";
    public static final String GITHUB_API_USER_URL = "/user";
    public static final String GITHUB_API_ACCEPT_HEADER = "application/vnd.github.v3+json";

}
