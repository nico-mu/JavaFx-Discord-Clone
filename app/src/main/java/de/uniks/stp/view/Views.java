package de.uniks.stp.view;

import java.net.URL;

public enum Views {
    MAIN_SCREEN("MainScreen.fxml"),
    HOME_SCREEN("HomeScreen.fxml"),
    LOGIN_SCREEN("LoginScreen.fxml"),
    SERVER_SCREEN("ServerScreen.fxml"),
    SERVER_CHAT_SCREEN("ServerChatScreen.fxml"),
    USER_INFO_SCREEN("UserInfoScreen.fxml"),
    SETTINGS_MODAL("SettingsModal.fxml"),
    ADD_SERVER_MODAL("AddServerModal.fxml"),
    SERVER_SETTINGS_MODAL("ServerSettingsModal.fxml"),
    CREATE_CATEGORY_MODAL("CreateCategoryModal.fxml"),
    ADD_CHANNEL_MODAL("AddChannelModal.fxml"),
    INVITES_MODAL("InvitesModal.fxml"),
    CREATE_INVITE_MODAL("CreateInviteModal.fxml"),
    EDIT_CATEGORY_MODAL("EditCategoryModal.fxml"),
    EDIT_CHANNEL_MODAL("EditChannelModal.fxml"),
    CONFIRMATION_MODAL("ConfirmationModal.fxml"),
    EASTER_EGG_MODAL("EasterEggModal.fxml"),
    SERVER_VOICE_CHAT_SCREEN("ServerVoiceChatScreen.fxml");

    public final URL path;

    Views(String path) {
        this.path = Views.class.getResource(path);
    }
}
