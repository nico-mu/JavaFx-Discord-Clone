package de.uniks.stp.network;

import de.uniks.stp.Constants;
import kong.unirest.*;

import javax.json.Json;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static de.uniks.stp.Constants.REST_SERVER_BASE_URL;

public class RestClient {
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public RestClient(){
        Unirest.config()
            .defaultBaseUrl(REST_SERVER_BASE_URL)
            .interceptor(new HttpRequestInterceptor());
    }

    public void stop(){
        executorService.shutdown();
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

    public String buildCreateServerRequest(String name) {
        System.out.println("buildCreateServerRequest called");
        return Json.createObjectBuilder().add("name", name).build().toString();
    }

    public void createServer(String name, String key, Callback<JsonNode> callback) {
        System.out.println("create server called");
        HttpRequest<?> postCreateServer = Unirest.post(Constants.SERVERS_PATH).header("userKey", key)
            .body(buildCreateServerRequest(name));
        sendRequest(postCreateServer, callback);
    }
}
