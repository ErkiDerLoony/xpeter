package erki.xpeter;

/**
 * This class contains static helper methods that are convenient for many parser implementations. If
 * these methods are used consistently in all parser implementations it ensures that the bot always
 * reacts in the same way.
 * 
 * @author Edgar Kalkowski
 */
public class BotApi {
    
    /**
     * Checks if the text of a certain message addresses a certain nickname, i.e. the message starts
     * with the nickname optionally followed by a separation character such as a colon.
     * 
     * @param msg
     *        The text of the message to check.
     * @param nick
     *        The nickname to check.
     * @return {@code true} if {@code message} addresses {@code nick}, {@code false} otherwise.
     */
    public static boolean addresses(String msg, String nick) {
        
        if (msg == null || nick == null) {
            return false;
        }
        
        if (msg.startsWith(nick + ":") || msg.startsWith(nick + ",") || msg.startsWith(nick + ";")
                || msg.startsWith(nick)) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Trims the nickname from the beginning of a message if it addresses the nickname (see
     * {@link #addresses(String, String)}).
     * 
     * @param msg
     *        The message to trim.
     * @param nick
     *        The nickname to trim from the beginning of {@code message}.
     * @return {@code msg} if
     */
    public static String trimNick(String msg, String nick) {
        
        if (msg == null) {
            return null;
        }
        
        if (nick == null) {
            return msg;
        }
        
        if (addresses(msg, nick)) {
            msg = msg.substring(nick.length());
            
            if (msg.startsWith(":") || msg.startsWith(";") || msg.startsWith(",")) {
                msg = msg.substring(1);
            }
            
            if (msg.startsWith(" ")) {
                msg = msg.substring(1);
            }
            
            return msg;
        } else {
            return msg;
        }
    }
}
