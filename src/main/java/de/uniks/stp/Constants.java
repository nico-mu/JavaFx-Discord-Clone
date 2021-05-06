package de.uniks.stp;

public class Constants {

    //Rest Client
    public static final String REST_SERVER_BASE_URL = "https://ac.uniks.de/api";
    // */users for register
    public static final String REGISTER_PATH = "";
    public static final String TEMP_REGISTER_PATH = "/temp";
    public static final String LOGIN_PATH = "/login";
    public static final String LOGOUT_PATH = "/logout";
    public static final String USERS_PATH = "/users";
    public static final String USER_KEY_HEADER_NAME = "userKey";
    public static final String MESSAGE = "message";
    public static final String SERVERS_PATH = "/servers";

    // Labels for internalization
    public static final String LBL_REGISTRATION_FAILED = "LBL_REGISTRATION_FAILED";
    public static final String LBL_REGISTRATION_NAME_TAKEN = "LBL_REGISTRATION_NAME_TAKEN";
    public static final String LBL_LOGIN_FAILED = "LBL_LOGIN_FAILED";
    public static final String LBL_LOGIN_WRONG_CREDENTIALS = "LBL_LOGIN_WRONG_CREDENTIALS";
    public static final String LBL_MISSING_FIELDS = "LBL_MISSING_FIELDS";
}
