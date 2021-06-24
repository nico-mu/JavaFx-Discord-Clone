package de.uniks.stp.component;

import java.net.URL;

public enum Components {
    USER_LIST("UserList.fxml"),
    USER_LIST_ENTRY("UserListEntry.fxml"),
    NAV_BAR_ELEMENT("NavBarElement.fxml"),
    NAV_BAR_LIST("NavBarList.fxml"),
    PRIVATE_CHAT_VIEW("PrivateChatView.fxml"),
    SERVER_CHAT_VIEW("ServerChatView.fxml"),
    CHAT_MESSAGE("ChatMessage.fxml"),
    SERVER_CATEGORY_LIST("ServerCategoryList.fxml"),
    SERVER_CATEGORY_ELEMENT("ServerCategoryElement.fxml"),
    SERVER_TEXT_CHANNEL_ELEMENT("ServerTextChannelElement.fxml"),
    SERVER_VOICE_CHANNEL_ELEMENT("ServerVoiceChannelElement.fxml"),
    USER_CHECK_LIST_ENTRY("UserCheckListEntry.fxml"),
    EMOTE_PICKER("EmotePicker.fxml"),
    EMOTER_PICKER_BUTTON("EmotePickerButton.fxml"),
    INVITE_LIST_ENTRY("InviteListEntry.fxml"),
    JOIN_SERVER_BUTTON("JoinServerButton.fxml"),
    DIRECT_MESSAGE_LIST_ENTRY("DirectMessageListEntry.fxml");


    public final URL path;

    Components(String path) {
        this.path = Components.class.getResource(path);
    }
}
