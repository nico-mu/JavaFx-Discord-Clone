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
    public static final String REST_CATEGORY_PATH = "/categories";
    public static final String REST_CHANNEL_PATH = "/channels";
    public static final String REST_MESSAGES_PATH = "/messages";
    public static final String REST_TIMESTAMP_PATH = "?timestamp=";
    public static final String REST_INVITES_PATH = "/invites";


    public static final String USER_KEY_HEADER_NAME = "userKey";
    public static final String MESSAGE = "message";
    public static final String SERVERS_PATH = "/servers";

    // Labels for internalization
    public static final String LBL_REGISTRATION_FAILED = "LBL_REGISTRATION_FAILED";
    public static final String LBL_REGISTRATION_NAME_TAKEN = "LBL_REGISTRATION_NAME_TAKEN";
    public static final String LBL_LOGIN_FAILED = "LBL_LOGIN_FAILED";
    public static final String LBL_LOGIN_WRONG_CREDENTIALS = "LBL_LOGIN_WRONG_CREDENTIALS";
    public static final String LBL_MISSING_FIELDS = "LBL_MISSING_FIELDS";
    public static final String LBL_FORBIDDEN_CHARS = "LBL_FORBIDDEN_CHARS";
    public static final String LBL_HOME = "LBL_HOME";
    public static final String LBL_CREATE_SERVER = "LBL_CREATE_SERVER";
    public static final String LBL_ADD_SERVER_TITLE = "LBL_ADD_SERVER_TITLE";
    public static final String LBL_CREATE_SERVER_FAILED = "LBL_CREATE_SERVER_FAILED";
    public static final String LBL_ONLINE_USERS = "LBL_ONLINE_USERS";
    public static final String LBL_USER_OFFLINE = "LBL_USER_OFFLINE";
    public static final String LBL_TIME_FORMATTING_TODAY= "LBL_TIME_FORMATTING_TODAY";
    public static final String LBL_TIME_FORMATTING_YESTERDAY = "LBL_TIME_FORMATTING_YESTERDAY";
    public static final String LBL_SELECT_LANGUAGE_TITLE = "LBL_SELECT_LANGUAGE_TITLE";
    public static final String LBL_ON = "LBL_ON";
    public static final String LBL_OFF = "LBL_OFF";
    public static final String LBL_NO_CHANGES = "LBL_NO_CHANGES";
    public static final String LBL_RENAME_SERVER_FAILED = "LBL_RENAME_SERVER_FAILED";
    public static final String LBL_DELETE_SERVER_FAILED = "LBL_DELETE_SERVER_FAILED";
    public static final String LBL_LEAVE_SERVER_FAILED = "LBL_LEAVE_SERVER_FAILED";
    public static final String LBL_EDIT_SERVER_TITLE = "LBL_EDIT_SERVER_TITLE";
    public static final String LBL_DELETE_SERVER = "LBL_DELETE_SERVER";
    public static final String LBL_CONFIRM_DELETE_SERVER = "LBL_CONFIRM_DELETE_SERVER";
    public static final String LBL_CREATE_CATEGORY_FAILED = "LBL_CREATE_CATEGORY_FAILED";
    public static final String LBL_RENAME_CATEGORY_FAILED = "LBL_RENAME_CATEGORY_FAILED";
    public static final String LBL_DELETE_CATEGORY_FAILED = "LBL_DELETE_CATEGORY_FAILED";
    public static final String LBL_CREATE_CATEGORY_TITLE = "LBL_CREATE_CATEGORY_TITLE";
    public static final String LBL_EDIT_CATEGORY_TITLE = "LBL_EDIT_CATEGORY_TITLE";
    public static final String LBL_DELETE_CATEGORY = "LBL_DELETE_CATEGORY";
    public static final String LBL_CONFIRM_DELETE_CATEGORY = "LBL_CONFIRM_DELETE_CATEGORY";
    public static final String LBL_CREATE_CHANNEL = "LBL_CREATE_CHANNEL";
    public static final String LBL_MISSING_NAME = "LBL_MISSING_NAME";
    public static final String LBL_MISSING_MEMBERS = "LBL_MISSING_MEMBERS";
    public static final String LBL_INVITATIONS = "LBL_INVITATIONS";
    public static final String LBL_INVITATION = "LBL_INVITATION";
    public static final String LBL_TYPE = "LBL_TYPE";
    public static final String LBL_CURRENT_MAX = "LBL_CURRENT_MAX";
    public static final String LBL_CREATE_INVITATION = "LBL_CREATE_INVITATION";
    public static final String LBL_CREATE_INVITATION_TIME = "LBL_CREATE_INVITATION_TIME";
    public static final String LBL_MAX = "LBL_MAX";
    public static final String LBL_MISSING_MAX_VALUE = "LBL_MISSING_MAX_VALUE";
    public static final String LBL_CANT_DELETE_INVITATION = "LBL_CANT_DELETE_INVITATION";
    public static final String LBL_NOT_SERVER_OWNER = "LBL_NOT_SERVER_OWNER";
    public static final String LBL_EDIT_CHANNEL = "LBL_EDIT_CHANNEL";
    public static final String LBL_EDIT = "LBL_EDIT";
    public static final String LBL_DELETE = "LBL_DELETE";
    public static final String LBL_DELETE_CHANNEL = "LBL_DELETE_CHANNEL";
    public static final String LBL_CONFIRM_DELETE_CHANNEL = "LBL_CONFIRM_DELETE_CHANNEL";
    public static final String LBL_TEXT_AREA_PLACEHOLDER = "LBL_TEXT_AREA_PLACEHOLDER";
    public static final String LBL_LEAVE_SERVER = "LBL_LEAVE_SERVER";
    public static final String LBL_CONFIRM_LEAVE_SERVER = "LBL_CONFIRM_LEAVE_SERVER";
    public static final String LBL_EASTER_EGG_TITLE = "LBL_EASTER_EGG_TITLE";
    public static final String LBL_CHOOSE_ACTION = "LBL_CHOOSE_ACTION";
    public static final String LBL_RESULT_WIN = "LBL_RESULT_WIN";
    public static final String LBL_RESULT_LOSS = "LBL_RESULT_LOSS";
    public static final String LBL_RESULT_DRAW = "LBL_RESULT_DRAW";
    public static final String LBL_REVANCHE_WAIT = "LBL_REVANCHE_WAIT";
    public static final String LBL_REVANCHE_RESPOND = "LBL_REVANCHE_RESPOND";
    public static final String LBL_GAME_LEFT = "LBL_GAME_LEFT";
    public static final String LBL_GAME_CHALLENGE = "LBL_GAME_CHALLENGE";
    public static final String LBL_GAME_WAIT = "LBL_GAME_WAIT";
    public static final String LBL_EDIT_MESSAGE = "LBL_EDIT_MESSAGE";
    public static final String LBL_EDIT_MESSAGE_FAILED = "LBL_EDIT_MESSAGE_FAILED";
    public static final String LBL_TEXT_CHANNEL = "LBL_TEXT_CHANNEL";
    public static final String LBL_VOICE_CHANNEL = "LBL_VOICE_CHANNEL";
    public static final String LBL_DELETE_MESSAGE_TITLE = "LBL_DELETE_MESSAGE_TITLE";
    public static final String LBL_CONFIRM_DELETE_MESSAGE = "LBL_CONFIRM_DELETE_MESSAGE";

    // Websocket
    public static final String WEBSOCKET_BASE_URL = "wss://ac.uniks.de/ws";
    public static final String WS_SYSTEM_PATH = "/system";
    public static final String WS_USER_PATH = "/chat?user=";
    public static final String WS_SERVER_SYSTEM_PATH = "?serverId=";
    public static final String WS_SERVER_CHAT_PATH = "&serverId=";

    // Audiostream UDP
    public static final String AUDIOSTREAM_BASE_URL = "cranberry.uniks.de";
    public static final int AUDIOSTREAM_PORT = 33100;
    public static final float AUDIOSTREAM_SAMPLE_RATE = 48000f;
    public static final int AUDIOSTREAM_SAMPLE_SIZE_BITS = 16;
    public static final boolean AUDIOSTREAM_SIGNED = true;
    public static final boolean AUDIOSTREAM_BIG_ENDIAN = false;
    public static final int AUDIOSTREAM_CHANNEL = 1;
    public static final int AUDIOSTREAM_METADATA_BUFFER_SIZE = 255;
    public static final int AUDIOSTREAM_AUDIO_BUFFER_SIZE = 1024;

    //route names
    public static final String ROUTE_LOGIN = "/login";
    public static final String ROUTE_MAIN = "/main";
    public static final String ROUTE_HOME = "/home";
    public static final String ROUTE_LIST_ONLINE_USERS = "/online";
    public static final String ROUTE_SERVER = "/server/:id";
    public static final String ROUTE_CHANNEL = "/category/:categoryId/channel/:channelId";
    public static final String ROUTE_PRIVATE_CHAT_ARGS = ":userId";
    public static final String ROUTE_PRIVATE_CHAT = "/chat/" + ROUTE_PRIVATE_CHAT_ARGS;
    public static final String ROUTE_ONLINE = "/online";

    //Languages
    public static final String LANG_LABEL_PREFIX = "LBL_LANG_";

    // Window resolution
    public static final double RES_MAIN_SCREEN_HEIGHT = 750.0d;
    public static final double RES_MAIN_SCREEN_WIDTH = 1300.0d;
    public static final double RES_MIN_MAIN_SCREEN_HEIGHT = 700.0d;
    public static final double RES_MIN_MAIN_SCREEN_WIDTH = 1000.0d;
    public static final double RES_LOGIN_SCREEN_HEIGHT = 355.0d;
    public static final double RES_LOGIN_SCREEN_WIDTH = 400.0d;
    public static final double RES_MIN_LOGIN_SCREEN_HEIGHT = 355.0d;
    public static final double RES_MIN_LOGIN_SCREEN_WIDTH = 400.0d;
}
