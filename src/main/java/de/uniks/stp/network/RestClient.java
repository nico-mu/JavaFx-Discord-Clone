package de.uniks.stp.network;

import kong.unirest.*;

import static de.uniks.stp.Constants.REST_SERVER_BASE_URL;

public class RestClient {

    public RestClient(){
        Unirest.config()
            .defaultBaseUrl(REST_SERVER_BASE_URL)
            .interceptor(new HttpRequestInterceptor());
    }

    public void sendRequest(HttpRequest<?> req, Callback<JsonNode> callback) {
        new Thread(() -> req.asJsonAsync(callback)).start();
    }
}
