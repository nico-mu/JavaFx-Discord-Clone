package de.uniks.stp.util;

import de.uniks.stp.Constants;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class should be used to detect/return commands or other special texts in a message
 */
public class MessageUtil {

    private final static String FILTERED_COMMANDS = "(!hangman |!hangman|!guess |!guess|!stop |!stop|!imagebot |!imagebot|!randomimage |!randomimage|!tictactoe |!tictactoe)";
    private final static Pattern replyPattern = Pattern.compile("^###quoteInit###.*###.*###.*\\[###.*###.*\\]\\[###.*###.*\\]###quoteStop###$");

    /**
     * Finds invite in message and returns serverId & inviteId if found
     *
     * @param msg
     * @return InviteInfo object, null when no (correct) link was found
     */
    public static InviteInfo getInviteInfo(String msg) {
        // possible problems: multiple links in one message, or '-' in server/invite id
        String invitePrefix = Constants.REST_SERVER_BASE_URL + Constants.REST_SERVER_PATH + "/";
        if (msg.contains(invitePrefix)) {
            try {
                int startIndex = msg.indexOf(invitePrefix);
                int serverIdIndex = startIndex + invitePrefix.length();
                String msgWithoutPrefix = msg.substring(serverIdIndex);

                List<String> splitUrl = Arrays.asList(msgWithoutPrefix.split(Constants.REST_INVITES_PATH + "/"));
                return new InviteInfo().setServerId(splitUrl.get(0)).setInviteId(splitUrl.get(1));
            } catch (Exception e) {
                //happens when the String is not a link or is incorrect
            }
        }
        return null;
    }

    public static boolean isEscapedCommand(String msg) {
        return msg.startsWith("\\");
    }

    public static String filterContent(String msg) {
        if (isEscapedCommand(msg)) {
            return msg;
        }
        if (replyPattern.matcher(msg).matches()) {
            msg = msg.replaceAll("\\[", "").split("###")[4];
        }
        return msg.replaceAll(FILTERED_COMMANDS, "");
    }
}
