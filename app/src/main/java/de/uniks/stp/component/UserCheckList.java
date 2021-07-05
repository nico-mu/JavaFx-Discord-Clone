package de.uniks.stp.component;

import de.uniks.stp.ViewLoader;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class UserCheckList extends ScrollPane {

    private final ObservableList<Node> userListEntries;
    @FXML
    private VBox container;
    private HashMap<String, UserCheckListEntry> userCheckListEntryHashMap;

    @Inject
    public UserCheckList(ViewLoader viewLoader) {
        final FXMLLoader fxmlLoader = viewLoader.getFXMLComponentLoader(Components.USER_LIST);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        userCheckListEntryHashMap = new HashMap<>();

        try {
            fxmlLoader.load();
            userListEntries = container.getChildren();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void addUserCheckListEntry(final UserCheckListEntry item) {
        userListEntries.add(item);
    }

    public void addUserToChecklist(UserCheckListEntry userCheckListEntry) {
        userCheckListEntryHashMap.put(userCheckListEntry.getUserName(), userCheckListEntry);
        userListEntries.add(userCheckListEntry);
    }

    public void filterUsers(String subString) {
        clearUserCheckList();
        for (String name : userCheckListEntryHashMap.keySet()) {
            if (name.contains(subString)) {
                addUserCheckListEntry(userCheckListEntryHashMap.get(name));
            }
        }
    }

    public ArrayList<String> getSelectedUserIds() {
        ArrayList<String> selectedUserIds = new ArrayList<>();
        for (UserCheckListEntry userCheckListEntry : userCheckListEntryHashMap.values()) {
            if (userCheckListEntry.isUserSelected()) {
                selectedUserIds.add(userCheckListEntry.getUserId());
            }
        }
        return selectedUserIds;
    }

    public void clearUserCheckList() {
        userListEntries.clear();
    }
}

