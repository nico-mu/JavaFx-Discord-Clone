package de.uniks.stp.util;

import de.uniks.stp.network.integration.Integrations;

public class IntegrationUtil {

    public static String getPictureNameForIntegration(Integrations integration) {
        return integration.key + ".png";
    }

    public static Integrations getIntegrationForToken(String token) {
        switch (token) {
            case "#":
                return Integrations.SPOTIFY;
            case "?":
                return Integrations.STEAM;
            case "%":
                return Integrations.GITHUB;
            case "=":
                return Integrations.CLUB_PENGUIN;
            default:
                return null;
        }
    }
}
