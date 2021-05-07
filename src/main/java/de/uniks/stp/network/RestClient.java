package de.uniks.stp.network;

import de.uniks.stp.Constants;
import kong.unirest.*;

import javax.json.Json;
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

    public static void stop(){
        executorService.shutdown();
        Unirest.shutDown();
    }

    private void sendRequest(HttpRequest<?> req, Callback<JsonNode> callback) {
        executorService.execute(() -> req.asJsonAsync(callback));
    }

    private void sendAuthRequest(String endpoint, String name, String password, Callback<JsonNode> callback) {
        HttpRequest<?> postUserRegister = Unirest.post(Constants.USERS_PATH + endpoint)
            .body(buildLoginOrRegisterBody(name, password));
        sendRequest(postUserRegister, callback);
    }

    private String buildLoginOrRegisterBody(String name, String password) {
        return Json.createObjectBuilder().add("name", name).add("password", password).build().toString();
    }

    public void register(String name, String password, Callback<JsonNode> callback) {
        sendAuthRequest(Constants.REGISTER_PATH, name, password, callback);
    }

    public void login(String name, String password, Callback<JsonNode> callback) {
        sendAuthRequest(Constants.LOGIN_PATH, name, password, callback);
    }

    public void tempRegister(Callback<JsonNode> callback) {
        HttpRequest<?> postUserRegister = Unirest.post(REST_SERVER_BASE_URL + Constants.USERS_PATH + Constants.TEMP_REGISTER_PATH);
        sendRequest(postUserRegister, callback);
    }
}
