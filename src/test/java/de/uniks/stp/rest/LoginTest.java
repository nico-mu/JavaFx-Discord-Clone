package de.uniks.stp.rest;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import de.uniks.stp.Editor;
import de.uniks.stp.StageManager;
import de.uniks.stp.controller.LoginScreenController;
import de.uniks.stp.network.auth.AuthClient;
import de.uniks.stp.view.ViewLoader;
import de.uniks.stp.view.Views;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit.ApplicationTest;

import static org.mockito.Mockito.*;

import java.util.Objects;

public class LoginTest {

    @Mock
    private AuthClient restMock;

    @Mock
    private HttpResponse<JsonNode> res;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;

    @Mock
    LoginScreenController ctrl;


    private Stage stage;
    private StageManager app;

    /*
    @BeforeEach
    public void setup(){
        System.out.println("a");
        MockitoAnnotations.openMocks(this);
    }
     */


    //@Test should be from org.junit.jupiter.api but this causes problems
    @Test
    public void testLogin(){
        System.out.println("b");
        MockitoAnnotations.openMocks(this);


        //mocking
        restMock.login("Guave", "evauG", response -> {});


        JSONObject j = new JSONObject().put("status", "success").put("message", "").put("data", new JSONObject().put("userKey", "123-45"));
        when(res.getBody()).thenReturn(new JsonNode(j.toString()));

        verify(restMock).login(eq("Guave"), eq("evauG"), callbackCaptor.capture());

        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);


        Assert.assertEquals("{}", res.getBody().toString());
    }

    /*
    private static LoginScreenController loginctrl;
    private static Parent view;
    private static Editor editor;


    @BeforeAll
    public static void setup(){
        mock(Views.LOGIN_SCREEN)
        view = ViewLoader.loadView(Views.LOGIN_SCREEN);
        Assert.assertNotNull(view);
        editor = new Editor();
        loginctrl = new LoginScreenController(view, editor);
        loginctrl.init();
    }

    @Test
    public void testLogin(){
        JFXTextField nameField = (JFXTextField) view.lookup("#name-field");
        JFXPasswordField passwordField = (JFXPasswordField) view.lookup("#password-field");
        nameField.setText("Guave");
        passwordField.setText("evauG");

        JFXButton loginButton = (JFXButton) view.lookup("#login-button");
        loginButton.fireEvent(new ActionEvent());
    }
    */

    /*
    //TestFX
        //check initial window title
        Assert.assertEquals("Accord - Login", this.stage.getTitle());

        clickOn("#name-field");
        write("Guave");
        clickOn("#password-field");
        write("evauG");
        clickOn("#login-button");
     */

    //when(res.getBody()).thenReturn(new JsonNode("{\"status\":\"success\",\"message\":\"\",\"data\":{\"userKey\":\"123-45\"}}"));
}
