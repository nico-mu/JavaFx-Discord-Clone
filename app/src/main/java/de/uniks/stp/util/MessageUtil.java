package de.uniks.stp.util;

import de.uniks.stp.Constants;
import javafx.util.Pair;

/**
 * This class should be used to detect/return commands or other special texts in a message
 */
public class MessageUtil {

    /**
     * Finds invite in message and returns serverId & inviteId if found
     * @param msg
     * @return pair with serverId as key and inviteId as value, null when no (correct) link was found
     */
    public static Pair<String, String> getInviteIds(String msg){
        // possible problems: multiple links in one message, or '-' in server/invite id
        String invitePrefix = Constants.REST_SERVER_BASE_URL + Constants.REST_SERVER_PATH + "/";
        if(msg.contains(invitePrefix)){
            try{
                int startIndex = msg.indexOf(invitePrefix);
                int serverIdIndex = startIndex+invitePrefix.length();
                String msgWithoutPrefix = msg.substring(serverIdIndex);
                String serverId = msgWithoutPrefix.split(Constants.REST_INVITES_PATH+"/")[0];

                String inviteIdPart = msgWithoutPrefix.split(Constants.REST_INVITES_PATH+"/")[1];
                String inviteId = inviteIdPart.split("[ "+ System.getProperty("line.separator") + "]")[0];
                return new Pair<String, String>(serverId, inviteId);
            } catch(Exception e){
                //happens when the String is not a link or is incorrect
            }
        }
        return null;
    }
}
