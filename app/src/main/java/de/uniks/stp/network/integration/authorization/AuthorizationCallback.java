package de.uniks.stp.network.integration.authorization;

import de.uniks.stp.network.integration.Credentials;

public interface AuthorizationCallback {

    void onSuccess(Credentials credentials);

    void onFailure(String errorMessage);
}
