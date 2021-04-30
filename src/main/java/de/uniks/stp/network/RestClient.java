package de.uniks.stp.network;

import kong.unirest.Callback;
import kong.unirest.HttpRequest;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

import static de.uniks.stp.Constants.REST_SERVER_BASE_URL;

public class RestClient {

    /**
     * Sets config for every http request made with unirest. Sets default url so we don't need to set it
     * on every http request. For more info about the request interceptor
     * @see HttpRequestInterceptor
     */
    static {
        Unirest.config()
            .defaultBaseUrl(REST_SERVER_BASE_URL)
            .interceptor(new HttpRequestInterceptor());
    }

    private static void sendRequest(HttpRequest<?> req, Callback<JsonNode> callback) {
        req.asJsonAsync(callback);
    }
}
