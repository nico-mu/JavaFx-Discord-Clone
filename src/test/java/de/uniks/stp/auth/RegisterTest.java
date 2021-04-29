package de.uniks.stp.auth;

import de.uniks.stp.network.auth.AuthClient;
import org.junit.Assert;
import org.junit.Test;


public class RegisterTest {

    @Test
    public void jsonBodyTest() {
        String body = AuthClient.buildLoginOrRegisterBody("Bob", "password");

        Assert.assertEquals("{\"name\":\"Bob\",\"password\":\"password\"}", body);
    }

}
