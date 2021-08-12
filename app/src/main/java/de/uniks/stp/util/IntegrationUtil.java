package de.uniks.stp.util;

import de.uniks.stp.network.integration.IntegrationConstants;
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

    public static String getTrimmedDescription(String description) {
        int remainingSigns = IntegrationConstants.DESCRIPTION_MAX_LENGTH -
            (description.length() + IntegrationConstants.EMPTY_DESCRIPTION_BODY.length() + 1);

        if(remainingSigns < 0) {
            remainingSigns -= IntegrationConstants.DESCRIPTION_SPOILER.length();
            String newDescription = description.substring(0, description.length() + remainingSigns);
            newDescription += IntegrationConstants.DESCRIPTION_SPOILER;
            return newDescription;
        }
        return description;
    }
}
