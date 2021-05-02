package de.uniks.stp.network;

import de.uniks.stp.Constants;
import kong.unirest.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static de.uniks.stp.Constants.REST_SERVER_BASE_URL;

public class RestClient {
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    static {
        Unirest.config()
            .defaultBaseUrl(REST_SERVER_BASE_URL)
            .interceptor(new HttpRequestInterceptor());
    }

    protected static void sendRequest(HttpRequest<?> req, Callback<JsonNode> callback) {
        executorService.execute(() -> req.asJsonAsync(callback));
    }

    public static void requestOnlineUsers(final Callback<JsonNode> callback) {
        sendRequest(Unirest.get(Constants.USERS_PATH), callback);
    }
}
