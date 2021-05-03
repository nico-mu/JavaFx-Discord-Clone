package de.uniks.stp.controller;

import de.uniks.stp.Editor;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

public class MainScreenController implements ControllerInterface {

    private final String NAV_BAR_ID = "#nav-bar";
    private final String USER_SETTINGS_PANE_ID = "#user-settings-pane";
    private final String SUBVIEW_CONTAINER_ID = "#subview-container";
    private final Parent view;
    private final Editor editor;
    private AnchorPane navBar;
    private AnchorPane userSettingsPane;
    private AnchorPane subViewContainer;
    private NavBarListController navBarController;

    public MainScreenController(Parent view, Editor editor) {
        this.view = view;
        this.editor = editor;
    }

    @Override
    public void init() {
        this.navBar = (AnchorPane) view.lookup(NAV_BAR_ID);
        this.userSettingsPane = (AnchorPane) view.lookup(USER_SETTINGS_PANE_ID);
        this.subViewContainer = (AnchorPane) view.lookup(SUBVIEW_CONTAINER_ID);

        navBarController = new NavBarListController(navBar, editor);
        navBarController.init();
    }

    @Override
    public void stop() {
        navBarController.stop();
    }
}
