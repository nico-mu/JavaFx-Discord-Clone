package de.uniks.stp;

public class Constants {

    //Rest Client
    public static final String REST_SERVER_BASE_URL = "https://ac.uniks.de/api";
    public static final String REST_REGISTER_PATH = "";
    public static final String REST_TEMP_REGISTER_PATH = "/temp";
    public static final String REST_LOGIN_PATH = "/login";
    public static final String REST_LOGOUT_PATH = "/logout";
    public static final String REST_USERS_PATH = "/users";
    public static final String REST_SERVER_PATH = "/servers";

    public static final String USER_KEY_HEADER_NAME = "userKey";
    public static final String MESSAGE = "message";
    public static final String SERVERS_PATH = "/servers";

    // Labels for internalization
    public static final String LBL_REGISTRATION_FAILED = "LBL_REGISTRATION_FAILED";
    public static final String LBL_REGISTRATION_NAME_TAKEN = "LBL_REGISTRATION_NAME_TAKEN";
    public static final String LBL_LOGIN_FAILED = "LBL_LOGIN_FAILED";
    public static final String LBL_LOGIN_WRONG_CREDENTIALS = "LBL_LOGIN_WRONG_CREDENTIALS";
    public static final String LBL_MISSING_FIELDS = "LBL_MISSING_FIELDS";
    public static final String LBL_HOME = "LBL_HOME";
    public static final String LBL_CREATE_SERVER = "LBL_CREATE_SERVER";
    public static final String LBL_SERVERNAME_TITLE = "LBL_SERVERNAME_TITLE";
    public static final String LBL_ENTER_SERVERNAME_PROMPT = "LBL_ENTER_SERVERNAME_PROMPT";
    public static final String LBL_SERVERNAME = "LBL_SERVERNAME";
    public static final String LBL_SELECT_LANGUAGE = "LBL_SELECT_LANGUAGE";
    public static final String LBL_CLOSE_WINDOW = "LBL_CLOSE_WINDOW";

    // Websocket
    public static final String WEBSOCKET_BASE_URL = "wss://ac.uniks.de/ws";
    public static final String WS_SYSTEM_PATH = "/system";

    //route names
    public static final String ROUTE_LOGIN = "/login";
    public static final String ROUTE_MAIN = "/main";
    public static final String ROUTE_HOME = "/home";
    public static final String ROUTE_SERVER = "/server/:id";
}
