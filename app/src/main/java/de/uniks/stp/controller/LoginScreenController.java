package de.uniks.stp.controller;

import com.jfoenix.controls.*;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import de.uniks.stp.AccordApp;
import de.uniks.stp.Constants;
import de.uniks.stp.Editor;
import de.uniks.stp.ViewLoader;
import de.uniks.stp.annotation.Route;
import de.uniks.stp.dagger.components.AppComponent;
import de.uniks.stp.dagger.components.SessionComponent;
import de.uniks.stp.dagger.components.test.AppTestComponent;
import de.uniks.stp.jpa.AccordSettingKey;
import de.uniks.stp.jpa.AppDatabaseService;
import de.uniks.stp.jpa.model.AccordSettingDTO;
import de.uniks.stp.model.User;
import de.uniks.stp.network.rest.AppRestClient;
import de.uniks.stp.router.Router;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.regex.Pattern;

@Route(Constants.ROUTE_LOGIN)
public class LoginScreenController implements ControllerInterface {
    private static final Logger log = LoggerFactory.getLogger(LoginScreenController.class);

    private static final String USERNAME_PATTERN = "[a-zA-Z0-9.:!?,; _-]+";
    private static final Pattern pattern = Pattern.compile(USERNAME_PATTERN);

    private final Parent view;
    private final Editor editor;
    private final AppRestClient restClient;
    private final AppDatabaseService databaseService;
    private final Router router;
    private final ViewLoader viewLoader;
    private final AccordApp app;

    private JFXTextField nameField;
    private JFXPasswordField passwordField;
    private JFXButton registerButton;
    private JFXButton loginButton;
    private JFXButton tempLoginButton;
    private JFXSpinner spinner;
    private JFXCheckBox rememberMeCheckBox;
    private Label errorLabel;

    private String name;
    private String password;
    private boolean tempUserLogin;

    @AssistedInject
    public LoginScreenController(AccordApp app,
                                 Editor editor,
                                 AppDatabaseService databaseService,
                                 AppRestClient restClient,
                                 Router router,
                                 ViewLoader viewLoader,
                                 @Assisted Parent view) {
        this.view = view;
        this.editor = editor;
        this.restClient = restClient;
        this.databaseService = databaseService;
        this.router = router;
        this.viewLoader = viewLoader;
        this.app = app;
    }

    /**
     * Looks up View Objects, activates Buttons and sets default button.
     * Should be called in StageManager::showLoginScreen.
     */
    public void init() {
        nameField = (JFXTextField) view.lookup("#name-field");
        passwordField = (JFXPasswordField) view.lookup("#password-field");
        registerButton = (JFXButton) view.lookup("#register-button");
        loginButton = (JFXButton) view.lookup("#login-button");
        tempLoginButton = (JFXButton) view.lookup("#temp-login-button");
        errorLabel = (Label) view.lookup("#error-message");
        spinner = (JFXSpinner) view.lookup("#spinner");
        rememberMeCheckBox = (JFXCheckBox) view.lookup("#remember-me-checkbox");

        // Register button event handler
        registerButton.setOnAction(this::onRegisterButtonClicked);
        loginButton.setOnAction(this::onLoginButtonClicked);
        tempLoginButton.setOnAction(this::onTempLoginButtonClicked);

        loginButton.setDefaultButton(true);  // Allows to use Enter in order to press login button

        AccordSettingDTO lastUserLogin = databaseService.getAccordSetting(AccordSettingKey.LAST_USER_LOGIN);
        if (Objects.nonNull(lastUserLogin) && Objects.nonNull(lastUserLogin.getValue())) {
            Platform.runLater(() -> {
                nameField.setText(lastUserLogin.getValue());
                passwordField.requestFocus();
            });
        }
    }

    public void stop() {
        registerButton.setOnAction(null);
        loginButton.setOnAction(null);
        tempLoginButton.setOnAction(null);
    }

    private void readInputFields() {
        name = nameField.getText();
        password = passwordField.getText();
    }

    private boolean isEmptyInput() {
        return name.isEmpty() || password.isEmpty();
    }

    /**
     * Changes ErrorLabel to display given error.
     *
     * @param label Constant of resource label
     */
    private void setErrorMessage(String label) {
        if (Objects.isNull(label)) {
            Platform.runLater(() -> {
                errorLabel.setText("");
            });
            return;
        }
        String message = viewLoader.loadLabel(label);
        Platform.runLater(() -> {
            errorLabel.setText(message);
        });
    }

    /**
     * Disables TextFields and Buttons and shows Spinner.
     * Should be called when waiting for server response.
     */
    private void disableUserInput() {
        Platform.runLater(() -> {
            nameField.setDisable(true);
            passwordField.setDisable(true);
            loginButton.setDisable(true);
            tempLoginButton.setDisable(true);
            registerButton.setDisable(true);
            spinner.setVisible(true);
            rememberMeCheckBox.setDisable(true);
        });
    }

    /**
     * Enables TextFields and Buttons and hides Spinner.
     * Should be called when server response is received.
     */
    private void enableUserInput() {
        Platform.runLater(() -> {
            nameField.setDisable(false);
            passwordField.setDisable(false);
            loginButton.setDisable(false);
            tempLoginButton.setDisable(false);
            registerButton.setDisable(false);
            spinner.setVisible(false);
            rememberMeCheckBox.setDisable(false);
        });
    }

    /**
     * Reads TextFields and sends register request.
     *
     * @param event (passed automatically)
     */
    private void onRegisterButtonClicked(ActionEvent event) {
        // read/check TextFields
        readInputFields();
        if (isEmptyInput()) {
            setErrorMessage(Constants.LBL_MISSING_FIELDS);
            return;
        }
        if(isInputValid()) {
            // prepare for and send register request
            setErrorMessage(null);
            disableUserInput();
            restClient.register(name, password, this::handleRegisterResponse);
        }
    }

    /**
     * Reads TextFields and sends login request.
     *
     * @param event (passed automatically)
     */
    private void onLoginButtonClicked(ActionEvent event) {
        // read/check TextFields
        readInputFields();
        if (isEmptyInput()) {
            setErrorMessage(Constants.LBL_MISSING_FIELDS);
            return;
        }
        if (isInputValid()) {
            // prepare for and send login request
            setErrorMessage(null);
            disableUserInput();
            tempUserLogin = false;
            restClient.login(name, password, this::handleLoginResponse);
        }
    }

    /**
     * Requests a temp user and sends login request.
     *
     * @param event (passed automatically)
     */
    private void onTempLoginButtonClicked(ActionEvent event) {
        // prepare for and send login request
        setErrorMessage(null);
        disableUserInput();
        tempUserLogin = true;

        restClient.tempRegister(this::handleTempRegisterResponse);
    }

    private void handleTempRegisterResponse(HttpResponse<JsonNode> response) {
        if (response.isSuccess()) {
            final JSONObject data = response.getBody().getObject().getJSONObject("data");
            name = data.getString("name");
            password = data.getString("password");

            restClient.login(name, password, this::handleLoginResponse);
        } else {
            setErrorMessage(Constants.LBL_REGISTRATION_FAILED);
        }
    }

    /**
     * Checks whether register was successful.
     * If so, user will be logged in.
     * Else, matching error will be shown.
     *
     * @param response contains server response
     */
    private void handleRegisterResponse(HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toPrettyString());
        // log user in
        if (response.isSuccess()) {
            setErrorMessage(null);
            restClient.login(name, password, this::handleLoginResponse);
            return;
        }
        // Registration failed
        passwordField.clear();
        enableUserInput();
        if (response.getBody().getObject().getString(Constants.MESSAGE).equals("Name already taken")){
            setErrorMessage(Constants.LBL_REGISTRATION_NAME_TAKEN);
        }
        else{
            setErrorMessage(Constants.LBL_REGISTRATION_FAILED);
        }
    }

    /**
     * Checks whether login was successful.
     * If so, HomeScreen will be shown.
     * Else, matching error will be shown.
     *
     * @param response contains server response
     */
    private void handleLoginResponse(HttpResponse<JsonNode> response) {
        log.debug(response.getBody().toPrettyString());
        // set currentUser + userKey and switch to HomeScreen
        if (response.isSuccess()) {
            setErrorMessage(null);
            String userKey = response.getBody().getObject().getJSONObject("data").getString("userKey");
            User currentUser = editor.createCurrentUser(name, true).setPassword(password);
            editor.setCurrentUser(currentUser);
            SessionComponent sessionComponent;

            //create session component here
            if(app.isTestMode()) {
                AppTestComponent appTestComponent = (AppTestComponent) app.getAppComponent();
                sessionComponent = appTestComponent.sessionTestComponentBuilder()
                    .userKey(userKey)
                    .currentUser(currentUser)
                    .build();
            }
            else {
                AppComponent appComponent = (AppComponent) app.getAppComponent();
                sessionComponent = appComponent.sessionComponentBuilder()
                    .userKey(userKey)
                    .currentUser(currentUser)
                    .build();
            }
            app.setSessionComponent(sessionComponent);
            sessionComponent.getWebsocketService().init();

            //we have to request all online users here to get the id of the current user and start the integration service
            sessionComponent.getSessionRestClient().requestOnlineUsers(this::handleUserOnlineRequest);

            if (!tempUserLogin && rememberMeCheckBox.isSelected()) {
                // Save in db
                databaseService.saveAccordSetting(AccordSettingKey.LAST_USER_LOGIN, name);
            } else {
                databaseService.saveAccordSetting(AccordSettingKey.LAST_USER_LOGIN, null);
            }

            Platform.runLater(()-> router.route(Constants.ROUTE_MAIN + Constants.ROUTE_HOME + Constants.ROUTE_LIST_ONLINE_USERS));
        }
        else {
            // Login failed
            Platform.runLater(passwordField::clear);
            enableUserInput();
            if (response.getBody().getObject().getString(Constants.MESSAGE).equals("Invalid credentials")){
                setErrorMessage(Constants.LBL_LOGIN_WRONG_CREDENTIALS);
            }
            else{
                setErrorMessage(Constants.LBL_LOGIN_FAILED);
            }
        }
    }

    private void handleUserOnlineRequest(HttpResponse<JsonNode> response) {
        if(response.isSuccess()) {
            final JSONArray data = response.getBody().getObject().getJSONArray("data");
            final User currentUser = editor.getOrCreateAccord().getCurrentUser();
            for (Object o : data) {
                final JSONObject jsonUser = (JSONObject) o;
                final String userId = jsonUser.getString("id");
                final String name = jsonUser.getString("name");
                if(currentUser.getName().equals(name)) {
                    currentUser.setId(userId);
                }
            }

            if(!currentUser.getId().isEmpty()) {
                app.getSessionComponent().getIntegrationService().init();
            }
        }
        else {
            log.warn("Could not request online users");
        }
    }

    private boolean isInputValid() {
        if (!(pattern.matcher(name).matches() && pattern.matcher(password).matches())) {
            setErrorMessage(Constants.LBL_FORBIDDEN_CHARS);
            return false;
        }
        return true;
    }

    @AssistedFactory
    public interface LoginScreenControllerFactory {
        LoginScreenController create(Parent view);
    }

}
