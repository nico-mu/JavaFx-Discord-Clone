package de.uniks.stp.network.auth;

import de.uniks.stp.Constants;
import de.uniks.stp.network.RestClient;
import kong.unirest.Callback;
import kong.unirest.HttpRequest;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

import javax.json.Json;

import static de.uniks.stp.Constants.REST_SERVER_BASE_URL;

public class AuthClient extends RestClient {

    private static void sendAuthRequest(String endpoint, String name, String password, Callback<JsonNode> callback) {
        HttpRequest<?> postUserRegister = Unirest.post(Constants.USERS_PATH + endpoint)
            .body(buildLoginOrRegisterBody(name, password));
        RestClient.sendRequest(postUserRegister, callback);
    }

    public static String buildLoginOrRegisterBody(String name, String password) {
        return Json.createObjectBuilder().add("name", name).add("password", password).build().toString();
    }

    public static void register(String name, String password, Callback<JsonNode> callback) {
        sendAuthRequest(Constants.REGISTER_PATH, name, password, callback);
    }

    public static void login(String name, String password, Callback<JsonNode> callback) {
        sendAuthRequest(Constants.LOGIN_PATH, name, password, callback);
    }

    public static void tempRegister(Callback<JsonNode> callback) {
        HttpRequest<?> postUserRegister = Unirest.post(REST_SERVER_BASE_URL + Constants.USERS_PATH + Constants.TEMP_REGISTER_PATH);
        RestClient.sendRequest(postUserRegister, callback);
    }
}
