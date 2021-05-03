package de.uniks.stp.network;

import de.uniks.stp.Constants;
import kong.unirest.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static de.uniks.stp.Constants.REST_SERVER_BASE_URL;

public class RestClient {
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

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

    public static void getServers(Callback<JsonNode> callback) {
        HttpRequest<?> req = Unirest.get(Constants.SERVER_PATH);
        sendRequest(req, callback);
    }

    protected static void sendRequest(HttpRequest<?> req, Callback<JsonNode> callback) {
        executorService.execute(() -> req.asJsonAsync(callback));
    }
}
