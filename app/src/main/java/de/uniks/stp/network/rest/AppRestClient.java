package de.uniks.stp.network.rest;

import de.uniks.stp.Constants;
import kong.unirest.Callback;
import kong.unirest.HttpRequest;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

import javax.json.Json;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppRestClient {

    protected static final ExecutorService executorService = Executors.newCachedThreadPool();

    public AppRestClient() {
        Unirest.config()
            .defaultBaseUrl(Constants.REST_SERVER_BASE_URL);
    }

    public static void stop() {
        executorService.shutdown();
        Unirest.shutDown();
    }

    protected void sendRequest(HttpRequest<?> req, Callback<JsonNode> callback) {
        executorService.execute(() -> req.asJsonAsync(callback));
    }

    private void sendAuthRequest(String endpoint, String name, String password, Callback<JsonNode> callback) {
        HttpRequest<?> postUserRegister = Unirest.post(Constants.REST_USERS_PATH + endpoint)
            .body(buildLoginOrRegisterBody(name, password));
        sendRequest(postUserRegister, callback);
    }

    private String buildLoginOrRegisterBody(String name, String password) {
        return Json.createObjectBuilder().add("name", name).add("password", password).build().toString();
    }

    public void register(String name, String password, Callback<JsonNode> callback) {
        sendAuthRequest(Constants.REST_REGISTER_PATH, name, password, callback);
    }

    public void login(String name, String password, Callback<JsonNode> callback) {
        sendAuthRequest(Constants.REST_LOGIN_PATH, name, password, callback);
    }

    public void tempRegister(Callback<JsonNode> callback) {
        HttpRequest<?> postUserRegister = Unirest.post(Constants.REST_USERS_PATH + Constants.REST_TEMP_REGISTER_PATH);
        sendRequest(postUserRegister, callback);
    }
}
